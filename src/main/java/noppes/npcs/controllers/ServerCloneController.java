package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import noppes.npcs.CustomNpcs;
import noppes.npcs.shared.common.util.LogWriter;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.packets.server.SPacketToolMobSpawner;
import noppes.npcs.util.NBTJsonUtil;

public class ServerCloneController implements ICloneHandler {
	public long lastLoaded = System.currentTimeMillis();
	public static ServerCloneController Instance;
	
	public ServerCloneController(){
		loadClones();
	}
	
	private void loadClones(){
		try {
			File dir = new File(getDir(), "..");
	        File file = new File(dir, "clonednpcs.dat");
	        if(file.exists()){
	        	Map<Integer, Map<String, CompoundTag>> clones = loadOldClones(file);
		        file.delete();
				file = new File(dir, "clonednpcs.dat_old");
				if(file.exists())
					file.delete();
				
				for(int tab : clones.keySet()){
					Map<String, CompoundTag> map = clones.get(tab);
					for(String name: map.keySet()){
						saveClone(tab, name, map.get(name));
					}
				}
	        }
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	public File getDir(){
		File dir = new File(CustomNpcs.getLevelSaveDirectory(), "clones");
		if(!dir.exists())
			dir.mkdir();
		return dir;
	}
	
	private Map<Integer, Map<String, CompoundTag>> loadOldClones(File file) throws Exception{
		Map<Integer, Map<String, CompoundTag>> clones = new HashMap<Integer, Map<String, CompoundTag>>();
        CompoundTag nbttagcompound1 = NbtIo.readCompressed(new FileInputStream(file));
        ListTag list = nbttagcompound1.getList("Data", 10);
        if(list == null){
        	return clones;
        }
        for(int i = 0; i < list.size(); i++)
        {
            CompoundTag compound = list.getCompound(i);
            if(!compound.contains("ClonedTab")){
            	compound.putInt("ClonedTab", 1);
            }
            
            Map<String, CompoundTag> tab = clones.get(compound.getInt("ClonedTab"));
            if(tab == null)
            	clones.put(compound.getInt("ClonedTab"), tab = new HashMap<String, CompoundTag>());
            
        	String name = compound.getString("ClonedName");
        	int number = 1;
        	while(tab.containsKey(name)){
        		number++;
            	name = String.format("%s%s", compound.getString("ClonedName"), number);
        	}
        	compound.remove("ClonedName");
        	compound.remove("ClonedTab");
        	compound.remove("ClonedDate");
    		cleanTags(compound);
        	tab.put(name, compound);
        }
		return clones;
	}
	
	public CompoundTag getCloneData(CommandSourceStack player, String name, int tab) {
		File file = new File(new File(getDir(), tab + ""), name + ".json");
		if(!file.exists()){
			if(player != null) {
				player.sendFailure(Component.literal("Could not find clone file"));
			}
			return null;
		}
		try {
			return NBTJsonUtil.LoadFile(file);
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			if(player != null)
				player.sendFailure(Component.literal(e.getMessage()));
		}
		return null;
	}
	
	public void saveClone(int tab, String name, CompoundTag compound){
		try {
			File dir = new File(getDir(), tab + "");
			if(!dir.exists())
				dir.mkdir();
			String filename = name + ".json";
			
            File file = new File(dir, filename + "_new");
            File file2 = new File(dir, filename);
            
            NBTJsonUtil.SaveFile(file, compound);
            if(file2.exists()){
            	file2.delete();
            }
            file.renameTo(file2);
			lastLoaded = System.currentTimeMillis();
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}
	public List<String> getClones(int tab){
		List<String> list = new ArrayList<String>();
		File dir = new File(getDir(), tab + "");
		if(!dir.exists() || !dir.isDirectory())
			return list;
		for(String file : dir.list()){
			if(file.endsWith(".json"))
				list.add(file.substring(0, file.length() - 5));
		}
		return list;
	}
	
	public boolean removeClone(String name, int tab){
        File file = new File(new File(getDir(), tab + ""), name + ".json");
        if(!file.exists())
        	return false;
        file.delete();
        return true;
	}
	
	public String addClone(CompoundTag nbttagcompound, String name, int tab) {
		cleanTags(nbttagcompound);
		saveClone(tab, name, nbttagcompound);
		return name;
	}
	
	public void cleanTags(CompoundTag nbttagcompound){
		if(nbttagcompound.contains("ItemGiverId"))
			nbttagcompound.putInt("ItemGiverId", 0);
		if(nbttagcompound.contains("TransporterId"))
			nbttagcompound.putInt("TransporterId", -1);
		
		nbttagcompound.remove("StartPosNew");
		nbttagcompound.remove("StartPos");
		nbttagcompound.remove("MovingPathNew");
		nbttagcompound.remove("Pos");
		nbttagcompound.remove("Riding");

		nbttagcompound.remove("UUID");
		nbttagcompound.remove("UUIDMost");
		nbttagcompound.remove("UUIDLeast");

		if(!nbttagcompound.contains("ModRev"))
			nbttagcompound.putInt("ModRev", 1);

		if(nbttagcompound.contains("TransformRole")){
			CompoundTag adv = nbttagcompound.getCompound("TransformRole");
			adv.putInt("TransporterId", -1);
			nbttagcompound.put("TransformRole", adv);
		}

		if(nbttagcompound.contains("TransformJob")){
			CompoundTag adv = nbttagcompound.getCompound("TransformJob");
			adv.putInt("ItemGiverId", 0);
			nbttagcompound.put("TransformJob", adv);
		}
		
		if(nbttagcompound.contains("TransformAI")){
			CompoundTag adv = nbttagcompound.getCompound("TransformAI");
			adv.remove("StartPosNew");
			adv.remove("StartPos");
			adv.remove("MovingPathNew");
			nbttagcompound.put("TransformAI", adv);
		}

		if(nbttagcompound.contains("id")){
			String id = nbttagcompound.getString("id");
			if(!CustomNpcs.FixUpdateFromPre_1_12) {
				id = id.replace("customnpcs.", "customnpcs:");
			}
			nbttagcompound.putString("id", id);
		}
	}

	@Override
	public IEntity spawn(double x, double y, double z, int tab, String name, IWorld level) {
		CompoundTag compound = getCloneData(null, name, tab);
		if(compound == null)
			throw new CustomNPCsException("Unknown clone tab:" + tab + " name:"+ name);
		Entity entity = SPacketToolMobSpawner.spawnClone(compound, x, y, z, level.getMCLevel());
		if(entity == null)
			return null;
		return NpcAPI.Instance().getIEntity(entity);
	}

	@Override
	public IEntity get(int tab, String name, IWorld level) {
		CompoundTag compound = getCloneData(null, name, tab);
		if(compound == null)
			throw new CustomNPCsException("Unknown clone tab:" + tab + " name:"+ name);
		ServerCloneController.Instance.cleanTags(compound);
		Entity entity = EntityType.create(compound, level.getMCLevel()).orElse(null);
		if(entity == null)
			return null;
		return NpcAPI.Instance().getIEntity(entity);
	}

	@Override
	public void set(int tab, String name, IEntity entity) {
		CompoundTag compound = new CompoundTag();
		if(!entity.getMCEntity().saveAsPassenger(compound))
			throw new CustomNPCsException("Cannot save dead entities");
		cleanTags(compound);
		saveClone(tab, name, compound);
	}

	@Override
	public void remove(int tab, String name) {
		removeClone(name, tab);
	}

    public boolean hasClone(int tab, String name) {
		return getCloneData(null, name, tab) != null;
    }
}
