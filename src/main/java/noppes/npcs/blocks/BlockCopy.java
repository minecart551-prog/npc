package noppes.npcs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.packets.server.SPacketGuiOpen;

public class BlockCopy extends BlockInterface{
	public BlockCopy() {
        super(Block.Properties.copy(Blocks.BARRIER).sound(SoundType.STONE));
	}

    @Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
    	if(level.isClientSide)
    		return InteractionResult.PASS;
    	
		ItemStack currentItem = player.getInventory().getSelected();
		if (currentItem != null	&& currentItem.getItem() == CustomItems.wand) {
			SPacketGuiOpen.sendOpenGui(player, EnumGuiType.CopyBlock, null, pos);
		}
		return InteractionResult.SUCCESS;
    }

    @Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
    	if(!context.getLevel().isClientSide){
			SPacketGuiOpen.sendOpenGui(context.getPlayer(), EnumGuiType.CopyBlock, null, context.getClickedPos());
    	}
    	return defaultBlockState();
    }
    
//    @Override
//    public boolean isFullCube(BlockState state){
//        return false;
//    }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileCopy(pos, state);
	}

}
