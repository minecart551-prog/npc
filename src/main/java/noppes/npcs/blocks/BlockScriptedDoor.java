package noppes.npcs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.packets.server.SPacketGuiOpen;
import org.jetbrains.annotations.Nullable;

public class BlockScriptedDoor extends BlockNpcDoorInterface{
	//public static final IntegerProperty MODEL = IntegerProperty.create("model", 0, 6);
	
	public BlockScriptedDoor() {
		super(Block.Properties.copy(Blocks.IRON_DOOR).strength(5.0F, 10));
	}


    @Override
    public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(CustomBlocks.scripted_door_item);
    }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileScriptedDoor(pos, state);
	}

	@Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.INVISIBLE;
    }
	
	@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
		if(level.isClientSide)
            return InteractionResult.SUCCESS;
		BlockPos blockpos1 = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState iblockstate1 = pos.equals(blockpos1) ? state : level.getBlockState(blockpos1);

        if (iblockstate1.getBlock() != this){
            return InteractionResult.FAIL;
        }
    	ItemStack currentItem = player.getInventory().getSelected();
    	
		if (currentItem != null	&& (currentItem.getItem() == CustomItems.wand || currentItem.getItem() == CustomItems.scripter || currentItem.getItem() == CustomBlocks.scripted_door_item)) {
            PlayerData data = PlayerData.get(player);
            data.scriptBlockPos = blockpos1;
		    SPacketGuiOpen.sendOpenGui(player, EnumGuiType.ScriptDoor, null, blockpos1);
            return InteractionResult.SUCCESS;
		}

    	TileScriptedDoor tile = (TileScriptedDoor) level.getBlockEntity(blockpos1);
        Vec3 vec = ray.getLocation();
        float x = (float)(vec.x - (double)pos.getX());
        float y = (float)(vec.y - (double)pos.getY());
        float z = (float)(vec.z - (double)pos.getZ());
    	if(EventHooks.onScriptBlockInteract(tile, player, ray.getDirection().get3DDataValue(), x, y, z))
            return InteractionResult.FAIL;
    	
    	setOpen(player, level, iblockstate1, blockpos1, iblockstate1.getValue(DoorBlock.OPEN).equals(false));
        return InteractionResult.SUCCESS;
	}

	@Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block neighborBlock, BlockPos pos2, boolean isMoving) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos blockpos1 = pos.below();
            BlockState iblockstate1 = worldIn.getBlockState(blockpos1);

            if (iblockstate1.getBlock() != this)
            {
                worldIn.removeBlock(pos, false);
            }
            else if (neighborBlock != this)
            {
                this.neighborChanged(iblockstate1, worldIn, blockpos1, neighborBlock, blockpos1, isMoving);
            }
        }
        else
        {
            BlockPos blockpos2 = pos.above();
            BlockState iblockstate2 = worldIn.getBlockState(blockpos2);

            if (iblockstate2.getBlock() != this)
            {
                worldIn.removeBlock(pos, false);
            }
            else
            {
            	TileScriptedDoor tile = (TileScriptedDoor) worldIn.getBlockEntity(pos);
            	if(!worldIn.isClientSide)
            		EventHooks.onScriptBlockNeighborChanged(tile, pos2);
            	
                boolean flag = worldIn.hasNeighborSignal(pos) || worldIn.hasNeighborSignal(blockpos2);

                if ((flag || neighborBlock.defaultBlockState().isSignalSource()) && neighborBlock != this && flag != ((Boolean)iblockstate2.getValue(POWERED)).booleanValue())
                {
                    worldIn.setBlock(blockpos2, iblockstate2.setValue(POWERED, Boolean.valueOf(flag)), 2);

                    if (flag != ((Boolean)state.getValue(OPEN)).booleanValue()){
                    	setOpen(null, worldIn, state, pos, flag);
                    }
                }
                
            	int power = 0;
                for (Direction enumfacing : Direction.values()){
                	int p = worldIn.getSignal(pos.relative(enumfacing), enumfacing);
                	if(p > power)
                		power = p;
                }
            	tile.newPower = power;
            }
        }
    }
	
	@Override
	public void setOpen(Entity entity, Level worldIn, BlockState state, BlockPos pos, boolean open){
		TileScriptedDoor tile = (TileScriptedDoor) worldIn.getBlockEntity(pos);
		
		if(EventHooks.onScriptBlockDoorToggle(tile))
			return;
    	super.setOpen(entity, worldIn, state, pos, open);
        //worldIn.setBlockEntity(pos, tile);
	}
	
	@Override    
    public void attack(BlockState state, Level level, BlockPos pos, Player playerIn) {
		if(level.isClientSide)
			return;
		BlockPos blockpos1 = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState iblockstate1 = pos.equals(blockpos1) ? state : level.getBlockState(blockpos1);
        if (iblockstate1.getBlock() != this) {
            return;
        }
        TileScriptedDoor tile = (TileScriptedDoor) level.getBlockEntity(blockpos1);
        EventHooks.onScriptBlockClicked(tile, playerIn);
		
    }
	
	@Override 
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() == newState.getBlock()){
            return;
        }

		BlockPos blockpos1 = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState iblockstate1 = pos.equals(blockpos1) ? state : level.getBlockState(blockpos1);


        if(!level.isClientSide && iblockstate1.getBlock() == this){
        	TileScriptedDoor tile = (TileScriptedDoor) level.getBlockEntity(pos);
        	EventHooks.onScriptBlockBreak(tile);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(!level.isClientSide){
            TileScriptedDoor tile = (TileScriptedDoor) level.getBlockEntity(pos);
            if(!EventHooks.onScriptBlockHarvest(tile, player))
                super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }
    }

    @Override    
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
		if(level.isClientSide)
			return;
		TileScriptedDoor tile = (TileScriptedDoor) level.getBlockEntity(pos);
    	EventHooks.onScriptBlockCollide(tile, entityIn);
    }

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player){
		BlockPos blockpos1 = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
		BlockState iblockstate1 = pos.equals(blockpos1) ? state : level.getBlockState(blockpos1);
        if (player.getAbilities().instabuild && iblockstate1.getValue(HALF) == DoubleBlockHalf.LOWER && iblockstate1.getBlock() == this){
            level.removeBlock(blockpos1, false);
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float f = ((TileScriptedDoor) level.getBlockEntity(pos)).blockHardness;
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = player.hasCorrectToolForDrops(state) ? 30 : 100;
            return player.getDestroySpeed(state) / f / (float)i;
        }
    }

//    @Override
//    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion){
//        return ((TileScriptedDoor) level.getBlockEntity(pos)).blockResistance;
//    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, CustomBlocks.tile_scripteddoor, TileScriptedDoor::tick);
    }
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
    }
}
