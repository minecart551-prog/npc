package noppes.npcs.mixin;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ParticleEngine.class)
public interface ParticleManagerMixin {


    @Accessor(value="spriteSets")
    Map<ResourceLocation, Object> getPacks();

}
