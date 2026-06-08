package noppes.npcs.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.CustomBlocks;

public class TileBlockAnvil extends TileNpcEntity {

	public TileBlockAnvil(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_anvil, pos, state);
	}
	
    public boolean canUpdate(){
        return false;
    }

//	@Override
//	public void updateEntity(){
//	
//	}
	
//	@Override
//    public void load(CompoundTag par1CompoundTag)
//    {
//    	super.load(par1CompoundTag);
//    }
//
//	@Override
//    public void save(CompoundTag par1CompoundTag)
//    {
//    	super.save(par1CompoundTag);
//    }
}
