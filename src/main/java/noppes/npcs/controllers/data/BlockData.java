package noppes.npcs.controllers.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class BlockData{
	public BlockPos pos;
	public BlockState state;
	public CompoundTag tile;
	
	private ItemStack stack;
	
	public BlockData(BlockPos pos, BlockState state, CompoundTag tile){
		this.pos = pos;
		this.state = state;
		this.tile = tile;
	}

	public CompoundTag getNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putInt("BuildX", pos.getX());
		compound.putInt("BuildY", pos.getY());
		compound.putInt("BuildZ", pos.getZ());
		compound.putString("Block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
		if(tile != null)
			compound.put("Tile", tile);
		return compound;
	}
	
	public static BlockData getData(CompoundTag compound){
		BlockPos pos = new BlockPos(compound.getInt("BuildX"), compound.getInt("BuildY"), compound.getInt("BuildZ"));
		Block b = BuiltInRegistries.BLOCK.get(new ResourceLocation(compound.getString("Block")));
		if(b == null)
			return null;
		CompoundTag tile = null;
		if(compound.contains("Tile"))
			tile = compound.getCompound("Tile");
		return new BlockData(pos, b.defaultBlockState(), tile);
		
	}
	
	public ItemStack getStack(){
		if(stack == null)
			stack = new ItemStack(state.getBlock(), 1);
		return stack;
	}
}
