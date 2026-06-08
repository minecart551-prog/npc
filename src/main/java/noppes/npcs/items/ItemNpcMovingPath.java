package noppes.npcs.items;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.CustomTabs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;


public class ItemNpcMovingPath extends Item{
    public ItemNpcMovingPath(){
		super(new Item.Properties().stacksTo(1));
    }

	@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
    	ItemStack itemstack = player.getItemInHand(hand);
		if(level.isClientSide || !CustomNpcsPermissions.hasPermission((ServerPlayer) player, CustomNpcsPermissions.TOOL_PATHER))
	        return new InteractionResultHolder(InteractionResult.PASS, itemstack);
		EntityNPCInterface npc = getNpc(itemstack, level);
		if(npc != null)
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.MovingPath, npc);
        return new InteractionResultHolder(InteractionResult.SUCCESS, itemstack);
    }
    
	@Override
	public InteractionResult useOn(UseOnContext context){
    	if(context.getLevel().isClientSide || !CustomNpcsPermissions.hasPermission((ServerPlayer) context.getPlayer(), CustomNpcsPermissions.TOOL_PATHER))
			return InteractionResult.FAIL;
		ItemStack stack = context.getItemInHand();
		EntityNPCInterface npc = getNpc(stack, context.getLevel());
		if(npc == null)
			return InteractionResult.PASS;
		List<int[]> list = npc.ais.getMovingPath();
		int[] pos = list.get(list.size() - 1);
		
		int x = context.getClickedPos().getX(), y = context.getClickedPos().getY(), z = context.getClickedPos().getZ();
		list.add(new int[]{x,y,z});
		
        double d3 = x - pos[0];
        double d4 = y - pos[1];
        double d5 = z - pos[2];
        double distance = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

		context.getPlayer().sendSystemMessage(Component.translatable("message.pather.added", x, y, z, npc.getName()));
        if(distance > CustomNpcs.NpcNavRange)
			((ServerPlayer) context.getPlayer()).sendSystemMessage(Component.translatable("message.pather.farwarning", CustomNpcs.NpcNavRange));
		
		return InteractionResult.SUCCESS;
    }
	
	private EntityNPCInterface getNpc(ItemStack item, Level level){
		if(level.isClientSide || item.getTag() == null)
			return null;
		
		Entity entity = level.getEntity(item.getTag().getInt("NPCID"));
		if(entity == null || !(entity instanceof EntityNPCInterface))
			return null;
		
		return (EntityNPCInterface) entity;
	}
}
