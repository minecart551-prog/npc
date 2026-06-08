package noppes.npcs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerMixin {

    @Invoker
    void callSetNextSpawnData(Level p_151325_, BlockPos p_151326_, SpawnData p_151327_);
}
