package noppes.npcs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.shared.client.model.Model2DRenderer;



public class MarkRenderer {
	public static final ResourceLocation markExclamation = new ResourceLocation("customnpcs", "textures/marks/exclamation.png");
	public static final ResourceLocation markQuestion = new ResourceLocation("customnpcs", "textures/marks/question.png");
	public static final ResourceLocation markPointer = new ResourceLocation("customnpcs", "textures/marks/pointer.png");
	public static final ResourceLocation markCross = new ResourceLocation("customnpcs", "textures/marks/cross.png");
	public static final ResourceLocation markSkull = new ResourceLocation("customnpcs", "textures/marks/skull.png");
	public static final ResourceLocation markStar = new ResourceLocation("customnpcs", "textures/marks/star.png");
	
	public static int displayList = -1;
	public static Model2DRenderer renderer = new Model2DRenderer(32, 32, 0, 0, 32, 32, markExclamation);

	public static void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Mark mark){
		PoseStack matrixStack = poseStack;
		matrixStack.pushPose();

		int color = mark.color;
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;

		ResourceLocation location = markExclamation;
		if(mark.type == MarkType.QUESTION)
			location = markQuestion;
		else if(mark.type == MarkType.POINTER)
			location = markPointer;
		else if(mark.type == MarkType.CROSS)
			location = markCross;
		else if(mark.type == MarkType.SKULL)
			location = markSkull;
		else if(mark.type == MarkType.STAR)
			location = markStar;

		matrixStack.translate(0, entity.getBbHeight() + 0.6, 0);
		matrixStack.mulPose(Axis.XN.rotationDegrees(180));
		matrixStack.mulPose(Axis.YP.rotationDegrees(entity.yHeadRot));
		matrixStack.translate(-0.5f, 0, 0);
		renderer.render(location, matrixStack, buffer.getBuffer(RenderType.entityCutout(location)), packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, 1);
		matrixStack.popPose();
	}
	
//	public static void render(LivingEntity entity, double x, double y, double z, Mark mark){
//		Minecraft mc = Minecraft.getInstance();
//		RenderSystem.pushMatrix();
//        int color = mark.color;
//        float red = (float)(color >> 16 & 255) / 255.0F;
//        float blue = (float)(color >> 8 & 255) / 255.0F;
//        float green = (float)(color & 255) / 255.0F;
//        RenderSystem.setShaderColor(red, blue, green, 1);
//		RenderSystem.translated(x, y + entity.getBbHeight() + 0.6, z);
//		RenderSystem.rotatef(-entity.yHeadRot, 0, 1, 0);
//
//		if(mark.type == MarkType.EXCLAMATION)
//			Minecraft.getInstance().getTextureManager().bindForSetup(markExclamation);
//		else if(mark.type == MarkType.QUESTION)
//			Minecraft.getInstance().getTextureManager().bindForSetup(markQuestion);
//		else if(mark.type == MarkType.POINTER)
//			Minecraft.getInstance().getTextureManager().bindForSetup(markPointer);
//		else if(mark.type == MarkType.CROSS)
//			Minecraft.getInstance().getTextureManager().bindForSetup(markCross);
//		else if(mark.type == MarkType.SKULL)
//			Minecraft.getInstance().getTextureManager().bindForSetup(markSkull);
//		else if(mark.type == MarkType.STAR)
//			Minecraft.getInstance().getTextureManager().bindForSetup(markStar);
//
//		if(displayList >= 0){
//			GL11.glCallList(displayList);
//		}
//		else{
//			displayList = GL11.glGenLists(1);
//			GL11.glNewList(displayList, GL11.GL_COMPILE);
//			RenderSystem.translatef(-0.5f, 0, 0);
//	        Model2DRenderer.renderItemIn2D(Tesselator.getInstance().getBuilder(), 0f, 0f, 1f, 1f, 32, 32, 0.0625F);
//			GL11.glEndList();
//		}
//		RenderSystem.popMatrix();
//	}
}
