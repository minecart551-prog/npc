package noppes.npcs.api.wrapper;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.item.IItemArmor;

public class ItemArmorWrapper extends ItemStackWrapper implements IItemArmor{
	protected ArmorItem armor;
	protected ItemArmorWrapper(ItemStack item) {
		super(item);
		armor = (ArmorItem) item.getItem();
	}

	@Override
	public int getType(){
		return ItemType.ARMOR;
	}

	@Override
	public int getArmorSlot() {
		return armor.getEquipmentSlot().getIndex();
	}

	@Override
	public String getArmorMaterial() {
		return armor.getMaterial().getName();
	}
}
