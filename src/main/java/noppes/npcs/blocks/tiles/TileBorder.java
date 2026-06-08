package noppes.npcs.blocks.tiles;

import com.google.common.base.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomBlocks;
import noppes.npcs.blocks.BlockBorder;
import noppes.npcs.controllers.data.Availability;

import java.util.List;

public class TileBorder extends TileNpcEntity implements Predicate {
	public Availability availability = new Availability();
	public AABB boundingbox;
	public int rotation = 0;
	public int height = 10;
	public String message = "availability.areaNotAvailble";

	public TileBorder(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_border, pos, state);
	}

    @Override
    public void load(CompoundTag compound){
		super.load(compound);
        readExtraNBT(compound);
        if(getLevel() != null)
			getLevel().setBlockAndUpdate(this.getBlockPos(), CustomBlocks.border.defaultBlockState().setValue(BlockBorder.ROTATION, rotation));
    }
    
    public void readExtraNBT(CompoundTag compound){
        availability.load(compound.getCompound("BorderAvailability"));
        rotation = compound.getInt("BorderRotation");
        height = compound.getInt("BorderHeight");
        message = compound.getString("BorderMessage");
    }

    @Override
    public void saveAdditional(CompoundTag compound){
    	writeExtraNBT(compound);
    	super.saveAdditional(compound);
    }
    
    public void writeExtraNBT(CompoundTag compound){
    	compound.put("BorderAvailability", availability.save(new CompoundTag()));
    	compound.putInt("BorderRotation", rotation);
    	compound.putInt("BorderHeight", height);
    	compound.putString("BorderMessage", message);
    }
    
	public static void tick(Level level, BlockPos pos, BlockState state, TileBorder tile) {
    	if(level.isClientSide)
    		return;
    	AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + tile.height + 1, pos.getZ() + 1);
    	List<Entity> list = level.getEntitiesOfClass(Entity.class, box, tile);
    	for(Entity entity : list){
    		if(entity instanceof ThrownEnderpearl){
				ThrownEnderpearl pearl = (ThrownEnderpearl) entity;
    			if(pearl.getOwner() instanceof Player && !tile.availability.isAvailable((Player)pearl.getOwner()))
    				entity.setRemoved(Entity.RemovalReason.DISCARDED);
    			continue;
    		}
    		Player player = (Player) entity;
    		if(tile.availability.isAvailable(player))
    			continue;
    		BlockPos pos2 = new BlockPos(tile.worldPosition);
    		if(tile.rotation == 2){
    			pos2 = pos2.south();
    		}
    		else if(tile.rotation == 0){
    			pos2 = pos2.north();
    		}
    		else if(tile.rotation == 1){
    			pos2 = pos2.east();
    		}
    		else if(tile.rotation == 3){
    			pos2 = pos2.west();
    		}
    		while(!level.isEmptyBlock(pos2)){
    			pos2 = pos2.above();
    		}
    		player.teleportTo(pos2.getX() + 0.5, pos2.getY(), pos2.getZ() + 0.5);
    		if(!tile.message.isEmpty())
    			player.displayClientMessage(Component.translatable(tile.message), true);
    	}
    }

//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
//    	handleUpdateTag(pkt.getTag());
//    }
//
//    @Override
//    public void handleUpdateTag(CompoundTag compound){
//    	rotation = compound.getInt("Rotation");
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
    	compound.putInt("Rotation", rotation);
    	return compound;
    }

	public boolean isEntityApplicable(Entity var1) {
		return var1 instanceof ServerPlayer || var1 instanceof ThrownEnderpearl;
	}

	@Override
	public boolean apply(Object ob) {
		return isEntityApplicable((Entity) ob);
	}
}
