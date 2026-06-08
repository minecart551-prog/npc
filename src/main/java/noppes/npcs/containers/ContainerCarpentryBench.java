package noppes.npcs.containers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomContainer;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;

public class ContainerCarpentryBench extends AbstractContainerMenu {
    public CraftingContainer craftMatrix = new TransientCraftingContainer(this, 4, 4);
    public Container craftResult = new ResultContainer();
    private Player player;
    private BlockPos pos;

    public ContainerCarpentryBench(int id, Inventory par1PlayerInventory, BlockPos pos){
        super(CustomContainer.container_carpentrybench, id);
        this.pos = pos;
        this.player = par1PlayerInventory.player;
        this.addSlot(new SlotNpcCrafting(par1PlayerInventory.player, this.craftMatrix, this.craftResult, 0, 133, 41));
        int var6;
        int var7;

        for (var6 = 0; var6 < 4; ++var6)
        {
            for (var7 = 0; var7 < 4; ++var7)
            {
                this.addSlot(new Slot(this.craftMatrix, var7 + var6 * 4, 17 + var7 * 18, 14 + var6 * 18));
            }
        }

        for (var6 = 0; var6 < 3; ++var6)
        {
            for (var7 = 0; var7 < 9; ++var7)
            {
                this.addSlot(new Slot(par1PlayerInventory, var7 + var6 * 9 + 9, 8 + var7 * 18, 98 + var6 * 18));
            }
        }

        for (var6 = 0; var6 < 9; ++var6)
        {
            this.addSlot(new Slot(par1PlayerInventory, var6, 8 + var6 * 18, 156));
        }

        this.slotsChanged(this.craftMatrix);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    @Override
    public void slotsChanged(Container par1Container)
    {
    	if(!player.level().isClientSide){
    		RecipeCarpentry recipe = RecipeController.instance.findMatchingRecipe(this.craftMatrix);
    		
    		ItemStack item = ItemStack.EMPTY;
    		if(recipe != null && recipe.availability.isAvailable(player)){
    			item = recipe.assemble(this.craftMatrix, player.level().registryAccess());
    		}
    		
    		this.craftResult.setItem(0, item);
    		ServerPlayer plmp = (ServerPlayer) player;
    		plmp.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, item));
    	}
    }

    @Override
    public void removed(Player par1Player){
        super.removed(par1Player);

        if (!par1Player.level().isClientSide)
        {
            for (int var2 = 0; var2 < 16; ++var2)
            {
                ItemStack var3 = this.craftMatrix.removeItemNoUpdate(var2);

                if (var3 != null)
                {
                    par1Player.drop(var3, false);
                }
            }
        }
    }
    
    @Override
    public boolean stillValid(Player par1Player){
        return par1Player.level().getBlockState(pos).getBlock() == CustomBlocks.carpenty && par1Player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    @Override
    public ItemStack quickMoveStack(Player par1Player, int par1) {
        ItemStack var2 = ItemStack.EMPTY;
        Slot var3 = this.slots.get(par1);

        if (var3 != null && var3.hasItem())
        {
            ItemStack var4 = var3.getItem();
            var2 = var4.copy();

            if (par1 == 0)
            {
                if (!this.moveItemStackTo(var4, 17, 53, true))
                {
                    return ItemStack.EMPTY;
                }

                var3.onQuickCraft(var4, var2);
            }
            else if (par1 >= 17 && par1 < 44)
            {
                if (!this.moveItemStackTo(var4, 44, 53, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (par1 >= 44 && par1 < 53)
            {
                if (!this.moveItemStackTo(var4, 17, 44, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(var4, 17, 53, false))
            {
                return ItemStack.EMPTY;
            }

            if (var4.getCount() == 0)
            {
                var3.set(ItemStack.EMPTY);
            }
            else
            {
                var3.setChanged();
            }

            if (var4.getCount() == var2.getCount())
            {
                return ItemStack.EMPTY;
            }

            var3.onTake(par1Player, var4);
        }

        return var2;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        return slotIn.container != this.craftResult && super.canTakeItemForPickAll(stack, slotIn);
    }
}
