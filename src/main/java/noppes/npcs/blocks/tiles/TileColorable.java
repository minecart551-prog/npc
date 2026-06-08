package noppes.npcs.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileColorable extends TileNpcEntity {
	
	public int color = 14;
	public int rotation;

    public TileColorable(BlockEntityType<?> p_i48289_1_, BlockPos pos, BlockState state) {
        super(p_i48289_1_, pos, state);
    }

    @Override
    public void load(CompoundTag compound){
        super.load(compound);
        color = compound.getInt("BannerColor");
        rotation = compound.getInt("BannerRotation");
    }

    @Override
    public void saveAdditional(CompoundTag compound){
    	compound.putInt("BannerColor", color);
    	compound.putInt("BannerRotation", rotation);
    	super.saveAdditional(compound);
    }
	
    public boolean canUpdate(){
        return false;
    }

//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
//    	CompoundTag compound = pkt.getTag();
//    	load(compound);
//    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
    	return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(){
    	CompoundTag compound = new CompoundTag();
    	saveAdditional(compound);
    	compound.remove("Items");
    	compound.remove("ExtraData");
    	return compound;
    }
    
//	@Override
//    public AABB getRenderBoundingBox(){
//		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1);
//    }
	
	public int powerProvided(){
		return 0;
	}
}
