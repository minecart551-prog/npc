package noppes.npcs.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ChunkMapMixin {

    @Accessor(value="visibleChunkMap")
    Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap();

    @Accessor(value="playerMap")
    PlayerMap playerMap();

}