package noppes.npcs;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import noppes.npcs.api.constants.QuestType;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemSoulstoneEmpty;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiCloneOpen;
import noppes.npcs.packets.client.PacketGuiOpen;
import noppes.npcs.packets.client.PacketMarkData;
import noppes.npcs.quests.QuestKill;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class ServerEventsHandler implements UseEntityCallback, ServerLivingEntityEvents.AfterDeath {

	public static Villager Merchant;

	@Override
	public InteractionResult interact(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		ItemStack item = player.getMainHandItem();
		boolean isCancelled = false;
		if(item.isEmpty() || hand != InteractionHand.MAIN_HAND)
			return InteractionResult.PASS;
		boolean isClientSide = player.level().isClientSide;
		boolean npcInteracted = entity instanceof EntityNPCInterface;

		if(!isClientSide && CustomNpcs.OpsOnly && !player.getServer().getPlayerList().isOp(player.getGameProfile())){
			return InteractionResult.PASS;
		}
		
		if(!isClientSide && item.getItem() == CustomItems.soulstoneEmpty && entity instanceof LivingEntity) {
			((ItemSoulstoneEmpty)item.getItem()).store((LivingEntity)entity, item, player);
		}
		
		if(item.getItem() == CustomItems.wand && npcInteracted && !isClientSide){
			if (!CustomNpcsPermissions.hasPermission((ServerPlayer) player, CustomNpcsPermissions.NPC_GUI)){
				return InteractionResult.PASS;
			}
			isCancelled=(true);
//			if(event.getEntity().isCrouching()){
//				MainMenuGui.open(event.getEntity(), (EntityCustomNpc) event.getTarget());
//			}
//			else{
//				NoppesUtilServer.sendOpenGui(event.getEntity(), EnumGuiType.MainMenuDisplay, (EntityNPCInterface) event.getTarget());
//			}
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) entity);
		}
		else if(item.getItem() == CustomItems.cloner && !isClientSide && !(entity instanceof Player)){
			CompoundTag compound = new CompoundTag();
			if(!entity.saveAsPassenger(compound))
				return InteractionResult.PASS;
			PlayerData data = PlayerData.get(player);
			ServerCloneController.Instance.cleanTags(compound);
			Packets.send((ServerPlayer)player, new PacketGuiCloneOpen(compound));
			//event.getEntity().sendMessage(new TextComponent("Entity too big to clone"));
			data.cloned = compound;
			isCancelled=(true);
		}
		else if(item.getItem() == CustomItems.scripter && !isClientSide && npcInteracted){
			if(!CustomNpcsPermissions.hasPermission((ServerPlayer) player, CustomNpcsPermissions.NPC_GUI))
				return InteractionResult.PASS;
	    	NoppesUtilServer.setEditingNpc(player, (EntityNPCInterface)entity);
			isCancelled=(true);

			Packets.send((ServerPlayer) player, new PacketGuiOpen(EnumGuiType.Script, BlockPos.ZERO));
		}
		else if(item.getItem() == CustomItems.mount && !isClientSide){
			if(!CustomNpcsPermissions.hasPermission((ServerPlayer) player, CustomNpcsPermissions.TOOL_MOUNTER))
				return InteractionResult.PASS;
			PlayerData data = PlayerData.get(player);
			isCancelled=(true);
			data.mounted = entity;
			Packets.send((ServerPlayer) player, new PacketGuiOpen(EnumGuiType.MobSpawnerMounter, BlockPos.ZERO));
		}
		return isCancelled?InteractionResult.FAIL:InteractionResult.PASS;
