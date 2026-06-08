package noppes.npcs.blocks.tiles;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.CustomBlocks;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.event.Event;
import noppes.npcs.api.wrapper.BlockScriptedDoorWrapper;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.data.DataTimers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TileScriptedDoor extends TileDoor implements IScriptBlockHandler{
	public List<ScriptContainer> scripts = new ArrayList<ScriptContainer>();
	public boolean shouldRefreshData = false;
	
	public String scriptLanguage = "ECMAScript";
	public boolean enabled = false;

	private IBlock blockDummy = null;
	public DataTimers timers = new DataTimers(this);
	
	public long lastInited = -1;
	
	private short tickCount = 0;

	public int newPower = 0; //used for block redstone event
	public int prevPower = 0; //used for block redstone event

	public float blockHardness = 5;
	public float blockResistance = 10;

	public TileScriptedDoor(BlockPos pos, BlockState state){
		super(CustomBlocks.tile_scripteddoor, pos, state);
	}
		
	public IBlock getBlock(){
		if(blockDummy == null)
			blockDummy = new BlockScriptedDoorWrapper(getLevel(), CustomBlocks.scripted_door, getBlockPos());
		return blockDummy;
	}
    
	@Override
    public void load(CompoundTag compound){
		super.load(compound);
		setNBT(compound);
		timers.load(compound);
    }
	
	public void setNBT(CompoundTag compound){
		scripts = NBTTags.GetScript(compound.getList("Scripts", 10), this);
		scriptLanguage = compound.getString("ScriptLanguage");
		enabled = compound.getBoolean("ScriptEnabled");
		prevPower = compound.getInt("BlockPrevPower");

		if(compound.contains("BlockHardness")){
			blockHardness = compound.getFloat("BlockHardness");
			blockResistance = compound.getFloat("BlockResistance");
		}
	}
	
	@Override
    public void saveAdditional(CompoundTag compound){
		getNBT(compound);
		timers.save(compound);
    	super.saveAdditional(compound);
    }
	
	public CompoundTag getNBT(CompoundTag compound){
		compound.put("Scripts", NBTTags.NBTScript(scripts));
		compound.putString("ScriptLanguage", scriptLanguage);
		compound.putBoolean("ScriptEnabled", enabled);
		compound.putInt("BlockPrevPower", prevPower);
		compound.putFloat("BlockHardness", blockHardness);
		compound.putFloat("BlockResistance", blockResistance);
		return compound;
	}

	@Override
	public void runScript(EnumScriptType type, Event event) {
		if(!isEnabled())
			return;
		if(ScriptController.Instance.lastLoaded > lastInited){
			lastInited = ScriptController.Instance.lastLoaded;
			if(type != EnumScriptType.INIT)
				EventHooks.onScriptBlockInit(this);
		}
		
		for(ScriptContainer script : scripts){			
			script.run(type, event);
		}
	}

	private boolean isEnabled() {
		return enabled && ScriptController.HasStart && !level.isClientSide;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TileScriptedDoor tile) {
		tile.tickCount++;

        if (tile.prevPower != tile.newPower){
        	EventHooks.onScriptBlockRedstonePower(tile, tile.prevPower, tile.newPower);
			tile.prevPower = tile.newPower;
        }

		tile.timers.update();
		if(tile.tickCount >= 10){
			EventHooks.onScriptBlockUpdate(tile);
			tile.tickCount = 0;
		}
	}

	@Override
	public boolean isClient() {
		return getLevel().isClientSide;
	}

	@Override
	public boolean getEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean bo) {
		enabled = bo;
	}

	@Override
	public String getLanguage() {
		return scriptLanguage;
	}

	@Override
	public void setLanguage(String lang) {
		scriptLanguage = lang;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return scripts;
	}

	@Override
	public String noticeString() {
		BlockPos pos = getBlockPos();
		return MoreObjects.toStringHelper(this).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
	}

	@Override
	public Map<Long, String> getConsoleText(){
		Map<Long, String> map = new TreeMap<Long, String>();
		int tab = 0;
		for(ScriptContainer script : getScripts()){
			tab++;
			for(Entry<Long, String> entry : script.console.entrySet()){
				map.put(entry.getKey(), " tab " + tab + ":\n" + entry.getValue());
			}
		}
		return map;
	}

	@Override
	public void clearConsole() {
		for(ScriptContainer script : getScripts()){
			script.console.clear();
		}
	}
}
