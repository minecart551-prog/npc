package noppes.npcs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingRenderer3Mixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayerParent<T, M> {
    @Invoker
    void callScale(T p_115314_, PoseStack p_115315_, float p_115316_);

    @Invoker
    float callGetBob(T p_115305_, float p_115306_);
}
