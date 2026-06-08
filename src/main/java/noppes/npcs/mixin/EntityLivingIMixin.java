package noppes.npcs.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import noppes.npcs.controllers.data.MarkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface EntityLivingIMixin {

    @Accessor(value="jumping")
    boolean jumping();

    @Accessor(value="useItemRemaining")
    void useItemRemaining(int i);

    @Accessor(value="animStep")
    float animStep();

    @Accessor(value="animStep")
    void animStep(float i);

    @Accessor(value="animStepO")
    float animStepO();

    @Accessor(value="animStepO")
    void animStepO(float i);

    @Accessor(value="swimAmount")
    float swimAmount();

    @Accessor(value="swimAmount")
    void swimAmount(float i);

    @Accessor(value="swimAmountO")
    float swimAmountO();

    @Accessor(value="swimAmountO")
    void swimAmountO(float i);

    @Accessor(value="lastHurtByPlayerTime")
    int lastHurtByPlayerTime();

    @Accessor(value="lastHurtByPlayerTime")
    void lastHurtByPlayerTime(int i);
}