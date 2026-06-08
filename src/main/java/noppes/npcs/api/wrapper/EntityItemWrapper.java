package noppes.npcs.api.wrapper;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.mixin.ItemEntityMixin;

import java.util.UUID;

public class EntityItemWrapper<T extends ItemEntity> extends EntityWrapper<T> implements IEntityItem {
	
	public EntityItemWrapper(T entity) {
		super(entity);
	}

	@Override
	public String getOwner() {
		if(this.entity.getOwner() == null)
			return null;
		return this.entity.getOwner().toString();
	}

	@Override
	public void setOwner(String name) {
		this.entity.setThrower(UUID.fromString(name));
	}

	@Override
	public int getPickupDelay() {
		return ((ItemEntityMixin)this.entity).pickupDelay();
	}

	@Override
	public void setPickupDelay(int delay) {
		this.entity.setPickUpDelay(delay);
	}

	@Override
	public int getType() {
		return EntitiesType.ITEM;
	}

	@Override
	public long getAge() {
		return this.entity.getAge();
	}

	@Override
	public void setAge(long age) {
		age = Math.max(Math.min(age, Integer.MAX_VALUE), Integer.MIN_VALUE);
		((ItemEntityMixin)this.entity).age((int)age);
	}

	@Override
	public IItemStack getItem() {
		return NpcAPI.Instance().getIItemStack(this.entity.getItem());
	}

	@Override
	public void setItem(IItemStack item) {
		ItemStack stack = item == null? ItemStack.EMPTY:item.getMCItemStack();
		this.entity.setItem(stack);
	}
}
