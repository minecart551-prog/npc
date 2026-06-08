package noppes.npcs.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import noppes.npcs.CustomTabs;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.packets.server.SPacketGuiOpen;

public class ItemScriptedDoor extends DoubleHighBlockItem {
	
    public ItemScriptedDoor(Block block){
    	super(block, new Item.Properties().stacksTo(1));
    }
	
    @Override
    public InteractionResult useOn(UseOnContext context){
    	InteractionResult res = super.useOn(context);
    	if(res == InteractionResult.SUCCESS && !context.getLevel().isClientSide){
            PlayerData data = PlayerData.get(context.getPlayer());
            data.scriptBlockPos = context.getClickedPos();
            SPacketGuiOpen.sendOpenGui(context.getPlayer(), EnumGuiType.ScriptDoor, null, context.getClickedPos().above());
        	return InteractionResult.SUCCESS;
    	}
    	return res;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity playerIn){
        return stack;
    }
    
//    @Override
//    public int getColorFromItemStack(ItemStack par1ItemStack, int limbSwingAmount){
//		return 0x8B4513;
//    }

}
