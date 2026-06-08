package noppes.npcs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.packets.server.SPacketGuiOpen;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class BlockScripted extends BlockInterface {
    public static final VoxelShape AABB = Shapes.create(new AABB(0.001f, 0.001f, 0.001f, 0.998f, 0.998f, 0.998f));

	public BlockScripted() {
		super(Block.Properties.copy(Blocks.STONE).sound(SoundType.STONE).strength(5.0F, 10));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileScripted(pos, state);
	}

    @Override 
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
    	return AABB;
    }

    @Override 
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context){
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	if(tile != null && tile.isPassible)
    		return Shapes.empty();
        return AABB;
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
    	if(level.isClientSide)
    		return InteractionResult.SUCCESS;
		ItemStack currentItem = player.getInventory().getSelected();
		if (currentItem != null	&& (currentItem.getItem() == CustomItems.wand || currentItem.getItem() == CustomItems.scripter)) {
            PlayerData data = PlayerData.get(player);
            data.scriptBlockPos = pos;
            SPacketGuiOpen.sendOpenGui(player, EnumGuiType.ScriptBlock, null, pos);
        	return InteractionResult.SUCCESS;
		}
		Vec3 vec = ray.getLocation();
        float x = (float)(vec.x - (double)pos.getX());
        float y = (float)(vec.y - (double)pos.getY());
        float z = (float)(vec.z - (double)pos.getZ());
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	return EventHooks.onScriptBlockInteract(tile, player, ray.getDirection().get3DDataValue(), x, y, z)?InteractionResult.FAIL : InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack item) {
        if(!level.isClientSide && entity instanceof Player){
            Player player = (Player) entity;
            PlayerData data = PlayerData.get(player);
            data.scriptBlockPos = pos;
            SPacketGuiOpen.sendOpenGui(player, EnumGuiType.ScriptBlock, null, pos);
    	}
    }

    @Override    
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
		if(level.isClientSide)
			return;
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	EventHooks.onScriptBlockCollide(tile, entityIn);
    }
    
    @Override    
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation type) {
		if(level.isClientSide || type != Biome.Precipitation.RAIN)
			return;
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	EventHooks.onScriptBlockRainFill(tile);
    }
    
    @Override       
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance){
		if(level.isClientSide)
			return;
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	fallDistance = EventHooks.onScriptBlockFallenUpon(tile, entity, fallDistance);
    	super.fallOn(level, state, pos, entity, fallDistance);
    }

//    @Override
//    public boolean isFullCube(BlockState state){
//        return false;
//    }
    
    @Override    
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if(level.isClientSide)
			return;
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	EventHooks.onScriptBlockClicked(tile, player);
    }
    
    @Override 
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving){
		if(!level.isClientSide){
	    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
	    	EventHooks.onScriptBlockBreak(tile);
		}
    	super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
    }

    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(!level.isClientSide){
            TileScripted tile = (TileScripted) level.getBlockEntity(pos);
            if(!EventHooks.onScriptBlockHarvest(tile, player))
                super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_287732_, LootParams.Builder p_287596_) {
        return Collections.emptyList();
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if(!level.isClientSide){
            TileScripted tile = (TileScripted) level.getBlockEntity(pos);
            if(EventHooks.onScriptBlockExploded(tile))
                return;
        }
        super.wasExploded(level, pos, explosion);
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos pos2, boolean isMoving) {
    	if(level.isClientSide)
    		return;
    	TileScripted tile = (TileScripted) level.getBlockEntity(pos);
    	EventHooks.onScriptBlockNeighborChanged(tile, pos2);
    	
    	int power = 0;
        for (Direction enumfacing : Direction.values()){
        	int p = level.getSignal(pos.relative(enumfacing), enumfacing);
        	if(p > power)
        		power = p;
        }
        if (tile.prevPower != power && tile.powering <= 0){
        	tile.newPower = power;
        }
    }

    @Override
    public boolean isSignalSource(BlockState state){
        return true;
    }

    
    @Override
    public int getSignal(BlockState state, BlockGetter worldIn, BlockPos pos, Direction side){
        return this.getDirectSignal(state, worldIn, pos, side);
    }
    
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side){
        TileScripted tile = ((TileScripted) level.getBlockEntity(pos));
        if(tile!=null) return tile.activePowering;
        return 0;
    }

//    @Override
//    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity){
//        TileScripted tile = ((TileScripted) level.getBlockEntity(pos));
//        LivingEntity
//        if(tile!=null) return tile.isLadder;
//        return false;
//    }
    
//    @Override
//    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, @Nullable EntityType<?> entityType){
//    	return true;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return super.propagatesSkylightDown(state, level, pos);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        TileScripted tile = (TileScripted) level.getBlockEntity(pos);
        if(tile == null)
            return 0;
        return tile.lightValue;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        TileScripted tile = ((TileScripted) level.getBlockEntity(pos));
        if(tile!=null) return tile.isPassible;
        return false;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        TileScripted tile = ((TileScripted) level.getBlockEntity(pos));
        float f = -1.0F;
        if(tile!=null) {
            f = tile.blockHardness;
        }
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = player.hasCorrectToolForDrops(state) ? 30 : 100;
            return player.getDestroySpeed(state) / f / (float)i;
        }
    }

//    @Override
//    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion){
//        TileScripted tile = ((TileScripted) level.getBlockEntity(pos));
//        if(tile!=null) return tile.blockResistance;
//        return 0;
//    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, CustomBlocks.tile_scripted, TileScripted::tick);
    }
}
