package noppes.npcs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableListModel.class)
public class AgeableModelMixin<T extends EntityNPCInterface> {

    private boolean isCanceled = false;
    @Inject(at = @At("HEAD"), method = "renderToBuffer", cancellable = true)
    private void renderToBuffer(PoseStack stack, VertexConsumer builder, int light, int overlay, float r, float g, float b, float a, CallbackInfo callbackInfo) {
        if(!isCanceled && RenderNPCInterface.currentNpc != null && RenderNPCInterface.currentNpc.display.getTint() < 0xFFFFFF){
            isCanceled = true;
            int color = RenderNPCInterface.currentNpc.display.getTint();
            r = (color >> 16 & 255) / 255f;
            g = (color >> 8  & 255) / 255f;
            b = (color & 255) / 255f;
            AgeableListModel model = (AgeableListModel)(Object)this;
            model.renderToBuffer(stack, builder, light, overlay, r, g, b, a);
            callbackInfo.cancel();
            return;
        }
        isCanceled = false;
    }
}