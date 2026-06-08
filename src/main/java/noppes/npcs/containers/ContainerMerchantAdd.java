package noppes.npcs.containers;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomContainer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.ServerEventsHandler;

public class ContainerMerchantAdd extends ContainerNpcInterface{
    private Merchant theMerchant;
    private SimpleContainer merchantInventory;

    private final Level level;

    public ContainerMerchantAdd(int containerId, Inventory playerInventory)
    {
    	super(CustomContainer.container_merchantadd, containerId, playerInventory);
        this.theMerchant = ServerEventsHandler.Merchant;
        this.level = playerInventory.player.level();
        this.merchantInventory = new SimpleContainer(3);
        this.addSlot(new Slot(this.merchantInventory, 0, 36, 53));
        this.addSlot(new Slot(this.merchantInventory, 1, 62, 53));
        this.addSlot(new Slot(this.merchantInventory, 2, 120, 53));
        int i;

        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
//
//    @Override
//    public void broadcastChanges()
//    {
//        super.broadcastChanges();
//    }
//
//    @Override
//    public void slotsChanged(Container par1Container)
//    {
//        //this.merchantInventory.resetRecipeAndSlots();
//        super.slotsChanged(par1Container);
//    }
//
//
//    public void setCurrentRecipeIndex(int par1)
//    {
//        //this.merchantInventory.setCurrentRecipeIndex(par1);
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void updateProgressBar(int par1, int limbSwingAmount) {}


    @Override
    public ItemStack quickMoveStack(Player par1Player, int limbSwingAmount)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(limbSwingAmount);

        if (slot != null && slot.hasItem())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (limbSwingAmount != 0 && limbSwingAmount != 1 && limbSwingAmount != 2)
            {
                if (limbSwingAmount >= 3 && limbSwingAmount < 30)
                {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (limbSwingAmount >= 30 && limbSwingAmount < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemstack1, 3, 39, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0)
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(par1Player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public void removed(Player par1Player)
    {
        super.removed(par1Player);
        //this.theMerchant.setCustomer((Player)null);
        super.removed(par1Player);

        if (!this.level.isClientSide)
        {
            ItemStack itemstack = this.merchantInventory.removeItemNoUpdate(0);

            if (!NoppesUtilServer.IsItemStackNull(itemstack))
            {
                par1Player.drop(itemstack, false);
            }

            itemstack = this.merchantInventory.removeItemNoUpdate(1);

            if (!NoppesUtilServer.IsItemStackNull(itemstack))
            {
                par1Player.drop(itemstack, false);
            }
        }
    }
}
