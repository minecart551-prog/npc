package noppes.npcs.containers;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilServer;

public class InventoryNPC implements Container {
    private String inventoryTitle;
    private int slotsCount;
    public final NonNullList<ItemStack> inventoryContents;
    private AbstractContainerMenu con;

    public InventoryNPC(String s, int i, AbstractContainerMenu con){
    	this.con = con;
        inventoryTitle = s;
        slotsCount = i;
        inventoryContents = NonNullList.<ItemStack>withSize(i, ItemStack.EMPTY);
    }
    
    @Override
    public ItemStack getItem(int i){
        return inventoryContents.get(i);
    }

    @Override
    public ItemStack removeItem(int index, int count){
        return ContainerHelper.removeItem(inventoryContents, index, count);
    }

    @Override
    public void setItem(int index, ItemStack stack){
        this.inventoryContents.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize())
        {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int getContainerSize(){
        return slotsCount;
    }
    
    @Override
    public int getMaxStackSize(){
        return 64;
    }

    @Override
    public boolean stillValid(Player entityplayer){
        return false;
    }


	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(inventoryContents, i);
	}
	
	@Override
	public boolean canPlaceItem(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public void setChanged() {
    	con.slotsChanged(this);
	}

	@Override
	public void startOpen(Player player) {
		
	}
	@Override
	public void stopOpen(Player player) {
		
	}

	@Override
    public boolean isEmpty(){
        for (int slot = 0; slot < this.getContainerSize(); slot++){
        	ItemStack item = getItem(slot);
            if (!NoppesUtilServer.IsItemStackNull(item) && !item.isEmpty()){
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearContent() {

    }
}
