package noppes.npcs.client.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.ModelHeadwear;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.shared.client.model.Model2DRenderer;

public class LayerHeadwear extends LayerInterface implements LayerPreRender{
	private final ModelHeadwear headwear = new ModelHeadwear();
	
	public LayerHeadwear(LivingEntityRenderer render) {
		super(render);
	}
	@Override
	public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
		if(CustomNpcs.HeadWearType != 1 || npc.textureLocation == null)
			return;
		float red = 1, blue = 1, green = 1;
    	if(npc.hurtTime <= 0 && npc.deathTime <= 0){
    		int color = npc.display.getTint();
        	red = (color >> 16 & 255) / 255f;
        	green = (color >> 8  & 255) / 255f;
        	blue = (color & 255) / 255f;
    	}
		base.head.translateAndRotate(mStack);
		Model2DRenderer.textureOverride = npc.textureLocation;
		VertexConsumer ivertex = typeBuffer.getBuffer(RenderType.entityTranslucent(npc.textureLocation));
		int m = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(npc.hurtTime > 0 || npc.deathTime > 0));
		headwear.render(mStack, ivertex, lightmapUV, m, red, green, blue, alpha());
		Model2DRenderer.textureOverride = null;
	}


	@Override
	public void rotate(PoseStack matrixStack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void preRender(EntityCustomNpc npc) {
		base.hat.visible = base.head.visible && CustomNpcs.HeadWearType != 1;
		if(!base.hat.visible){
			headwear.config =  null;
		}
	}

}
