package noppes.npcs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@Mixin(PoseStack.class)
public interface MatrixStackMixin {

    @Accessor(value="poseStack")
    Deque<PoseStack.Pose> getStack();
}