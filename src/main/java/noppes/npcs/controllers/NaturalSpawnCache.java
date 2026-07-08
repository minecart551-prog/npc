package noppes.npcs.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

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
     * Called when a spawnCycle 4 NPC is removed from the world.
     * Saves its NBT data so it can be restored later.
     */
    public void cacheNpc(EntityNPCInterface npc) {
        if (npc.level().isClientSide || npc.stats.spawnCycle != 4)
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
                npc.level().dimension()
        );

        System.out.println("[NaturalSpawnCache] Caching NPC " + data.debugName + " at " + data.position + " (cache size: " + cache.size() + ")");
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
                // Ensure spawnCycle stays as 4
                npc.stats.spawnCycle = 4;
                npc.stats.respawnTime = 0;
                npc.ais.returnToStart = false;
                npc.ais.setStartPos(data.position);
                npc.moveTo(data.position.getX() + 0.5, data.position.getY(), data.position.getZ() + 0.5,
                        world.random.nextFloat() * 360.0F, 0.0F);

                world.addFreshEntity(npc);
                System.out.println("[NaturalSpawnCache] Restored NPC " + data.debugName + " at " + data.position);
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

        int viewDistance = 7; // chunks
        List<Integer> toRemove = new ArrayList<>();

        synchronized (cache) {
            for (Map.Entry<Integer, CachedNpcData> entry : cache.entrySet()) {
                CachedNpcData data = entry.getValue();
                if (!data.dimension.equals(world.dimension()))
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
    public void clearDimension(ResourceKey<Level> dimension) {
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

    public static class CachedNpcData {
        public final CompoundTag nbt;
        public final BlockPos position;
        public final int idHash;
        public final String debugName;
        public final ResourceKey<Level> dimension;

        public CachedNpcData(CompoundTag nbt, BlockPos position, int idHash, String debugName, ResourceKey<Level> dimension) {
            this.nbt = nbt;
            this.position = position;
            this.idHash = idHash;
            this.debugName = debugName;
            this.dimension = dimension;
        }
    }
}