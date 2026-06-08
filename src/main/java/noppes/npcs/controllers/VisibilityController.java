package noppes.npcs.controllers;

import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class VisibilityController {
    public static VisibilityController instance = new VisibilityController();
    private Map<Integer, EntityNPCInterface> trackedEntityHashTable = new ConcurrentHashMap<Integer, EntityNPCInterface>();


    public VisibilityController(){
    	trackedEntityHashTable = new TreeMap<Integer, EntityNPCInterface>();
    }
    
    public void trackNpc(EntityNPCInterface npc) {
        if(npc.isClientSide()){
            return;
        }
    	boolean hasOptions = npc.display.availability.hasOptions();
        if((hasOptions || npc.display.getVisible() != 0) && !trackedEntityHashTable.containsKey(npc.getId())) {
            trackedEntityHashTable.put(npc.getId(), npc);
        }
        if(!hasOptions && npc.display.getVisible() == 0 && trackedEntityHashTable.containsKey(npc.getId())) {
            trackedEntityHashTable.remove(npc.getId());
        }
    }

    public void remove(EntityNPCInterface npc){
        if(npc.isClientSide()){
            return;
        }
        trackedEntityHashTable.remove(npc.getId());
    }

    public void onUpdate(ServerPlayer player){
        if(!CustomNpcs.EnableInvisibleNpcs){
            return;
        }
        for(Map.Entry<Integer, EntityNPCInterface> entry : trackedEntityHashTable.entrySet()){
            checkIsVisible(entry.getValue(), player);
        }
    }

    public static void checkIsVisible(EntityNPCInterface npc, ServerPlayer playerMP){
        if(!CustomNpcs.EnableInvisibleNpcs){
            return;
        }
        if(npc.display.isVisibleTo(playerMP) || playerMP.isSpectator() || playerMP.getMainHandItem().getItem() == CustomItems.wand){
            npc.setVisible(playerMP);
        }
        else{
            npc.setInvisible(playerMP);
        }
    }    
    

    public static void addValue(HashMap<Integer, ArrayList<EntityNPCInterface>> map, int id, EntityNPCInterface npc){
        if(!map.containsKey(id)){
            map.put(id, new ArrayList<EntityNPCInterface>());
        }
        ArrayList<EntityNPCInterface> npcs=map.get(id);
        if(!npcs.contains(npc)){
            npcs.add(npc);
            map.replace(id, npcs);
        }
    }
}
