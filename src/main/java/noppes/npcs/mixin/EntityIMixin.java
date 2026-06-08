package noppes.npcs.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityIMixin {

    @Accessor(value="removalReason")
    Entity.RemovalReason removal();

    @Accessor(value="removalReason")
    void removal(Entity.RemovalReason i);

    @Accessor
    void setLevel(Level level);
}