package noppes.npcs.roles;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.INPCJob;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.EnumSet;



public abstract class JobInterface implements INPCJob{
	public static final JobInterface NONE = new JobInterface(null) {
		@Override
		public CompoundTag save(CompoundTag compound) {
			return null;
		}

		@Override
		public void load(CompoundTag compound) {

		}

		@Override
		public int getType() {
			return JobType.NONE;
		}
	};

	public EntityNPCInterface npc;
	
	public boolean overrideMainHand = false;
	public boolean overrideOffHand = false;
	
	public JobInterface(EntityNPCInterface npc){
		this.npc = npc;
	}
	public abstract CompoundTag save(CompoundTag compound);
	public abstract void load(CompoundTag compound);
	public void killed(){};
	public void delete(){};
	
	public boolean aiShouldExecute() {
		return false;
	}
	
	public boolean aiContinueExecute() {
		return aiShouldExecute();
	}
	public void aiStartExecuting() {}
	public void aiUpdateTask() {}
	public void reset() {}
	public void stop() {}
	
	public IItemStack getMainhand(){
		return null;
	}
	
	public IItemStack getOffhand(){
		return null;
	}
	
	public boolean isFollowing() {
		return false;
	}
	
	public EnumSet<Goal.Flag> getFlags() {
		return EnumSet.noneOf(Goal.Flag.class);
	}
	
	public ItemStack stringToItem(String s){
		if(s.isEmpty())
			return ItemStack.EMPTY;
		return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(s)));
	}
	
	public String itemToString(ItemStack item){
		if(item == null || item.isEmpty())
			return "";
		return BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
	}
}
