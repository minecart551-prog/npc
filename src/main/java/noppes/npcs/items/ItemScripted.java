package noppes.npcs.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import noppes.npcs.CustomTabs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;

public class ItemScripted extends Item {
	
	public ItemScripted(Item.Properties props){
		super(props);
	}
	
	public static ItemScriptedWrapper GetWrapper(ItemStack stack) {
		return (ItemScriptedWrapper) NpcAPI.Instance().getIItemStack(stack);
	}

	@Override
    public boolean isBarVisible(ItemStack stack){
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if(istack instanceof ItemScriptedWrapper) {
			return ((ItemScriptedWrapper) istack).durabilityShow;
		}
        return super.isBarVisible(stack);
    }

	@Override
    public int getBarWidth(ItemStack stack){
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if(istack instanceof ItemScriptedWrapper) {
			return Math.round(13.0F - ((ItemScriptedWrapper) istack).durabilityValue * 13.0F);
		}
        return super.getBarWidth(stack);
    }

	@Override
    public int getBarColor(ItemStack stack){
		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
		if(!(istack instanceof ItemScriptedWrapper)) {
			return super.getBarColor(stack);
		}
		int color = ((ItemScriptedWrapper)istack).durabilityColor;
		if(color >= 0)
			return color;
        return Mth.hsvToRgb(Math.max(0.0F, (1.0F - getBarWidth(stack))) / 3.0F, 1.0F, 1.0F);
    }

//	@Override
//    public int getMaxStackSize(ItemStack stack){ TODO FABRIC
//		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
//		if(istack instanceof ItemScriptedWrapper)
//			return ((ItemScriptedWrapper)istack).getMaxStackSize();
//        return super.getMaxStackSize(stack);
//    }

	@Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker){
        return true;
    }

	@Override
	public boolean shouldOverrideMultiplayerNbt() {
		return true;
	}

//	@Override TODO FABRIC
//	public CompoundTag getShareTag(ItemStack stack) {
//		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
//		if(istack instanceof ItemScriptedWrapper)
//			return ((ItemScriptedWrapper)istack).getMCNbt();
//		return null;
//	}
//
//	@Override
//	public void readShareTag(ItemStack stack, CompoundTag nbt) {
//		if(nbt == null) {
//			return;
//		}
//		IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
//		if(istack instanceof ItemScriptedWrapper)
//			((ItemScriptedWrapper)istack).setMCNbt(nbt);
//	}
}
