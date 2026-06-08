package noppes.npcs.containers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilServer;

public class SlotNpcCrafting extends ResultSlot {

    private final CraftingContainer craftMatrix;

    public SlotNpcCrafting(Player player, CraftingContainer craftingInventory, Container inventory, int slotIndex, int x, int y) {
        super(player, craftingInventory, inventory, slotIndex, x, y);
		this.craftMatrix = craftingInventory;
	}
	
	@Override
    public void onTake(Player player, ItemStack itemStack) {
        this.checkTakeAchievements(itemStack);

        for (int i = 0; i < this.craftMatrix.getContainerSize(); ++i)
        {
            ItemStack itemstack1 = this.craftMatrix.getItem(i);

            if (!NoppesUtilServer.IsItemStackNull(itemstack1))
            {
                this.craftMatrix.removeItem(i, 1);

                if (itemstack1.getItem().hasCraftingRemainingItem())
                {
                    ItemStack itemstack2 = new ItemStack(itemstack1.getItem().getCraftingRemainingItem());

                    if (!NoppesUtilServer.IsItemStackNull(itemstack2) && itemstack2.isDamageableItem() && itemstack2.getDamageValue() > itemstack2.getMaxDamage())
                    {
                        continue;
                    }

                    if (!player.getInventory().add(itemstack2))
                    {
                        if (NoppesUtilServer.IsItemStackNull(this.craftMatrix.getItem(i)))
                        {
                            this.craftMatrix.setItem(i, itemstack2);
                        }
                        else
                        {
                            player.drop(itemstack2, false);
                        }
                    }
                }
            }
        }
    }
}
