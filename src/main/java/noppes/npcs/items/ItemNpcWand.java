package noppes.npcs.items;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import noppes.npcs.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.CustomNPCsScheduler;

public class ItemNpcWand extends Item {
	
    public ItemNpcWand(){
		super(new Item.Properties().stacksTo(1));
    }

	@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
    	ItemStack itemstack = player.getItemInHand(hand);
		if(!level.isClientSide)
			return new InteractionResultHolder(InteractionResult.SUCCESS, itemstack);
		CustomNpcs.proxy.openGui(player, EnumGuiType.NpcRemote);
		return new InteractionResultHolder(InteractionResult.SUCCESS, itemstack);
    }

	@Override
	public int getUseDuration(ItemStack p_77626_1_) {
		return 72000;
	}

	@Override
	public InteractionResult useOn(UseOnContext context){
		if(context.getLevel().isClientSide)
			return InteractionResult.SUCCESS;
		
		if(CustomNpcs.OpsOnly && !context.getPlayer().getServer().getPlayerList().isOp(context.getPlayer().getGameProfile())){
			context.getPlayer().sendSystemMessage(Component.translatable("availability.permission"));
		} else if (CustomNpcsPermissions.hasPermission((ServerPlayer) context.getPlayer(), CustomNpcsPermissions.NPC_CREATE)) {
			final EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, context.getLevel());
	    	npc.ais.setStartPos(context.getClickedPos().above());
			npc.moveTo(context.getClickedPos().getX() + 0.5F, npc.getStartYPos(), context.getClickedPos().getZ() + 0.5F, context.getPlayer().getYRot(), context.getPlayer().getXRot());

			context.getLevel().addFreshEntity(npc);
			npc.setHealth(npc.getMaxHealth());

			CustomNPCsScheduler.runTack(() -> NoppesUtilServer.sendOpenGui(context.getPlayer(), EnumGuiType.MainMenuDisplay, npc), 100);
		}
		else
			((ServerPlayer) context.getPlayer()).sendSystemMessage(Component.translatable("availability.permission"));
        return InteractionResult.SUCCESS;
    }

	@Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity playerIn){
        return stack;
    }
}
