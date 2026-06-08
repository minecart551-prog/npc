package noppes.npcs.containers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.roles.RoleCompanion;

public class SlotCompanionArmor extends Slot{
	public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};


	final EquipmentSlot armorType;
    final RoleCompanion role;

    public SlotCompanionArmor(RoleCompanion role, Container iinventory, int id, int x, int y, EquipmentSlot type){
        super(iinventory, id, x, y);
        armorType = type;
        this.role = role;
    }

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon(){
        return Pair.of(InventoryMenu.BLOCK_ATLAS, SlotCompanionArmor.ARMOR_SLOT_TEXTURES[armorType.getIndex()]);
    }

	@Override
	public boolean mayPlace(ItemStack itemstack) {
		if (itemstack.getItem() instanceof ArmorItem && role.canWearArmor(itemstack))
			return ((ArmorItem)itemstack.getItem()).getEquipmentSlot() == armorType;
		
		if (itemstack.getItem() instanceof BlockItem)
			return armorType == EquipmentSlot.HEAD;
		
		return false;
	}
}
