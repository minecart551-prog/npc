package noppes.npcs.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.controllers.data.Availability;

import java.util.List;

public class TileRedstoneBlock extends TileNpcEntity {
	public int onRange = 12;
	public int offRange = 20;

	public int onRangeX = 12;
	public int onRangeY = 12;
	public int onRangeZ = 12;

	public int offRangeX = 20;
	public int offRangeY = 20;
	public int offRangeZ = 20;
	
	public boolean isDetailed = false;

	public Availability availability = new Availability();
	
	public boolean isActivated = false;
	
	private int ticks = 10;

	public TileRedstoneBlock(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_redstoneblock, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TileRedstoneBlock tile) {
		if(tile.level.isClientSide)
			return;
		tile.ticks--;
		if(tile.ticks > 0)
			return;
		tile.ticks = tile.onRange > 10? 20 : 10;
		Block block = state.getBlock();
		if(block == null || block instanceof BlockNpcRedstone == false){
			return;
		}

		if(CustomNpcs.FreezeNPCs){
			if(tile.isActivated)
				tile.setActive(block,false);
			return;
		}
		if(!tile.isActivated){
			int x = tile.isDetailed?tile.onRangeX:tile.onRange;
			int y = tile.isDetailed?tile.onRangeY:tile.onRange;
			int z = tile.isDetailed?tile.onRangeZ:tile.onRange;
			List<Player> list = tile.getPlayerList(x,y,z);
			if(list.isEmpty())
				return;
			for(Player player : list){
				if(tile.availability.isAvailable(player)){
					tile.setActive(block,true);
					return;
				}
			}
		}
		else{
			int x = tile.isDetailed?tile.offRangeX:tile.offRange;
			int y = tile.isDetailed?tile.offRangeY:tile.offRange;
			int z = tile.isDetailed?tile.offRangeZ:tile.offRange;
			List<Player> list = tile.getPlayerList(x,y,z);
			for(Player player : list){
				if(tile.availability.isAvailable(player))
					return;
			}
			tile.setActive(block,false);
		}
	
	}

	private void setActive(Block block, boolean bo){
		isActivated = bo;
		BlockState state = block.defaultBlockState().setValue(BlockNpcRedstone.ACTIVE, isActivated);
		level.setBlock(worldPosition, state, 2);
		setChanged();
		level.sendBlockUpdated(worldPosition, state, state, 3);
		block.onPlace(state, level, worldPosition, state, false);
	}
	
	private List<Player> getPlayerList(int x, int y, int z){
		return level.getEntitiesOfClass(Player.class, new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1).inflate(x, y, z));
	}
	
	@Override
    public void load(CompoundTag compound){
		super.load(compound);
    	onRange = compound.getInt("BlockOnRange");
    	offRange = compound.getInt("BlockOffRange");
    	
    	isDetailed = compound.getBoolean("BlockIsDetailed");
    	if(compound.contains("BlockOnRangeX")){
    		isDetailed = true;
    		onRangeX = compound.getInt("BlockOnRangeX");
    		onRangeY = compound.getInt("BlockOnRangeY");
    		onRangeZ = compound.getInt("BlockOnRangeZ");

    		offRangeX = compound.getInt("BlockOffRangeX");
    		offRangeY = compound.getInt("BlockOffRangeY");
    		offRangeZ = compound.getInt("BlockOffRangeZ");
    	}

    	if(compound.contains("BlockActivated"))
    		isActivated = compound.getBoolean("BlockActivated");
    	
    	availability.load(compound);
    }

	@Override
    public void saveAdditional(CompoundTag compound){
    	compound.putInt("BlockOnRange", onRange);
    	compound.putInt("BlockOffRange", offRange);
    	compound.putBoolean("BlockActivated", isActivated);
    	compound.putBoolean("BlockIsDetailed", isDetailed);

    	if(isDetailed){
	    	compound.putInt("BlockOnRangeX", onRangeX);
	    	compound.putInt("BlockOnRangeY", onRangeY);
	    	compound.putInt("BlockOnRangeZ", onRangeZ);

	    	compound.putInt("BlockOffRangeX", offRangeX);
	    	compound.putInt("BlockOffRangeY", offRangeY);
	    	compound.putInt("BlockOffRangeZ", offRangeZ);
    	}
    	
    	
    	availability.save(compound);
    	super.saveAdditional(compound);
    }
}
