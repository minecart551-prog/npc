package noppes.npcs;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;

public class NpcMiscInventory extends SimpleContainer {
    public final NonNullList<ItemStack> items;
	public int stackLimit = 64;

	private int size;
	
	public NpcMiscInventory(int size){
		this.size = size;
        items = NonNullList.<ItemStack>withSize(size, ItemStack.EMPTY);
	}
	
	public CompoundTag getToNBT(){
		CompoundTag nbttagcompound = new CompoundTag();
		nbttagcompound.put("NpcMiscInv", NBTTags.nbtItemStackList(items));
		return nbttagcompound;
	}
	public void setFromNBT(CompoundTag nbttagcompound){
		NBTTags.getItemStackList(nbttagcompound.getList("NpcMiscInv", 10), items);
	}
	@Override
	public int getContainerSize() {
		return size;
	}

	@Override
	public ItemStack getItem(int index) {
		return items.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(items, index, count);
	}


	public boolean removeItem(ItemStack eating, int decrease) {
		for(int slot = 0; slot < items.size(); slot++){
			ItemStack item = items.get(slot);
			if(!item.isEmpty() && eating == item && item.getCount() >= decrease){
				item.split(decrease);
				if(item.getCount() <= 0)
					items.set(slot, ItemStack.EMPTY);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int var1) {
        return items.set(var1, ItemStack.EMPTY);
	}

	@Override
	public void setItem(int var1, ItemStack var2) {
		if(var1 >= getContainerSize())
			return;
		items.set(var1, var2);
	}

	@Override
	public int getMaxStackSize() {
		return stackLimit;
	}


	@Override
	public boolean stillValid(Player var1) {
		return true;
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemstack) {
		return true;
	}
	
	@Override
	public void setChanged() {
		
	}
	public boolean addItemStack(ItemStack item) {
		ItemStack mergable;
		boolean merged = false;
		while(!(mergable = getMergableItem(item)).isEmpty() && mergable.getCount() > 0){
			int size = mergable.getMaxStackSize() - mergable.getCount();
			if(size > item.getCount()){
				mergable.setCount(mergable.getMaxStackSize());
				item.setCount(item.getCount() - size);
				merged = true;
			}
			else{
				mergable.setCount(mergable.getCount() + item.getCount());
				item.setCount(0);
			}
		}
		if(item.getCount() <= 0)
			return true;
		int slot = firstFreeSlot();
		if(slot >= 0){
			items.set(slot, item.copy());
			item.setCount(0);
			return true;
		}
		return merged;
	}
	
	public ItemStack getMergableItem(ItemStack item){
		for(ItemStack is : items){
			if(NoppesUtilPlayer.compareItems(item, is, false, false) && is.getCount() < is.getMaxStackSize()){
				return is;
			}
		}
		return ItemStack.EMPTY;
	}
	
	public int firstFreeSlot(){
		for(int i = 0; i < getContainerSize(); i++){
			if(items.get(i).isEmpty())
				return i;
		}
		return -1;
	}

	public void setSize(int i) {
		size = i;
	}

	@Override
	public void startOpen(Player player) {
		
	}

	@Override
	public void stopOpen(Player player) {
		
	}

	@Override
	public void clearContent() {
		
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
}
