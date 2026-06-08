package noppes.npcs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LayerNpcCloak extends LayerInterface{


	public LayerNpcCloak(LivingEntityRenderer render) {
		super(render);
	}

	@Override
    public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
		if(npc.textureCloakLocation == null){
			if(npc.display.getCapeTexture() == null || npc.display.getCapeTexture().isEmpty() || !(base instanceof PlayerModel))
				return;
			npc.textureCloakLocation = new ResourceLocation(npc.display.getCapeTexture());
		}

        mStack.pushPose();
        mStack.translate(0.0D, 0.0D, 0.125D);
        double d0 = Mth.lerp((double)partialTicks, npc.prevChasingPosX, npc.chasingPosX) - Mth.lerp((double)partialTicks, npc.xo, npc.getX());
        double d1 = Mth.lerp((double)partialTicks, npc.prevChasingPosY, npc.chasingPosY) - Mth.lerp((double)partialTicks, npc.yo, npc.getY());
        double d2 = Mth.lerp((double)partialTicks, npc.prevChasingPosZ, npc.chasingPosZ) - Mth.lerp((double)partialTicks, npc.zo, npc.getZ());
        float f = npc.yBodyRotO + (npc.yBodyRot - npc.yBodyRotO);
        double d3 = (double)Mth.sin(f * ((float)Math.PI / 180F));
        double d4 = (double)(-Mth.cos(f * ((float)Math.PI / 180F)));
        float f1 = (float)d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);
        float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);
        float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);
        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        f1 = f1 + Mth.sin(Mth.lerp(partialTicks, npc.walkDistO, npc.walkDist) * 6.0F) * 32.0F * partialTicks;
        if (npc.isCrouching()) {
            f1 += 25.0F;
        }

        mStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        mStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
        mStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
        VertexConsumer ivertexbuilder = typeBuffer.getBuffer(RenderType.entityTranslucent(npc.textureCloakLocation));


        ((PlayerModel) base).renderCloak(mStack, ivertexbuilder, lightmapUV, OverlayTexture.NO_OVERLAY);
        mStack.popPose();

	}

	@Override
	public void rotate(PoseStack matrixStack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
