package noppes.npcs;

import net.minecraft.nbt.CompoundTag;



public interface ICompatibilty {
	public int getVersion();
	public void setVersion(int version);
	public CompoundTag save(CompoundTag compound);
}
