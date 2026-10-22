package noppes.npcs.controllers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.wrapper.WorldWrapper;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.shared.common.util.LogWriter;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.NBTJsonUtil.JsonException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;



public class ScriptController {
	
	public static ScriptController Instance;
	public static boolean HasStart = false;
	private ScriptEngineManager manager;
	public Map<String, String> languages = new HashMap<String, String>();
	public Map<String, ScriptEngineFactory> factories = new HashMap<String, ScriptEngineFactory>();
	public Map<String, String> scripts = new HashMap<String, String>();
	public PlayerScriptData playerScripts = new PlayerScriptData(null);
	public ForgeScriptData forgeScripts = new ForgeScriptData();
	public long lastLoaded = 0;
	public long lastPlayerUpdate = 0;
	public File dir;
	public CompoundTag compound = new CompoundTag();

	/** Name of the default worlddata file, world_data.json, used by getStoreddata() */
	public static final String DEFAULT_WORLD_DATA = "world_data";
	private static final Pattern WORLD_DATA_PATTERN = Pattern.compile("[a-zA-Z0-9_-]{1,64}");

	/** All other worlddata files, lazily loaded from scripts/world_data/<name>.json */
	private final Map<String, CompoundTag> namedWorldData = new HashMap<String, CompoundTag>();
	/** Names of worlddata files that have unsaved changes */
	private final Set<String> dirtyWorldData = new HashSet<String>();

	private boolean loaded = false;
	public boolean shouldSave = false;
	
	public ScriptController(){
		loaded = false;
		Instance = this;
		if(!CustomNpcs.NashorArguments.isEmpty()) {
			System.setProperty("nashorn.args", CustomNpcs.NashorArguments);
		}
		//ClassLoader classLoader = MinecraftForge.class.getClassLoader();
		LogWriter.info("Script Engines Available:");
		try {
			manager = new ScriptEngineManager();
			try {
				if (manager.getEngineByName("ecmascript") == null) {
					//Launch.classLoader.addClassLoaderExclusion("jdk.nashorn."); might need to fix this
					//Launch.classLoader.addClassLoaderExclusion("jdk.internal.dynalink");
					Class c = Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory");
					ScriptEngineFactory factory = (ScriptEngineFactory) c.newInstance();
					factory.getScriptEngine();
					LogWriter.info(factory.getLanguageName() + ": " + ".js");
					manager.registerEngineName("ecmascript", factory);
					manager.registerEngineExtension("js", factory);
					manager.registerEngineMimeType("application/ecmascript", factory);
					languages.put(factory.getLanguageName(), ".js");
					factories.put(factory.getLanguageName().toLowerCase(), factory);
				}
			} catch (Throwable e) {

			}
			try {
				Class c = Class.forName("org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory");
				ScriptEngineFactory factory = (ScriptEngineFactory) c.newInstance();
				factory.getScriptEngine();
				LogWriter.info(factory.getLanguageName() + ": " + ".ktl");
				manager.registerEngineName("kotlin", factory);
				manager.registerEngineExtension("ktl", factory);
				manager.registerEngineMimeType("application/kotlin", factory);
				languages.put(factory.getLanguageName(), ".ktl");
				factories.put(factory.getLanguageName().toLowerCase(), factory);
			} catch (Throwable e) {

			}
			try {
				Class c = Class.forName("noppes.scriptengines.ScriptEngines");
				List<ScriptEngineFactory> seFactories = (List<ScriptEngineFactory>) c.getDeclaredField("factories").get(null);
				for (ScriptEngineFactory fac : seFactories) {
					if (fac.getExtensions().size() == 0 || languages.containsKey(fac.getLanguageName()))
						continue;

					if (!(fac.getScriptEngine() instanceof Invocable) && !fac.getLanguageName().equals("lua"))
						continue;
					String ext = "." + fac.getExtensions().get(0).toLowerCase();
					LogWriter.info(fac.getLanguageName() + ": " + ext);
					languages.put(fac.getLanguageName(), ext);
					factories.put(fac.getLanguageName().toLowerCase(), fac);
				}
			} catch (Throwable e) {

			}

			for (ScriptEngineFactory fac : manager.getEngineFactories()) {
				try {
					if (fac.getExtensions().size() == 0 || languages.containsKey(fac.getLanguageName()))
						continue;

					if (!(fac.getScriptEngine() instanceof Invocable) && !fac.getLanguageName().equals("lua"))
						continue;
					String ext = "." + fac.getExtensions().get(0).toLowerCase();
					LogWriter.info(fac.getLanguageName() + ": " + ext);
					languages.put(fac.getLanguageName(), ext);
					factories.put(fac.getLanguageName().toLowerCase(), fac);
				} catch (Throwable e) {
					LogWriter.except(e);
				}
			}
		} catch (Throwable e) {
			LogWriter.except(e);
		}
	}

