package noppes.npcs.controllers.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import noppes.npcs.controllers.ServerCloneController;

import java.util.HashMap;
import java.util.Map;

public class CloneSpawnData {
    public int tab;
    public String name;

    private long lastLoaded;
    private CompoundTag compound;

    public CloneSpawnData(int tab, String name){
        this.name = name;
        this.tab = tab;
    }

    public CompoundTag getCompound(){
        if(lastLoaded < ServerCloneController.Instance.lastLoaded){
            compound = ServerCloneController.Instance.getCloneData(null, name, tab);
            lastLoaded = ServerCloneController.Instance.lastLoaded;
        }
        return compound;
    }

    public static Map<Integer, CloneSpawnData> load(ListTag list){
        Map<Integer, CloneSpawnData> data = new HashMap<>();
        for(int i = 0; i < list.size(); i++){
            CompoundTag c = list.getCompound(i);
            int tab = c.getInt("tab");
            String name = c.getString("name");
            if(ServerCloneController.Instance == null || ServerCloneController.Instance.hasClone(tab, name)){
                data.put(c.getInt("slot"), new CloneSpawnData(tab, name));
            }
        }
        return data;
    }

    public static ListTag save(Map<Integer, CloneSpawnData> data){
        ListTag list = new ListTag();
        for(Map.Entry<Integer, CloneSpawnData> entry : data.entrySet()){
            if(ServerCloneController.Instance != null && !ServerCloneController.Instance.hasClone(entry.getValue().tab, entry.getValue().name)){
                continue;
            }
            CompoundTag c = new CompoundTag();
            c.putInt("slot", entry.getKey());
            c.putInt("tab", entry.getValue().tab);
            c.putString("name", entry.getValue().name);
            list.add(c);
        }
        return list;
    }
}
