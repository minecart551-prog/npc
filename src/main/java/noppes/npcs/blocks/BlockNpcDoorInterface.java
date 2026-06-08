package noppes.npcs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import java.util.Collections;
import java.util.List;

public abstract class BlockNpcDoorInterface extends DoorBlock implements EntityBlock {
	
	public BlockNpcDoorInterface(Block.Properties properties) {
		super(properties, BlockSetType.STONE);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
		level.removeBlockEntity(pos);
    }

	@Override
	public List<ItemStack> getDrops(BlockState p_287732_, LootParams.Builder p_287596_) {
		return Collections.emptyList();
	}

	@Override
	public void playerDestroy(Level p_180657_1_, Player p_180657_2_, BlockPos p_180657_3_, BlockState p_180657_4_, BlockEntity p_180657_5_, ItemStack p_180657_6_) {
		p_180657_2_.awardStat(Stats.BLOCK_MINED.get(this));
		p_180657_2_.causeFoodExhaustion(0.005F);
		dropResources(p_180657_4_, p_180657_1_, p_180657_3_, p_180657_5_, p_180657_2_, p_180657_6_);
	}
//	@Override
//	public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
//		BlockState iblockstate1;
//
//		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
//
//			iblockstate1 = worldIn.getBlockState(pos.above());
//
//			if (iblockstate1.getBlock() == this) {
//				state = state.setValue(HINGE, iblockstate1.getValue(HINGE)).with(POWERED, iblockstate1.getValue(POWERED));
//			}
//		}
//		else {
//			iblockstate1 = worldIn.getBlockState(pos.below());
//
//			if (iblockstate1.getBlock() == this) {
//				state = state.setValue(FACING,iblockstate1.getValue(FACING)).with(OPEN, iblockstate1.getValue(OPEN));
//			}
//		}
//
//		return state;
//	}
}
