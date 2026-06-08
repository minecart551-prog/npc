package noppes.npcs.blocks.tiles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomBlocks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.schematics.SchematicWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TileBuilder extends BlockEntity{
	private SchematicWrapper schematic = null;
	public int rotation = 0;
	public int yOffest = 0;
	public boolean enabled = false;
	public boolean started = false;
	public boolean finished = false;
	public Availability availability = new Availability();
	private Stack<Integer> positions = new Stack<Integer>();
	private Stack<Integer> positionsSecond = new Stack<Integer>();
	
	public static BlockPos DrawPos = null;
	public static boolean Compiled = false;
	
	private int ticks = 20;

	public TileBuilder(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_builder, pos, state);
	}

	@Override
    public void load(CompoundTag compound){
		super.load(compound);
        if(compound.contains("SchematicName")){
        	schematic = SchematicController.Instance.load(compound.getString("SchematicName"));
        }
        
        Stack<Integer> positions = new Stack<Integer>();
        positions.addAll(NBTTags.getIntegerList(compound.getList("Positions", 10)));
        this.positions = positions;

        positions = new Stack<Integer>();
        positions.addAll(NBTTags.getIntegerList(compound.getList("PositionsSecond", 10)));
        this.positionsSecond = positions;
        
        readPartNBT(compound);
    }

    public void readPartNBT(CompoundTag compound){
        rotation = compound.getInt("Rotation");
        yOffest = compound.getInt("YOffset");
        enabled = compound.getBoolean("Enabled");
        started = compound.getBoolean("Started");
        finished = compound.getBoolean("Finished");
        availability.load(compound.getCompound("Availability"));
    }

    @Override
    public void saveAdditional(CompoundTag compound){
    	super.saveAdditional(compound);
    	if(schematic != null){
    		compound.putString("SchematicName", schematic.schema.getName());
    	}
    	compound.put("Positions", NBTTags.nbtIntegerCollection(new ArrayList<Integer>(positions)));
    	compound.put("PositionsSecond", NBTTags.nbtIntegerCollection(new ArrayList<Integer>(positionsSecond)));
    	writePartNBT(compound);
    }

    public CompoundTag writePartNBT(CompoundTag compound){
    	compound.putInt("Rotation", rotation);
    	compound.putInt("YOffset", yOffest);
    	compound.putBoolean("Enabled", enabled);
    	compound.putBoolean("Started", started);
    	compound.putBoolean("Finished", finished);
    	compound.put("Availability", availability.save(new CompoundTag()));
    	return compound;
    }

	@Environment(EnvType.CLIENT)
    public void setDrawSchematic(SchematicWrapper schematics){
    	this.schematic = schematics;
    }
    
    public void setSchematic(SchematicWrapper schematics){
    	this.schematic = schematics;
    	if(schematics == null){
    		positions.clear();
    		positionsSecond.clear();
    		return;
    	}
    	Stack<Integer> positions = new Stack<Integer>();
		for(int y = 0; y < schematics.schema.getHeight(); y++){
			for(int z = 0; z < schematics.schema.getLength() / 2; z++){
				for(int x = 0; x < schematics.schema.getWidth() / 2; x++){
					positions.add(0, xyzToIndex(x, y, z));
				}
			}
			for(int z = 0; z < schematics.schema.getLength() / 2; z++){
				for(int x = schematics.schema.getWidth() / 2; x < schematics.schema.getWidth(); x++){
					positions.add(0, xyzToIndex(x, y, z));
				}
			}
			for(int z = schematics.schema.getLength() / 2; z < schematics.schema.getLength(); z++){
				for(int x = 0; x < schematics.schema.getWidth() / 2; x++){
					positions.add(0, xyzToIndex(x, y, z));
				}
			}
			for(int z = schematics.schema.getLength() / 2; z < schematics.schema.getLength(); z++){
				for(int x = schematics.schema.getWidth() / 2; x < schematics.schema.getWidth(); x++){
					positions.add(0, xyzToIndex(x, y, z));
				}
			}
		}
		this.positions = positions;
		positionsSecond.clear();
    }
	
	public int xyzToIndex(int x, int y, int z){
		return (y * schematic.schema.getLength() + z) * schematic.schema.getWidth() + x;
	}
    
    public SchematicWrapper getSchematic(){
    	return schematic;
    }
    
    public boolean hasSchematic(){
    	return schematic != null;
    }

	public static void tick(Level level, BlockPos pos, BlockState state, TileBuilder tile) {
		if(level.isClientSide || !tile.hasSchematic() || tile.finished)
			return;
		tile.ticks--;
		if(tile.ticks > 0)
			return;
		tile.ticks = 200;
		if(tile.positions.isEmpty() && tile.positionsSecond.isEmpty()){
			tile.finished = true;
			return;
		}
		
		if(!tile.started){
			for(Player player : tile.getPlayerList()){
				if(tile.availability.isAvailable(player)){
					tile.started = true;
					break;
				}
			}
			if(!tile.started)
				return;
		}
		
		List<EntityNPCInterface> list = level.getEntitiesOfClass(EntityNPCInterface.class, new AABB(pos, pos).inflate(32, 32, 32));
		for(EntityNPCInterface npc : list){
			if(npc.job.getType() == JobType.BUILDER){
				JobBuilder job = (JobBuilder) npc.job;
				if(job.build == null){
					job.build = tile;
				}
				
			}
		}
	}
	
	private List<Player> getPlayerList(){
		return level.getEntitiesOfClass(Player.class, new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1).inflate(10, 10, 10));
	}
	
	public Stack<BlockData> getBlock(){
		if(!enabled || finished || !hasSchematic())
			return null;
		boolean bo = positions.isEmpty();
		Stack<BlockData> list = new Stack<BlockData>();
		int size = schematic.schema.getWidth() * schematic.schema.getLength() / 4;
		if(size > 30)
			size = 30;
		for(int i = 0; i < size; i++){
			if(positions.isEmpty() && !bo || positionsSecond.isEmpty() && bo)
				return list;
			
			int pos = bo?positionsSecond.pop():positions.pop();
			if(pos >= schematic.size){
				continue;
			}

			int x = (int) (pos % schematic.schema.getWidth());
			int z = (int)((pos - x)/schematic.schema.getWidth()) % schematic.schema.getLength();
			int y = (int)(((pos - x)/schematic.schema.getWidth()) - z) / schematic.schema.getLength();
			BlockState state = schematic.schema.getBlockState(x, y, z);
	    	if(!state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) && !bo && state.getBlock() != Blocks.AIR){
	    		positionsSecond.add(0, pos);
	    		continue;
	    	}
			
			BlockPos blockPos = getBlockPos().offset(1, yOffest, 1).offset(schematic.rotatePos(x, y, z, rotation));
			
			BlockState original = level.getBlockState(blockPos);
			if(Block.getId(state) == Block.getId(original)) //If block is already set ignore
				continue;
			
			state = schematic.rotationState(state, rotation);
			CompoundTag tile = null;
	    	if(state.getBlock() instanceof EntityBlock){
	    		tile = schematic.getBlockEntity(x, y, z, blockPos);
	    	}
	    	list.add(0, new BlockData(blockPos, state, tile));
		}
		return list;
	}

//	@Override
//	public AABB getRenderBoundingBox(){
//		if(schematic == null){
//			return super.getRenderBoundingBox();
//		}
//		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() +
//				schematic.schema.getWidth() + 1, worldPosition.getY() + schematic.schema.getHeight() + 1, worldPosition.getZ() + schematic.schema.getLength() + 1);
//	}
	
	public static void SetDrawPos(BlockPos pos){
		DrawPos = pos;
		Compiled = false;
	}
}
