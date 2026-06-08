package noppes.npcs.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.CustomBlocks;

public class TileDoor extends TileNpcEntity{
	public int tickCount=0;
	public Block blockModel = CustomBlocks.scripted_door;
	public boolean needsClientUpdate = false;

	public TileDoor(BlockEntityType<?> p_i48289_1_, BlockPos pos, BlockState state) {
		super(p_i48289_1_, pos, state);
	}


	@Override
    public void load(CompoundTag compound){
		super.load(compound);
		setDoorNBT(compound);
    }
	
	public void setDoorNBT(CompoundTag compound){
		blockModel = (Block) BuiltInRegistries.BLOCK.get(new ResourceLocation(compound.getString("ScriptDoorBlockModel")));
		if(blockModel == null || !(blockModel instanceof DoorBlock))
			blockModel = CustomBlocks.scripted_door;
	}
	
	@Override
    public void saveAdditional(CompoundTag compound){
		getDoorNBT(compound);
    	super.saveAdditional(compound);
    }
	
	
	public CompoundTag getDoorNBT(CompoundTag compound){
		compound.putString("ScriptDoorBlockModel", BuiltInRegistries.BLOCK.getKey(blockModel) + "");
		
		return compound;
	}
	
	public void setItemModel(Block block) {
		if(block == null || !(block instanceof DoorBlock))
			block = CustomBlocks.scripted_door;
		if(blockModel == block)
			return;
		blockModel = block;
		needsClientUpdate = true;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TileDoor tile) {
		tile.tickCount++;
		if(tile.tickCount >= 10){
			tile.tickCount = 0;
			if(tile.needsClientUpdate){
				tile.setChanged();
	    		level.setBlockAndUpdate(pos, state);
				tile.needsClientUpdate = false;
			}
		}
	}
//
//	@Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
//		handleUpdateTag(pkt.getTag());
//    }
//
//    @Override
//    public void handleUpdateTag(CompoundTag compound){
//    	setDoorNBT(compound);
//    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
    	return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(){
    	CompoundTag compound = new CompoundTag();
    	compound.putInt("x", this.worldPosition.getX());
    	compound.putInt("y", this.worldPosition.getY());
    	compound.putInt("z", this.worldPosition.getZ());
    	getDoorNBT(compound);
    	return compound;
    }
}
