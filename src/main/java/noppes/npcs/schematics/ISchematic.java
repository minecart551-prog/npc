package noppes.npcs.schematics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface ISchematic {

	public short getWidth();

	public short getHeight();

	public short getLength();

	public int getBlockEntityDimensions();
	
	public CompoundTag getBlockEntity(int i);

	public String getName();

	public BlockState getBlockState(int x, int y, int z);

	public BlockState getBlockState(int i);
	
	public CompoundTag getNBT();
}
