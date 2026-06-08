package noppes.npcs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

@Environment(EnvType.CLIENT)
public class LayerGlow<T extends EntityNPCInterface, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public LayerGlow(RenderCustomNpc npcRenderer) {
        super(npcRenderer);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource typeBuffer, int packedLightIn, T npc, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (npc.display.getOverlayTexture().isEmpty())
            return;
        if (npc.textureGlowLocation == null) {
            npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
        }
        VertexConsumer ivertexbuilder = null;
        if(npc.display.isOverlayGlowing()) {
            ivertexbuilder = typeBuffer.getBuffer(RenderType.entityTranslucentEmissive(npc.textureGlowLocation));
        }else{
            ivertexbuilder = typeBuffer.getBuffer(RenderType.entityTranslucent(npc.textureGlowLocation));
        }
        this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, LivingEntityRenderer.getOverlayCoords(npc, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);

//        this.renderer.bindForSetup(npc.textureGlowLocation);
//        RenderSystem.enableBlend();
//        //RenderSystem.disableAlphaTest();
//        RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//        RenderSystem.disableLighting();
//        RenderSystem.depthFunc(GL11.GL_EQUAL);
//        char c0 = 61680;
//        int i = c0 % 65536;
//        int j = c0 / 65536;
//        OpenGlHelper.glMultiTexCoord2f(OpenGlHelper.GL_TEXTURE1, (float)i, (float)j);
//        RenderSystem.enableLighting();
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        this.renderer.getModel().render(matrixStackIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//        this.renderer.setLightmap(npc);
//        RenderSystem.disableBlend();
//        RenderSystem.enableAlphaTest();
//        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }
}