//		else if(item.getItem() == CustomItems.wand && event.getTarget() instanceof Villager){
//			if(!CustomNpcsPermissions.hasPermission((ServerPlayer) event.getEntity(), CustomNpcsPermissions.EDIT_VILLAGER))
//				return;
//			event.setCanceled(true);
//			Merchant = (Villager)event.getTarget();
//
//			if(!isClientSide){
//				ServerPlayer player = (ServerPlayer) event.getEntity();
//				//TODO fix
//				//NoppesUtilServer.openContainerGui(player, EnumGuiType.MerchantAdd, null, BlockPos.ZERO);
//		        //MerchantRecipeList merchantrecipelist = Merchant.getRecipes(player);
//
//		        //if (merchantrecipelist != null)
//		        //{
//	            //	Packets.send(player, new PacketMerchantList(merchantrecipelist));
//		        //}
//			}
//		}
	}

	@Override
	public void afterDeath(LivingEntity entity, DamageSource damageSource) {
		if(entity.level().isClientSide)
			return;
		Entity source = NoppesUtilServer.GetDamageSourcee(damageSource);
		if(source != null){			
			if(source instanceof EntityNPCInterface && entity != null){
				EntityNPCInterface npc = (EntityNPCInterface) source;
				Line line = npc.advanced.getKillLine();
				if(line != null)
					npc.saySurrounding(Line.formatTarget(line, entity));
				EventHooks.onNPCKills(npc, entity);
			}
			
			Player player = null;
			if(source instanceof Player)
				player = (Player) source;
			else if(source instanceof EntityNPCInterface && ((EntityNPCInterface)source).getOwner() instanceof Player)
				player = (Player) ((EntityNPCInterface)source).getOwner();
			if(player != null){
				doQuest(player, entity, true);
		
				if(entity instanceof EntityNPCInterface)
					doFactionPoints(player, (EntityNPCInterface)entity);
			}
		}
		if(entity instanceof Player){
			PlayerData data = PlayerData.get((Player)entity);
			data.save(false);
		}
	}

	private void doFactionPoints(Player player, EntityNPCInterface npc) {
		npc.advanced.factions.addPoints(player);
	}

	private void doQuest(Player player, LivingEntity entity, boolean all) {
		PlayerData pdata = PlayerData.get(player);
		PlayerQuestData playerdata = pdata.questData;
		String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
		if(entity instanceof Player)
			entityName = "Player";
		
		for(QuestData data : playerdata.activeQuests.values()){
			if(data.quest.type != QuestType.KILL && data.quest.type != QuestType.AREA_KILL)
				continue;
			if(data.quest.type == QuestType.AREA_KILL && all){
				List<Player> list = player.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(10, 10, 10));
				for(Player pl : list)
					if(pl != player)
						doQuest(pl, entity, false);
			
			}
			String name = entityName;
			QuestKill quest = (QuestKill) data.quest.questInterface;
			if(quest.targets.containsKey(entity.getName().getString()))
				name = entity.getName().getString();
			else if(!quest.targets.containsKey(name))
				continue;
			HashMap<String, Integer> killed = quest.getKilled(data);
			if(killed.containsKey(name) && killed.get(name) >= quest.targets.get(name))
				continue;
			int amount = 0;
			if(killed.containsKey(name))
				amount = killed.get(name);
			killed.put(name, amount + 1);
			quest.setKilled(data, killed);
			pdata.updateClient = true;
		}
		playerdata.checkQuestCompletion(player, QuestType.KILL);
		playerdata.checkQuestCompletion(player, QuestType.AREA_KILL);
	}

	public static boolean allowCommandMessage(ParseResults<CommandSourceStack> parseRes) {
		String command = parseRes.getReader().getString();
		if(command.startsWith("give ")){
			try {
				CommandContext<CommandSourceStack> context = parseRes.getContext().build(parseRes.getReader().getString());
				Collection<ServerPlayer> players =  EntityArgument.getPlayers(context, "targets");

				for(ServerPlayer player : players){
					player.getServer().execute(ListenableFutureTask.create(Executors.callable(() -> {
						PlayerQuestData playerdata = PlayerData.get(player).questData;
						playerdata.checkQuestCompletion(player, QuestType.ITEM);
					})));
				}
			}
			catch(Throwable ignored) {

			}
		}

		if(command.startsWith("time ")){
			try {
				CustomNpcs.Server.submit(() -> {
					List<ServerPlayer> players = CustomNpcs.Server.getPlayerList().getPlayers();
					for(ServerPlayer playerMP:players){
						VisibilityController.instance.onUpdate(playerMP);
					}
				});
			}
			catch(Throwable ignored) {

			}
		}

		return true;
	}
}
