package noppes.npcs;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import noppes.npcs.controllers.NaturalSpawnCache;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.ChunkMapMixin;
import noppes.npcs.mixin.PersistentEntitySectionManagerMixin;
import noppes.npcs.mixin.ServerLevelMixin;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NPCSpawning {
	
	
    public static void findChunksForSpawning(ServerLevel level){
    	if(SpawnController.instance.data.isEmpty() || level.getGameTime() % 400L != 0L) {
    	    // Even if we skip natural spawning, check if cached NPCs need restoring
    	    if (level.getGameTime() % 100L == 0L) {
    	        NaturalSpawnCache.instance.checkNearbyPlayers(level);
    	    }
    	    return;
    	}

        EntitySectionStorage<Entity> sectionManager = ((PersistentEntitySectionManagerMixin<Entity>)((ServerLevelMixin)level).entityManager()).sectionStorage();

        ChunkMap chunkManager = level.getChunkSource().chunkMap;
        List<ChunkHolder> list = new ArrayList<>(((ChunkMapMixin)chunkManager).visibleChunkMap().values());
        Collections.shuffle(list);
        for(ChunkHolder chunkHolder : list){
            LevelChunk levelchunk = chunkHolder.getTickingChunk();
            if(levelchunk == null){
                break;
            }
            ChunkPos pos = levelchunk.getPos();
            Biome biome = level.getBiome(pos.getWorldPosition()).value();
            if(SpawnController.instance.hasSpawnList(level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome))){
                AABB bb = new AABB(pos.getMinBlockX(), 0, pos.getMinBlockZ(), pos.getMaxBlockX(), level.getMaxBuildHeight(), pos.getMaxBlockZ());
                List<Entity> entities = Lists.newArrayList();
                sectionManager.getEntities(EntityType.PLAYER, bb.inflate(4), (e)-> {entities.add(e); return AbortableIterationConsumer.Continuation.CONTINUE;});
                if(entities.isEmpty()){
                    sectionManager.getEntities(CustomEntities.entityCustomNpc, bb, (e)-> {entities.add(e); return AbortableIterationConsumer.Continuation.CONTINUE;});
                    if(entities.size() < CustomNpcs.NpcNaturalSpawningChunkLimit){
                        spawnChunk(level, levelchunk);
                    }
                }
            }

        }

//        for (int i = 0; i < level.players.size(); ++i){
//            Player entityplayer = (Player)level.players.get(i);
//            if(entityplayer.isSpectator())
//            	continue;
//            int j = Mth.floor(entityplayer.getX() / 16.0D);
//            int k = Mth.floor(entityplayer.getZ() / 16.0D);
//            byte size = 7;
//
//            for (int x = -size; x <= size; ++x){
//                for (int z = -size; z <= size; ++z){
//                	ChunkPos pos = new ChunkPos(x + j, z + k);
//                    if (!eligibleChunksForSpawning.contains(pos) && level.getWorldBorder().isWithinBounds(pos)){
//                        if (CNpcsChunkMapHelper.canSpawn(level.getChunkSource().chunkMap, pos)) {
//                            eligibleChunksForSpawning.add(pos);
//                        }
//                    }
//                }
//            }
//        }

    }

    private static void spawnChunk(ServerLevel level, LevelChunk chunk){
        BlockPos chunkposition = getChunk(level, chunk);
        int j1 = chunkposition.getX();
        int k1 = chunkposition.getY();
        int l1 = chunkposition.getZ();

        for(int i = 0; i < 3; i++){
            int x = j1;
            int y = k1;
            int z = l1;
            byte b1 = 6;

            x += level.random.nextInt(b1) - level.random.nextInt(b1);
            z += level.random.nextInt(b1) - level.random.nextInt(b1);

            BlockPos pos = new BlockPos(x, y, z);

            ResourceLocation name = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(level.getBiome(pos).value());
            SpawnData data = SpawnController.instance.getRandomSpawnData(name);

            if (data == null || data.data.isEmpty() || !canCreatureTypeSpawnAtLocation(data, level, pos))
                continue;

            spawnData(data, level, pos);
        }
    }
    
    public static int countNPCs(ServerLevel level){
        int count = 0;
        Iterable<Entity> list = level.getAllEntities();
        for (Entity entity : list){
            if (entity instanceof EntityNPCInterface){
                count++;
            }
        }
        return count;
    }


    private static BlockPos getChunk(Level level, LevelChunk chunk) {
        ChunkPos chunkpos = chunk.getPos();
        int i = chunkpos.getMinBlockX() + level.random.nextInt(16);
        int j = chunkpos.getMinBlockZ() + level.random.nextInt(16);
        int k = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
        int l = level.random.nextInt(k + 1);
        return new BlockPos(i, l, j);
    }
    
    public static void performLevelGenSpawning(ServerLevelAccessor level, Biome biome, int x, int z, RandomSource rand){
        if(biome.getMobSettings().getCreatureProbability() >= 1 || biome.getMobSettings().getCreatureProbability() < 0 || !SpawnController.instance.hasSpawnList(level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome))) {
            return;
        }
        int tries = 0;
    	while (rand.nextFloat() < biome.getMobSettings().getCreatureProbability()){
            tries++;
            if(tries > 20){
                break;
            }

    		SpawnData data = SpawnController.instance.getRandomSpawnData(level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome));
    		
    		int size = 16;
    		
            int j1 = x + rand.nextInt(size);
            int k1 = z + rand.nextInt(size);
            int l1 = j1;
            int i2 = k1;
            
            for (int k2 = 0; k2 < 4; ++k2){
                BlockPos pos = getTopNonCollidingPos(level, CustomEntities.entityCustomNpc, j1, k1);

                if (!canCreatureTypeSpawnAtLocation(data, level, pos)){
                    j1 += rand.nextInt(5) - rand.nextInt(5);

                    for (k1 += rand.nextInt(5) - rand.nextInt(5); j1 < x || j1 >= x + size || k1 < z || k1 >= z + size; k1 = i2 + rand.nextInt(5) - rand.nextInt(5))
                    {
                        j1 = l1 + rand.nextInt(5) - rand.nextInt(5);
                    }
                }
                else if(spawnData(data, level, pos))
	                break;
                
            }
        }
    }
    
    private static boolean spawnData(SpawnData data, ServerLevelAccessor level, BlockPos pos){
        Mob entityliving;

        try{
            CompoundTag nbt = data.getCompound(1);
            if(nbt == null)
                return false;
			Entity entity = EntityType.create(nbt, level.getLevel()).orElse(null);
			if(entity == null || !(entity instanceof Mob))
				return false;
			
			entityliving = (Mob) entity;
			
			if(entity instanceof EntityCustomNpc){
				EntityCustomNpc npc = (EntityCustomNpc) entity;
				npc.stats.spawnCycle = 4;
				npc.stats.respawnTime = 0;
				npc.ais.returnToStart = false;
				npc.ais.setStartPos(pos);
			}
			entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        }
        catch (Exception exception){
            exception.printStackTrace();
            return false;
        }
        if(!(entityliving.checkSpawnRules(level, MobSpawnType.NATURAL) && entityliving.checkSpawnObstruction(level)))
            return false;
//        Result canSpawn = canEntitySpawn(entityliving, level, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, null, MobSpawnType.NATURAL);
//        if (canSpawn == Result.DENY || (canSpawn == Result.DEFAULT && !entityliving.checkSpawnRules(level, MobSpawnType.NATURAL)))
//        	return false;

        level.getServer().submit(() -> {
            level.addFreshEntity(entityliving);
        });
    	
    	return true;
    }

    public static float getLightLevel(LevelReader level, BlockPos pos){
        int blockLight = level.getBrightness(LightLayer.BLOCK,pos);
        int skyLight = level.getBrightness(LightLayer.SKY,pos);
        int skyDarken = level.getSkyDarken();
        float skyLightValue = (11f-skyDarken)*15f/11f;
        return Math.max(blockLight, skyLight/15f*skyLightValue);
    }
    
    public static boolean canCreatureTypeSpawnAtLocation(SpawnData data, LevelReader level, BlockPos pos){
        if (!level.getWorldBorder().isWithinBounds(pos) || !level.noCollision(CustomEntities.entityCustomNpc.getAABB(pos.getX(), pos.getY(), pos.getZ()))){
            return false;
        }
        if(data.type == 1 && getLightLevel(level, pos) > 8 || data.type == 2 && getLightLevel(level, pos) <= 8){
        	return false;
        }
        
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (data.liquid){
            return state.liquid() && level.getBlockState(pos.below()).liquid() && !level.getBlockState(pos.above()).isRedstoneConductor(level, pos.above());
        }
        
        BlockPos blockpos1 = pos.below();
        
        BlockState state1 = level.getBlockState(blockpos1);
        Block block1 = state1.getBlock();
//        if(!state1.isSideSolid(level, blockpos1, Direction.UP))
//        	return false;
        
        boolean flag = block1 != Blocks.BEDROCK && block1 != Blocks.BARRIER;
        BlockPos down = blockpos1.below();
        flag |= level.getBlockState(down).isValidSpawn(level, down, CustomEntities.entityCustomNpc);
        return flag && !state.isSignalSource() && !state.liquid() && !level.getBlockState(pos.above()).isSignalSource();
    }


    private static BlockPos getTopNonCollidingPos(LevelReader p_208498_0_, EntityType<?> p_208498_1_, int p_208498_2_, int p_208498_3_) {
        int i = p_208498_0_.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p_208498_2_, p_208498_3_);
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(p_208498_2_, i, p_208498_3_);
        if (p_208498_0_.dimensionType().hasCeiling()) {
            do {
                blockpos$mutable.move(Direction.DOWN);
            } while(!p_208498_0_.getBlockState(blockpos$mutable).isAir());

            do {
                blockpos$mutable.move(Direction.DOWN);
            } while(p_208498_0_.getBlockState(blockpos$mutable).isAir() && blockpos$mutable.getY() > 0);
        }

        BlockPos blockpos = blockpos$mutable.below();
        if (p_208498_0_.getBlockState(blockpos).isPathfindable(p_208498_0_, blockpos, PathComputationType.LAND)) {
            return blockpos;
        }

        return blockpos$mutable.immutable();
    }
}

