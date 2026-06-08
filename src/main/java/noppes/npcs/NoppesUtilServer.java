package noppes.npcs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.api.constants.QuestType;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.*;
import noppes.npcs.packets.server.SPacketGuiOpen;
import noppes.npcs.shared.common.CommonUtil;
import noppes.npcs.shared.common.util.LogWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class NoppesUtilServer {
	private static HashMap<UUID,Quest> editingQuests = new HashMap<UUID,Quest>();
	private static HashMap<UUID,Quest> editingQuestsClient = new HashMap<UUID,Quest>();

    public static void setEditingNpc(Player player, EntityNPCInterface npc){
    	PlayerData data = PlayerData.get(player);
    	data.editingNpc = npc;
    	if(npc != null)
			Packets.send((ServerPlayer)player, new PacketNpcEdit(npc.getId()));
    }
    public static EntityNPCInterface getEditingNpc(Player player){
    	PlayerData data = PlayerData.get(player);
    	return data.editingNpc;
    }

	public static void setEditingQuest(Player player, Quest quest) {
		if(player.level().isClientSide) {
			editingQuestsClient.put(player.getUUID(), quest);
		}
		else {
			editingQuests.put(player.getUUID(), quest);
		}
	}
    public static Quest getEditingQuest(Player player){
		if(player.level().isClientSide) {
			return editingQuestsClient.get(player.getUUID());
    	}
		return editingQuests.get(player.getUUID());
    }
	
	public static void openDialog(Player player, EntityNPCInterface npc, Dialog dia){
		Dialog dialog = dia.copy(player);
		PlayerData playerdata = PlayerData.get(player);

		if(EventHooks.onNPCDialog(npc, player, dialog)) {
			playerdata.dialogId = -1;
			return;
		}
		playerdata.dialogId = dialog.id;
		
		if(npc instanceof EntityDialogNpc || dia.id < 0){
			dialog.hideNPC = true;
			Packets.send((ServerPlayer)player, new PacketDialogDummy(npc.getName().getString(), dialog.save(new CompoundTag())));
		}
		else
			Packets.send((ServerPlayer)player, new PacketDialog(npc.getId(), dialog.id));
		dia.factionOptions.addPoints(player);
        if(dialog.hasQuest())
        	PlayerQuestController.addActiveQuest(dialog.getQuest(),player);
        if(!dialog.command.isEmpty()){       
            runCommand(npc, npc.getName().getString(), dialog.command, player);
        }
        if(dialog.mail.isValid())
        	PlayerDataController.instance.addPlayerMessage(player.getServer(), player.getName().getString(), dialog.mail);
        
        PlayerDialogData data = playerdata.dialogData;
        if(!data.dialogsRead.contains(dialog.id) && dialog.id >= 0){
        	data.dialogsRead.add(dialog.id);
	        playerdata.updateClient = true;
        }
		setEditingNpc(player, npc);
		playerdata.questData.checkQuestCompletion(player, QuestType.DIALOG);
	}
	
	public static String runCommand(Entity executer, String name, String command, Player player){
		return runCommand(executer.getCommandSenderWorld(), executer.blockPosition(), name, command, player, executer);
	}

	public static String runCommand(final Level level, final BlockPos pos, final String name, String command, Player player, final Entity executer){
		if(!level.getServer().isCommandBlockEnabled()){
			CommonUtil.NotifyOPs(level.getServer(), "Cant run commands if CommandBlocks are disabled");
			LogWriter.warn("Cant run commands if CommandBlocks are disabled");
			return "Cant run commands if CommandBlocks are disabled";
		}
		
        if(player != null)
        	command = command.replace("@dp", player.getName().getString());
    	command = command.replace("@npc", name);

    	Component output = Component.literal("");

		CommandSource icommandsender = new CommandSource(){


			@Override
			public void sendSystemMessage(Component component) {
				((MutableComponent)output).append(component);
			}

			@Override
			public boolean acceptsSuccess() {
				return true;
			}

			@Override
			public boolean shouldInformAdmins() {
				return level.getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
			}

			@Override
			public boolean acceptsFailure() {
				return true;
			}
		};
		int permLvl = CustomNpcs.NpcUseOpCommands ? 4 : 2;
		Vec3 point = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5D, pos.getZ() + 0.5D);

		CommandSourceStack commandSource = new CommandSourceStack(icommandsender, point, Vec2.ZERO, (ServerLevel) level, permLvl, "@CustomNPCs-" + name, Component.literal("@CustomNPCs-" + name), level.getServer(), executer){

			@Override
			public void sendFailure(Component text) {
				super.sendFailure(text);
				CommonUtil.NotifyOPs(level.getServer(), text);
			}
		};


		Commands icommandmanager = level.getServer().getCommands();
		icommandmanager.performPrefixedCommand(commandSource, command);
        
        if(output.getString().isEmpty())
        	return null;

        return output.getString();
	}

	public static void sendOpenGui(Player player,
			EnumGuiType gui, EntityNPCInterface npc) {
		SPacketGuiOpen.sendOpenGui(player, gui, npc, BlockPos.ZERO);
	}

	private static MenuType getType(EnumGuiType gui){
		if(gui == EnumGuiType.PlayerAnvil){
			return CustomContainer.container_carpentrybench;
		}
		if(gui == EnumGuiType.CustomGui){
			return CustomContainer.container_customgui;
		}
		if(gui == EnumGuiType.PlayerBankUnlock){
			return CustomContainer.container_bankunlock;
		}
		if(gui == EnumGuiType.PlayerBankLarge){
			return CustomContainer.container_banklarge;
		}
		if(gui == EnumGuiType.PlayerBankUprade){
			return CustomContainer.container_bankupgrade;
		}
		if(gui == EnumGuiType.PlayerBankSmall){
			return CustomContainer.container_banksmall;
		}
		if(gui == EnumGuiType.PlayerMailman){
			return CustomContainer.container_mail;
		}
		if(gui == EnumGuiType.MainMenuInv){
			return CustomContainer.container_inv;
		}
		if(gui == EnumGuiType.QuestItem){
			return CustomContainer.container_questtypeitem;
		}
		if(gui == EnumGuiType.QuestReward){
			return CustomContainer.container_questreward;
		}
		if(gui == EnumGuiType.CompanionInv){
			return CustomContainer.container_companion;
		}
		if(gui == EnumGuiType.PlayerTrader){
			return CustomContainer.container_trader;
		}
		if(gui == EnumGuiType.PlayerFollower){
			return CustomContainer.container_follower;
		}
		if(gui == EnumGuiType.PlayerFollowerHire){
			return CustomContainer.container_followerhire;
		}
		if(gui == EnumGuiType.SetupTrader){
			return CustomContainer.container_tradersetup;
		}
		if(gui == EnumGuiType.SetupFollower){
			return CustomContainer.container_followersetup;
		}
		if(gui == EnumGuiType.SetupItemGiver){
			return CustomContainer.container_itemgiver;
		}
		if(gui == EnumGuiType.ManageBanks){
			return CustomContainer.container_managebanks;
		}

		return null;
	}

	public static void openContainerGui(ServerPlayer player, EnumGuiType gui, Consumer<FriendlyByteBuf> extraDataWriter){
		FriendlyByteBuf outerbuf = new FriendlyByteBuf(Unpooled.buffer());
		extraDataWriter.accept(outerbuf);
		ByteBuf copy = outerbuf.copy();
		player.openMenu(new ExtendedScreenHandlerFactory() {
			@Override
			public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
				buf.writeBytes(copy);
			}

			@Override
			public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
				return ((ExtendedScreenHandlerType)getType(gui)).create(p_createMenu_1_, p_createMenu_2_, outerbuf);
			}

			@Override
			public Component getDisplayName() {
				return Component.literal(gui.name());
			}
		});
	}

	public static void spawnParticle(Entity entity,String particle,int dimension){
		Packets.sendNearby(entity, new PacketParticle(entity.getX(), entity.getY(), entity.getZ(), entity.getBbHeight(), entity.getBbWidth(), particle));
    }

	public static void sendScrollData(ServerPlayer player, Map<String,Integer> map){
		Packets.send(player, new PacketGuiScrollData(map));
	}

	public static void sendGuiError(Player player, int i) {
		Packets.send((ServerPlayer)player, new PacketGuiError(i, new CompoundTag()));
	}

	public static void sendGuiClose(ServerPlayer player, int i, CompoundTag comp) {
		Packets.send(player, new PacketGuiClose(comp));
	}

    public static void GivePlayerItem(Entity entity, Player player, ItemStack item) {
        if (entity.level().isClientSide || item == null || item.isEmpty()) {
            return;
        }
        item = item.copy();
        float f = 0.7F;
        double d = (double) (entity.level().random.nextFloat() * f) + (double) (1.0F - f);
        double d1 = (double) (entity.level().random.nextFloat() * f) + (double) (1.0F - f);
        double d2 = (double) (entity.level().random.nextFloat() * f) + (double) (1.0F - f);
        ItemEntity entityitem = new ItemEntity(entity.level(), entity.getX() + d, entity.getY() + d1, entity.getZ() + d2,
                item);
        entityitem.setPickUpDelay(2);
        entity.level().addFreshEntity(entityitem);

        if (player.getInventory().add(item)) {
            entity.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.take(entityitem, item.getCount());
    		PlayerQuestData playerdata = PlayerData.get(player).questData;
    		playerdata.checkQuestCompletion(player, QuestType.ITEM);

            if (item.getCount() <= 0) {
                entityitem.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

	
	public static BlockPos GetClosePos(BlockPos origin, Level level){
		for(int x = -1; x < 2; x++){
			for(int z = -1; z < 2; z++){
				for(int y = 2; y >= -2; y--){
					BlockPos pos = origin.offset(x, y, z);
					BlockState state = level.getBlockState(pos.above());
					if(state.isRedstoneConductor(level, pos) && level.isEmptyBlock(pos.above()) && level.isEmptyBlock(pos.above(2)))
						return pos.above();
				}
			}
		}
		return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin);
	}

	public static void playSound(LivingEntity entity, SoundEvent sound, float volume, float pitch) {
		entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
	}

	public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource cat, float volume, float pitch) {
		level.playSound(null, pos, sound, cat, volume, pitch);
	}

	public static Player getPlayer(MinecraftServer minecraftserver, UUID id) {
		List<ServerPlayer> list = minecraftserver.getPlayerList().getPlayers();
		for(Player player : list){
			if(id.equals(player.getUUID()))
				return player;
		}
        return null;
	}
	public static Entity GetDamageSourcee(DamageSource damagesource) {
		Entity entity = damagesource.getEntity();
		if(entity == null)
			entity = damagesource.getDirectEntity();
		if ((entity instanceof EntityProjectile) && ((EntityProjectile) entity).getOwner() instanceof LivingEntity)
            entity = ((AbstractArrow) entity).getOwner();
		else if ((entity instanceof ThrowableProjectile))
			entity = ((ThrowableProjectile) entity).getOwner();
		return entity;
	}
	public static boolean IsItemStackNull(ItemStack is) {
		return is == null || is.isEmpty() || is == ItemStack.EMPTY || is.getItem() == null;
	}
	public static ItemStack ChangeItemStack(ItemStack is, Item item) {
		CompoundTag comp = is.save(new CompoundTag());
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(item);
        comp.putString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
		return ItemStack.of(comp);
	}
}
