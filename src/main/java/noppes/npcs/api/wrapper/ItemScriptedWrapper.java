package noppes.npcs.api.wrapper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.event.Event;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;



public class ItemScriptedWrapper extends ItemStackWrapper implements IItemScripted, IScriptHandler  {
	public List<ScriptContainer> scripts = new ArrayList<ScriptContainer>();
	
	public String scriptLanguage = "ECMAScript";
	public boolean enabled = false;	
	public long lastInited = -1;
	
	public boolean updateClient = false;
	
	public boolean durabilityShow = true;
	public float durabilityValue = 1;
	public int durabilityColor = -1;

	public int itemColor = -1;
	
	public int stackSize = 64;
	
	public boolean loaded = false;

	public ResourceLocation texture = null;

	public ItemScriptedWrapper(ItemStack item) {
		super(item);
	}

	@Override
	public boolean hasTexture(int damage) {
		return texture != null;
	}

	@Override
	public String getTexture(int damage) {
		return getTexture();
	}

	@Override
	public String getTexture() {
		if(texture == null){
			return null;
		}
		return texture.toString();
	}

	@Override
	public void setTexture(int damage, String texture) {
		this.setTexture(texture);
	}

	@Override
	public void setTexture(String texture) {
		if(texture == null){
			this.texture = null;
		}
		else{
			this.texture = new ResourceLocation(texture);
		}
	}

	public CompoundTag getScriptNBT(CompoundTag compound) {
		compound.put("Scripts", NBTTags.NBTScript(scripts));
		compound.putString("ScriptLanguage", scriptLanguage);
		compound.putBoolean("ScriptEnabled", enabled);
		if(texture != null){
			compound.putString("ScriptTexture", texture.toString());
		}
		return compound;
	}
	
	@Override
	public CompoundTag getMCNbt(){
		CompoundTag compound = super.getMCNbt();
		getScriptNBT(compound);

		compound.putBoolean("DurabilityShow", durabilityShow);
		compound.putFloat("DurabilityValue", durabilityValue);
		compound.putInt("DurabilityColor", durabilityColor);

		compound.putInt("ItemColor", itemColor);

		compound.putInt("MaxStackSize", stackSize);
		if(item.getTag() != null){
			compound.put("ItemTags", item.getTag());
		}
		return compound;
	}
	
	public void setScriptNBT(CompoundTag compound) {
		if(!compound.contains("Scripts")) {
			return;
		}
		scripts = NBTTags.GetScript(compound.getList("Scripts", 10), this);
		scriptLanguage = compound.getString("ScriptLanguage");
		enabled = compound.getBoolean("ScriptEnabled");

		if(compound.contains("ScriptTexture")){
			this.texture = new ResourceLocation(compound.getString("ScriptTexture"));
		}
	}

	@Override
	public void setMCNbt(CompoundTag compound){
		super.setMCNbt(compound);
		setScriptNBT(compound);

		durabilityShow = compound.getBoolean("DurabilityShow");
		durabilityValue = compound.getFloat("DurabilityValue");
		if(compound.contains("DurabilityColor")) {
			durabilityColor = compound.getInt("DurabilityColor");
		}

		itemColor = compound.getInt("ItemColor");

		stackSize = compound.getInt("MaxStackSize");

		if(compound.contains("ItemTags")){
			item.setTag(compound.getCompound("ItemTags"));
		}
	}

	@Override
	public int getType(){
		return ItemType.SCRIPTED;
	}

	@Override
	public void runScript(EnumScriptType type, Event event) {
		if(!loaded) {
			loadScriptData();
			loaded = true;
		}
		if(!isEnabled())
			return;
		if(ScriptController.Instance.lastLoaded > lastInited){
			lastInited = ScriptController.Instance.lastLoaded;
			if(type != EnumScriptType.INIT)
				EventHooks.onScriptItemInit(this);
		}
		
		for(ScriptContainer script : scripts){			
			script.run(type, event);
		}
	}

	private boolean isEnabled() {
		return enabled && ScriptController.HasStart;
	}

	@Override
	public boolean isClient() {
		return false;
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
		this.scriptLanguage = lang;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return scripts;
	}

	@Override
	public String noticeString() {
		return "ScriptedItem";
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

	@Override
	public int getMaxStackSize(){ 
		return stackSize;
	}

	@Override
	public void setMaxStackSize(int size) {
		if(size < 1 || size > 64)
			throw new CustomNPCsException("Stacksize has to be between 1 and 64");
		this.stackSize = size;
	}

	@Override
	public double getDurabilityValue() {
		return durabilityValue;
	}

	@Override
	public void setDurabilityValue(float value) {
		if(value != durabilityValue)
			updateClient = true;
		durabilityValue = value;
	}

	@Override
	public boolean getDurabilityShow() {
		return durabilityShow;
	}

	@Override
	public void setDurabilityShow(boolean bo) {
		if(bo != durabilityShow)
			updateClient = true;
		durabilityShow = bo;
	}

	@Override
	public int getDurabilityColor() {
		return durabilityColor;
	}

	@Override
	public void setDurabilityColor(int color) {
		if(color != durabilityColor)
			updateClient = true;
		durabilityColor = color;
	}

	@Override
	public int getColor() {
		return itemColor;
	}

	@Override
	public void setColor(int color) {
		if(color != itemColor)
			updateClient = true;
		itemColor = color;
	}

	public void saveScriptData() {
		CompoundTag c = item.getTag();
		if(c == null) {
			item.setTag(c = new CompoundTag());
		}
		c.put("ScriptedData", getScriptNBT(new CompoundTag()));
	}
	
	public void loadScriptData() {
		CompoundTag c = item.getTag();
		if(c == null)
			return;
		setScriptNBT(c.getCompound("ScriptedData"));
	}
}
