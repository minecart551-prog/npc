package noppes.npcs.api.wrapper;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;

public class ContainerWrapper implements IContainer{
	private Container inventory;
	private AbstractContainerMenu container;

	public ContainerWrapper(Container inventory){
		this.inventory = inventory;
	}
	public ContainerWrapper(AbstractContainerMenu container){
		this.container = container;
	}
	
	@Override
	public int getSize() {
		if(inventory != null)
			return inventory.getContainerSize();
		return container.slots.size();
	}

	@Override
	public IItemStack getSlot(int slot) {
		if(slot < 0 || slot >= getSize())
			throw new CustomNPCsException("Slot is out of range " + slot);
		if(inventory != null)
			return NpcAPI.Instance().getIItemStack(inventory.getItem(slot));
		return NpcAPI.Instance().getIItemStack(container.getSlot(slot).getItem());
	}

	@Override
	public void setSlot(int slot, IItemStack item) {
		if(slot < 0 || slot >= getSize())
			throw new CustomNPCsException("Slot is out of range " + slot);
		ItemStack itemstack = item == null?ItemStack.EMPTY:item.getMCItemStack();
		if(inventory != null)
			inventory.setItem(slot, itemstack);
		else {
			container.setItem(slot, container.getStateId(), itemstack);
			container.broadcastChanges();
		}
	}
	
	@Override
	public int count(IItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
		int count = 0;
		for(int i = 0; i < getSize(); i++) {
			IItemStack toCompare = getSlot(i);
			if(NoppesUtilPlayer.compareItems(item.getMCItemStack(), toCompare.getMCItemStack(), ignoreDamage, ignoreNBT)) {
				count += toCompare.getStackSize();
			}
		}
		return count;
	}

	@Override
	public Container getMCInventory() {
		return inventory;
	}

	@Override
	public AbstractContainerMenu getMCContainer() {
		return container;
	}
	@Override
	public IItemStack[] getItems() {
		IItemStack[] items = new IItemStack[getSize()];
		for(int i = 0; i < getSize(); i++){
			items[i] = getSlot(i);
		}
		return items;
	}

}
