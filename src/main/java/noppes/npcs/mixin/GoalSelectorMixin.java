package noppes.npcs.mixin;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumSet;
import java.util.Map;

@Mixin(GoalSelector.class)
public interface GoalSelectorMixin {

    @Accessor(value="lockedFlags")
    Map<Goal.Flag, WrappedGoal> lockedFlags();

    @Accessor(value="disabledFlags")
    EnumSet<Goal.Flag> disabledFlags();
}
