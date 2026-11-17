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
    /**
     * Weakly keyed on the npc itself.
     *
     * This used to be keyed by entity id, which is never reused, and only ever written to
     * from {@link EntityNPCInterface#remove(Entity.RemovalReason)} / {@code delete()} - neither
     * of which runs when vanilla unloads an npc with its chunk (that path calls the final
     * {@code Entity.setRemoved} directly). Every load/unload cycle therefore left a permanent
     * entry holding a hard reference to the npc and everything it owns (script container,
     * script engine, ...). Keying weakly means the entry drops out as soon as the npc is
     * collected; the value is only a boolean so it cannot keep the key alive.
     */
    private final Set<EntityNPCInterface> trackedEntityHashTable =
            Collections.newSetFromMap(new WeakHashMap<EntityNPCInterface, Boolean>());


    public VisibilityController(){
    }
    
    public void trackNpc(EntityNPCInterface npc) {
        if(npc.isClientSide()){
            return;
        }
    	boolean hasOptions = npc.display.availability.hasOptions();
        if((hasOptions || npc.display.getVisible() != 0) && !trackedEntityHashTable.contains(npc)) {
            trackedEntityHashTable.add(npc);
        }
        if(!hasOptions && npc.display.getVisible() == 0 && trackedEntityHashTable.contains(npc)) {
            trackedEntityHashTable.remove(npc);
        }
    }

    public void remove(EntityNPCInterface npc){
        if(npc.isClientSide()){
            return;
        }
        trackedEntityHashTable.remove(npc);
    }

    public void onUpdate(ServerPlayer player){
        if(!CustomNpcs.EnableInvisibleNpcs){
            return;
        }
        Iterator<EntityNPCInterface> it = trackedEntityHashTable.iterator();
        while(it.hasNext()){
            EntityNPCInterface npc = it.next();
            // Unloaded / killed / discarded npcs can linger here until the gc collects them
            // (the weak key only drops the entry once nothing else references the entity).
            if(npc == null || npc.isRemoved()){
                continue;
            }
            checkIsVisible(npc, player);
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

    /**
     * Number of tracked npcs, for debugging/diagnostics.
     */
    public int size(){
        return trackedEntityHashTable.size();
    }
}
