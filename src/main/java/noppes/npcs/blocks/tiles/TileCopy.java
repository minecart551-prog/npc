package noppes.npcs.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomBlocks;

public class TileCopy extends BlockEntity {

	public short length = 10;
	public short width = 10;
	public short height = 10;
	
	public String name = "";

	public TileCopy(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_copy, pos, state);
	}

	@Override
    public void load(CompoundTag compound){
		super.load(compound);

    	length = compound.getShort("Length");
    	width = compound.getShort("Width");
    	height = compound.getShort("Height");
    	
    	name = compound.getString("Name");
    }
    

    @Override
    public void saveAdditional(CompoundTag compound){
    	compound.putShort("Length", length);
    	compound.putShort("Width", width);
    	compound.putShort("Height", height);
    	
    	compound.putString("Name", name);
    	super.saveAdditional(compound);
    }
    
//    @Override
//    public void handleUpdateTag(CompoundTag compound){
//    	length = compound.getShort("Length");
//    	width = compound.getShort("Width");
//    	height = compound.getShort("Height");
//    }
//
//	@Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
//		handleUpdateTag(pkt.getTag());
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
    	compound.putShort("Length", length);
    	compound.putShort("Width", width);
    	compound.putShort("Height", height);
    	return compound;
    }

//	@Override
//    public AABB getRenderBoundingBox(){
//		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + width + 1, worldPosition.getY() + height + 1, worldPosition.getZ() + length + 1);
//    }
}
