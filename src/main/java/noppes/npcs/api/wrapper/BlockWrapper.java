package noppes.npcs.api.wrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.api.*;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.blocks.tiles.TileNpcEntity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.IEntityPersistentData;
import noppes.npcs.mixin.EntityIMixin;
import noppes.npcs.shared.common.util.LRUHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BlockWrapper implements IBlock{
	private static final Map<String, BlockWrapper> blockCache = new LRUHashMap<String, BlockWrapper>(400);
	protected final IWorld level;
	protected final Block block;
	protected final BlockPos pos;
	protected final BlockPosWrapper bPos;
	protected BlockEntity tile;
	protected TileNpcEntity storage;
	
	private final IData tempdata = new IData() {
		
		@Override
		public void remove(String key) {
			if(storage == null)
				return;
			storage.tempData.remove(key);
		}
		
		@Override
		public void put(String key, Object value) {
			if(storage == null)
				return;
			storage.tempData.put(key, value);
		}
		
		@Override
		public boolean has(String key) {
			if(storage == null)
				return false;
			return storage.tempData.containsKey(key);
		}
		
		@Override
		public Object get(String key) {
			if(storage == null)
				return null;
			return storage.tempData.get(key);
		}
		
		@Override
		public void clear() {
			if(storage == null)
				return;
			storage.tempData.clear();
		}

		@Override
		public String[] getKeys() {
			return storage.tempData.keySet().toArray(new String[storage.tempData.size()]);
		}
	};
	
	private final IData storeddata = new IData() {

		@Override
		public void put(String key, Object value) {
			CompoundTag compound = getNBT();
			if(compound == null)
				return;
			if(value instanceof Number)
				compound.putDouble(key, ((Number) value).doubleValue());
			else if(value instanceof String)
				compound.putString(key, (String)value);
		}

		@Override
		public Object get(String key) {
			CompoundTag compound = getNBT();
			if(compound == null)
				return null;
			if(!compound.contains(key))
				return null;
			Tag base = compound.get(key);
			if(base instanceof NumericTag)
				return ((NumericTag)base).getAsDouble();
			return base.getAsString();
		}

		@Override
		public void remove(String key) {
			CompoundTag compound = getNBT();
			if(compound == null)
				return;
			compound.remove(key);
		}

		@Override
		public boolean has(String key) {
			CompoundTag compound = getNBT();
			if(compound == null)
				return false;
			return compound.contains(key);
		}

		@Override
		public void clear() {
			if(tile == null)
				return;
			((IEntityPersistentData)tile).getPersistentData().put("CustomNPCsData", new CompoundTag());
		}
		
		private CompoundTag getNBT(){
			if(tile == null)
				return null;			
			CompoundTag compound = ((IEntityPersistentData)tile).getPersistentData().getCompound("CustomNPCsData");
			if(compound.isEmpty() && !((IEntityPersistentData)tile).getPersistentData().contains("CustomNPCsData")){
				((IEntityPersistentData)tile).getPersistentData().put("CustomNPCsData", compound);
			}
			return compound;
		}

		@Override
		public String[] getKeys() {
			CompoundTag compound = getNBT();
			if(compound == null)
				return new String[0];
			return compound.getAllKeys().toArray(new String[compound.getAllKeys().size()]);
		}
	};
	
	protected BlockWrapper(Level level, Block block, BlockPos pos){
		this.level = NpcAPI.Instance().getIWorld((ServerLevel)level);
		this.block = block;
		this.pos = pos;
		this.bPos = new BlockPosWrapper(pos);
		this.setTile(level.getBlockEntity(pos));
	}

	@Override
	public int getX(){
		return pos.getX();
	}

	@Override
	public int getY(){
		return pos.getY();
	}

	@Override
	public int getZ(){
		return pos.getZ();
	}

	@Override
	public IPos getPos() {
		return bPos;
	}

	@Override
	public Object getProperty(String name) {
		BlockState state = getMCBlockState();
		for(Property p : state.getProperties()){
			if(p.getName().equalsIgnoreCase(name)){
				return state.getValue(p);
			}
		}
		throw new CustomNPCsException("Unknown property: " + name);
	}

	@Override
	public void setProperty(String name, Object val) {
		if(!(val instanceof Comparable)){
			throw new CustomNPCsException("Not a valid property value: " + val);
		}
		BlockState state = getMCBlockState();
		for(Property<? extends Comparable<?>> p : state.getProperties()){
			if(p.getName().equalsIgnoreCase(name)){
				setPropertyValue(state, p, (Comparable<?>)val);
				return;
			}
		}
		throw new CustomNPCsException("Unknown property: " + name);
	}

	private <T extends Comparable<T>> void setPropertyValue(BlockState state, Property<T> p, Comparable<?> c){
		this.level.getMCLevel().setBlock(pos,state.setValue(p, p.getValueClass().cast(c)),3);
	}

	@Override
	public String[] getProperties() {
		Collection<Property<?>> props = getMCBlockState().getProperties();
		List<String> list = new ArrayList<>();
		for(Property prop : props){
			list.add(prop.getName());
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public void remove(){
		level.getMCLevel().removeBlock(pos, false);
	}

	@Override
	public boolean isRemoved(){
		BlockState state = level.getMCLevel().getBlockState(pos);
		if(state == null)
			return true;
		return state.getBlock() != block;
	}

	@Override
	public boolean isAir(){
		return level.getMCLevel().getBlockState(pos).isAir();
	}
	
	@Override
	public BlockWrapper setBlock(String name){
		Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(name));
		if(block == null)
			return this;
		level.getMCLevel().setBlock(pos, block.defaultBlockState(), 2);
		return new BlockWrapper(level.getMCLevel(), block, pos);
	}
	
	@Override
	public BlockWrapper setBlock(IBlock block){
		level.getMCLevel().setBlock(pos, block.getMCBlock().defaultBlockState(), 2);
		return new BlockWrapper(level.getMCLevel(), block.getMCBlock(), pos);
	}

	@Override
	public boolean isContainer(){
		if(tile == null || !(tile instanceof Container))
			return false;
		return ((Container)tile).getContainerSize() > 0;
	}
	
	@Override
	public IContainer getContainer(){
		if(!isContainer())
			throw new CustomNPCsException("This block is not a container");
		return NpcAPI.Instance().getIContainer((Container) tile);
	}

	@Override
	public IData getTempdata(){
		return tempdata;
	}

	@Override
	public IData getStoreddata(){
		return storeddata;
	}

	@Override
	public String getName(){
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}

	@Override
	public String getDisplayName() {
		if(tile == null || !(tile instanceof Nameable))
			return getName();
		return ((Nameable)tile).getDisplayName().getString();
	}
	
	@Override
	public IWorld getWorld() {
		return level;
	}

	@Override
	public Block getMCBlock(){
		return block;
	}

	@Deprecated
	public static IBlock createNew(Level level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		String key = state.toString() + pos.toString();
		BlockWrapper b = blockCache.get(key);
		if(b != null){
			b.setTile(level.getBlockEntity(pos));
			return b;
		}
		
		if(block instanceof BlockScripted)
			b = new BlockScriptedWrapper(level, block, pos);
		else if(block instanceof BlockScriptedDoor)
			b = new BlockScriptedDoorWrapper(level, block, pos);
		else
			b = new BlockWrapper(level, block, pos);
		blockCache.put(key, b);	
		
		return b;
	}
	
	public static void clearCache(){
		blockCache.clear();
	}

	@Override
	public boolean hasTileEntity() {
		return tile != null;
	}
	
	protected void setTile(BlockEntity tile){
		this.tile = tile;
		if(tile instanceof TileNpcEntity)
			storage = (TileNpcEntity)tile;
	}
	
	@Override
	public INbt getBlockEntityNBT() {
		CompoundTag compound = tile.saveWithoutMetadata();
		return NpcAPI.Instance().getINbt(compound);
	}

	@Override
	public void setTileEntityNBT(INbt nbt){
		tile.load(nbt.getMCNBT());
		tile.setChanged();
		BlockState state = this.level.getMCLevel().getBlockState(pos);
		level.getMCLevel().sendBlockUpdated(pos, state, state, 3);
	}

	@Override
	public BlockEntity getMCTileEntity() {
		return tile;
	}

	@Override
	public BlockState getMCBlockState() {
		return this.level.getMCLevel().getBlockState(pos);
	}

	@Override
	public void blockEvent(int type, int data) {
		level.getMCLevel().blockEvent(pos, getMCBlock(), type, data);
	}

	@Override
	public void interact(int side) {
		Player player = EntityNPCInterface.GenericPlayer;
		Level w = level.getMCLevel();
		((EntityIMixin)player).setLevel(w);
		player.setPos(pos.getX(), pos.getY(), pos.getZ());
		getMCBlockState().use(w, EntityNPCInterface.CommandPlayer, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.from3DDataValue(side), pos, true));
	}
}