	public void loadCategories(){
		dir = new File(CustomNpcs.getLevelSaveDirectory(), "scripts");
		if(!dir.exists())
			dir.mkdirs();
		if(!worldDataFile().exists())
			shouldSave = true;
		WorldWrapper.tempData.clear();
		
		scripts.clear();
		for(String language : languages.keySet()){
			String ext = languages.get(language);
			File scriptDir = new File(dir,language.toLowerCase());
			if(!scriptDir.exists())
				scriptDir.mkdir();
			else
				loadDir(scriptDir, "", ext);
		}
		lastLoaded = System.currentTimeMillis();
	}
	
	private void loadDir(File dir, String name, String ext){
		for(File file : dir.listFiles()){
			String filename = name + file.getName().toLowerCase();
			if(file.isDirectory()){
				loadDir(file, filename + "/", ext);
				continue;
			}
			if(!filename.endsWith(ext))
				continue;
			try {
				scripts.put(filename, readFile(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean loadStoredData(){
		this.compound = new CompoundTag();
		this.namedWorldData.clear();
		this.dirtyWorldData.clear();
    	File file = worldDataFile();
        try {
        	if(!file.exists())
        		return false;
        	this.compound = NBTJsonUtil.LoadFile(file);
    		shouldSave = false;
		} 
        catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
        	return false;
        }
        return true;
	}
	
	private File worldDataFile(){
		return new File(dir, DEFAULT_WORLD_DATA + ".json");
	}

	private File worldDataFolder(){
		return new File(dir, DEFAULT_WORLD_DATA);
	}

	private File worldDataFile(String name){
		return new File(worldDataFolder(), name + ".json");
	}

	/**
	 * Checks whether the given name is a valid worlddata name, throws otherwise
	 */
	public static String checkWorldDataName(String name){
		if(name == null || !WORLD_DATA_PATTERN.matcher(name).matches())
			throw new CustomNPCsException("Invalid worlddata name: %s, only letters, numbers, _ and - are allowed (max 64)", name);
		return name;
	}

	/**
	 * Gets the compound of the given worlddata file, loading it from disk the first time its used.
	 * A file that doesnt exist yet simply returns an empty compound without being created.
	 */
	public CompoundTag getWorldData(String name){
		checkWorldDataName(name);
		if(DEFAULT_WORLD_DATA.equals(name))
			return compound;
		CompoundTag data = namedWorldData.get(name);
		if(data == null){
			data = new CompoundTag();
			File file = worldDataFile(name);
			if(file.exists()){
				try {
					data = NBTJsonUtil.LoadFile(file);
				}
				catch (Exception e) {
					LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
				}
			}
			namedWorldData.put(name, data);
		}
		return data;
	}

	/**
	 * Marks the given worlddata file as needing to be saved
	 */
	public void markWorldDataDirty(String name){
		if(DEFAULT_WORLD_DATA.equals(name))
			shouldSave = true;
		else
			dirtyWorldData.add(name);
	}

	/**
	 * Empties the given worlddata, the file is removed or created again on the next save
	 */
	public void clearWorldData(String name){
		checkWorldDataName(name);
		if(DEFAULT_WORLD_DATA.equals(name)){
			compound = new CompoundTag();
			shouldSave = true;
			return;
		}
		boolean existed = worldDataFile(name).exists() || (namedWorldData.containsKey(name) && !namedWorldData.get(name).isEmpty());
		namedWorldData.put(name, new CompoundTag());
		if(existed)
			dirtyWorldData.add(name);
	}

	/**
	 * @return The names of all worlddata files on disk plus all currently loaded ones, including the default
	 */
	public String[] getWorldDataNames(){
		Set<String> names = new TreeSet<String>();
		names.add(DEFAULT_WORLD_DATA);
		names.addAll(namedWorldData.keySet());
		File[] files = worldDataFolder().listFiles();
		if(files != null){
			for(File file : files){
				String name = file.getName();
				if(!file.isFile() || !name.endsWith(".json"))
					continue;
				name = name.substring(0, name.length() - 5);
				if(WORLD_DATA_PATTERN.matcher(name).matches())
					names.add(name);
			}
		}
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Removes the given worlddata from memory and deletes its file
	 * @return True if no file with that name exists anymore
	 */
	public boolean deleteWorldData(String name){
		checkWorldDataName(name);
		if(DEFAULT_WORLD_DATA.equals(name))
			throw new CustomNPCsException("The default worlddata %s cannot be deleted", DEFAULT_WORLD_DATA);
		namedWorldData.remove(name);
		dirtyWorldData.remove(name);
		File file = worldDataFile(name);
		return !file.exists() || file.delete();
	}
	
	private File playerScriptsFile(){
		return new File(dir, "player_scripts.json");
	}
	
	private File forgeScriptsFile(){
		return new File(dir, "forge_scripts.json");
	}
	
	public boolean loadPlayerScripts(){
		this.playerScripts.clear();
    	File file = playerScriptsFile();
        try {
        	if(!file.exists())
        		return false;
        	this.playerScripts.load(NBTJsonUtil.LoadFile(file));
		} 
        catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
        	return false;
        }
        return true;
	}
	
	public void setPlayerScripts(CompoundTag compound){
    	this.playerScripts.load(compound);
    	
    	File file = playerScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			lastPlayerUpdate = System.currentTimeMillis();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JsonException e) {
			e.printStackTrace();
		}
	}
	
	public boolean loadForgeScripts(){
		this.forgeScripts.clear();
    	File file = forgeScriptsFile();
        try {
        	if(!file.exists())
        		return false;
        	this.forgeScripts.load(NBTJsonUtil.LoadFile(file));
		} 
        catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
        	return false;
        }
        return true;
	}
	
	public void setForgeScripts(CompoundTag compound){
    	this.forgeScripts.load(compound);
    	
    	File file = forgeScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.forgeScripts.lastInited = -1;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JsonException e) {
			e.printStackTrace();
		}
	}
	
	private String readFile(File file) throws IOException {
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

	public ScriptEngine getEngineByName(String language) {
		ScriptEngineFactory fac = factories.get(language.toLowerCase());
		if(fac == null)
			return null;
		return fac.getScriptEngine();
	}

	public ListTag nbtLanguages() {
		ListTag list = new ListTag();
		for(String language : languages.keySet()){
			CompoundTag compound = new CompoundTag();
			ListTag scripts = new ListTag();
			for(String script : getScripts(language)){
				scripts.add(StringTag.valueOf(script));
			}
			compound.put("Scripts", scripts);
			compound.putString("Language", language);
			list.add(compound);
		}
		return list;
	}
	
	private List<String> getScripts(String language){
		List<String> list = new ArrayList<String>();
		String ext = languages.get(language);
		if(ext == null)
			return list;
		for(String script : scripts.keySet()){
			if(script.endsWith(ext)){
				list.add(script);
			}
		}
		return list;
	}

	public void saveLevel(Level level){
		if((!shouldSave && dirtyWorldData.isEmpty()) || !(level instanceof ServerLevel) || (level).dimension() != Level.OVERWORLD) {
			return;
		}
		
		if(shouldSave){
			try {
				NBTJsonUtil.SaveFile(worldDataFile(), compound.copy());
			} 
			catch (Exception e) {
				LogWriter.except(e);
			} 
			shouldSave = false;
		}

		if(!dirtyWorldData.isEmpty()){
			File folder = worldDataFolder();
			if(!folder.exists())
				folder.mkdirs();
			for(String name : dirtyWorldData){
				CompoundTag data = namedWorldData.get(name);
				if(data == null)
					continue;
				try {
					NBTJsonUtil.SaveFile(worldDataFile(name), data.copy());
				} 
				catch (Exception e) {
					LogWriter.except(e);
				}
			}
			dirtyWorldData.clear();
		}
	}
}