package noppes.npcs.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.shared.common.util.LogWriter;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores NPCs that were removed from the world because they were far from players.
 * Restores them when a player comes near.
 * Max capacity to prevent memory leaks.
 */
public class NaturalSpawnCache {

    public static NaturalSpawnCache instance = new NaturalSpawnCache();

    private static final int MAX_CACHE_SIZE = 1000;
    // NPCs that have been "despawned" and need restoration
    private final Map<Integer, CachedNpcData> cache = Collections.synchronizedMap(new LinkedHashMap<Integer, CachedNpcData>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, CachedNpcData> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    });

    private NaturalSpawnCache() {}

    /**
     * Called when a spawnCycle 3/4 NPC is removed from the world.
     * Saves its NBT data so it can be restored later.
     */
    public void cacheNpc(EntityNPCInterface npc) {
        if (npc.level().isClientSide || (npc.stats.spawnCycle != 3 && npc.stats.spawnCycle != 4))
            return;

        CompoundTag nbt = new CompoundTag();
        if (!npc.saveAsPassenger(nbt))
            return;

        // Remove temporary flags that would cause issues
        nbt.remove("UUID");
        nbt.remove("Pos");
        nbt.remove("Dimension");

        CachedNpcData data = new CachedNpcData(
                nbt,
                npc.blockPosition(),
                npc.getUUID().hashCode(),
                npc.getDisplayName().getString(),
                npc.level().dimension().location().toString(),
                npc.stats.spawnCycle
        );

        cache.put(data.idHash, data);
    }

    /**
     * Restores an NPC in the given world from cached data.
     */
    public void restoreNpc(CachedNpcData data, ServerLevel world) {
        try {
            CompoundTag nbt = data.nbt.copy();

            // Set spawn position
            nbt.putDouble("X", data.position.getX() + 0.5);
            nbt.putDouble("Y", data.position.getY());
            nbt.putDouble("Z", data.position.getZ() + 0.5);

            Entity entity = EntityType.create(nbt, world).orElse(null);
            if (entity instanceof EntityCustomNpc) {
                EntityCustomNpc npc = (EntityCustomNpc) entity;
                // Preserve original spawn cycle
                npc.stats.spawnCycle = data.spawnCycle;
                npc.stats.respawnTime = 0;
                npc.ais.returnToStart = false;
                npc.ais.setStartPos(data.position);
                npc.moveTo(data.position.getX() + 0.5, data.position.getY(), data.position.getZ() + 0.5,
                        world.random.nextFloat() * 360.0F, 0.0F);

                world.addFreshEntity(npc);
            }
        } catch (Exception e) {
            System.err.println("[NaturalSpawnCache] Failed to restore NPC " + data.debugName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if any cached NPCs are near the player and restores them.
     * Called periodically (every 100 ticks).
     */
    public void checkNearbyPlayers(ServerLevel world) {
        if (cache.isEmpty())
            return;

        String worldDim = world.dimension().location().toString();
        int viewDistance = 7; // chunks
        List<Integer> toRemove = new ArrayList<>();

        synchronized (cache) {
            for (Map.Entry<Integer, CachedNpcData> entry : cache.entrySet()) {
                CachedNpcData data = entry.getValue();
                if (!data.dimension.equals(worldDim))
                    continue;

                // Check if any player is near the NPC's position
                BlockPos npcPos = data.position;
                boolean playerNearby = !world.getPlayers(player -> {
                    BlockPos playerPos = player.blockPosition();
                    int dx = Math.abs(playerPos.getX() - npcPos.getX());
                    int dz = Math.abs(playerPos.getZ() - npcPos.getZ());
                    return dx < viewDistance * 16 && dz < viewDistance * 16;
                }).isEmpty();

                if (playerNearby) {
                    toRemove.add(entry.getKey());
                }
            }
        }

        // Restore NPCs outside the synchronized block to prevent deadlock
        for (int hash : toRemove) {
            CachedNpcData data = cache.remove(hash);
            if (data != null) {
                restoreNpc(data, world);
            }
        }
    }

    /**
     * Removes all cached NPCs for a given dimension (for cleanup).
     */
    public void clearDimension(String dimension) {
        synchronized (cache) {
            cache.entrySet().removeIf(entry -> entry.getValue().dimension.equals(dimension));
        }
    }

    /**
     * Removes a specific cached NPC.
     */
    public void removeNpc(int idHash) {
        cache.remove(idHash);
    }

    public int getCacheSize() {
        return cache.size();
    }

    // Returns the cache file path
    private File getCacheFile() {
        return new File(CustomNpcs.getLevelSaveDirectory(), "natural_spawn_cache.json");
    }

    /**
     * Saves cached NPC data to disk so it survives server restarts.
     */
    public void save() {
        if (cache.isEmpty())
            return;
        File file = getCacheFile();
        file.getParentFile().mkdirs();
        CompoundTag compound = new CompoundTag();
        ListTag list = new ListTag();
        synchronized (cache) {
            for (CachedNpcData data : cache.values()) {
                CompoundTag entry = new CompoundTag();
                entry.put("NBT", data.nbt);
                entry.putInt("X", data.position.getX());
                entry.putInt("Y", data.position.getY());
                entry.putInt("Z", data.position.getZ());
                entry.putInt("IdHash", data.idHash);
                entry.putString("Name", data.debugName);
                entry.putString("Dim", data.dimension);
                entry.putInt("SpawnCycle", data.spawnCycle);
                list.add(entry);
            }
        }
        compound.put("CachedNpcs", list);
        try {
            NBTJsonUtil.SaveFile(file, compound);
        } catch (Exception e) {
            LogWriter.error("[NaturalSpawnCache] Failed to save: ", e);
        }
    }

    /**
     * Loads cached NPC data from disk after server restart and restores them.
     */
    public void loadAndRestore(ServerLevel level) {
        File file = getCacheFile();
        if (!file.exists())
            return;
        try {
            CompoundTag compound = NBTJsonUtil.LoadFile(file);
            ListTag list = compound.getList("CachedNpcs", 10);
            String worldDim = level.dimension().location().toString();
            List<CachedNpcData> toRestore = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                String dim = entry.getString("Dim");
                if (!dim.equals(worldDim))
                    continue;
                CachedNpcData data = new CachedNpcData(
                        entry.getCompound("NBT"),
                        new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")),
                        entry.getInt("IdHash"),
                        entry.getString("Name"),
                        dim,
                        entry.getInt("SpawnCycle")
                );
                toRestore.add(data);
            }
            System.out.println("[NaturalSpawnCache] Restoring " + toRestore.size() + " cached NPCs...");
            for (CachedNpcData data : toRestore) {
                restoreNpc(data, level);
            }
            // Delete cache file after restoring
            file.delete();
        } catch (Exception e) {
            LogWriter.error("[NaturalSpawnCache] Failed to load: ", e);
        }
    }

    public static class CachedNpcData {
        public final CompoundTag nbt;
        public final BlockPos position;
        public final int idHash;
        public final String debugName;
        public final String dimension;

        public final int spawnCycle;

        public CachedNpcData(CompoundTag nbt, BlockPos position, int idHash, String debugName, String dimension, int spawnCycle) {
            this.nbt = nbt;
            this.position = position;
            this.idHash = idHash;
            this.debugName = debugName;
            this.dimension = dimension;
            this.spawnCycle = spawnCycle;
        }
    }
}