package noppes.npcs.client.renderer;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.MatrixStackMixin;
import noppes.npcs.shared.client.util.ImageDownloadAlt;
import noppes.npcs.shared.client.util.ResourceDownloader;
import noppes.npcs.shared.common.util.LogWriter;
import org.joml.Matrix4f;

import java.io.File;
import java.security.MessageDigest;
import java.util.Map;


public class RenderNPCInterface<T extends EntityNPCInterface, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    public static int LastTextureTick;
	public static EntityNPCInterface currentNpc;

	public RenderNPCInterface(EntityRendererProvider.Context manager, M model, float f){
        super(manager, model, f);
    }
    
	@Override
	public void renderNameTag(T npc, Component text, PoseStack matrixStack, MultiBufferSource buffer, int light) {
		if (npc == null || !this.shouldShowName(npc) || entityRenderDispatcher == null) {
			return;
		}

        double d0 = entityRenderDispatcher.distanceToSqr(npc);

        if (d0 > 8 * 8 * 8){
        	return;
        }
		matrixStack.pushPose();
		Vec3 renderOffset = getRenderOffset(npc, 0);
		matrixStack.translate(-renderOffset.x(),-renderOffset.y(),-renderOffset.z());
		if (npc.messages != null){
			float height = ((npc.baseSize.height / 5f) * npc.display.getSize());
			float offset = npc.getBbHeight() * (1.2f + (!npc.display.showName()?0:npc.display.getTitle().isEmpty()?0.15f:0.25f));
			matrixStack.translate(0, offset, 0);
			npc.messages.renderMessages(matrixStack, buffer, 0.666667F * height, npc.isInRange(entityRenderDispatcher.camera.getEntity(), 4), light);
			matrixStack.translate(0, -offset, 0);
		}
		if (npc.display.showName()) {
			renderLivingLabel(npc, matrixStack, buffer, light);
		}
		matrixStack.popPose();
	}
    
    protected void renderLivingLabel(T npc, PoseStack matrixStack, MultiBufferSource buffer, int light){
		float scale = (npc.baseSize.height / 5f) * npc.display.getSize();
		float height = npc.getBbHeight() - 0.06f * scale;

		matrixStack.pushPose();
		Font fontrenderer = this.getFont();

        float f2 = 0.01666667F * scale;

		matrixStack.translate(0, height, 0);
		matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        int color = npc.getFaction().color;
		matrixStack.translate(0, scale / 6.5f * 2, 0);
		float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
		int j = (int)(f1 * 255.0F) << 24;

		matrixStack.scale(-f2, -f2, f2);
		Matrix4f matrix4f = matrixStack.last().pose();
		float y = 0;
		boolean nearby = npc.isInRange(entityRenderDispatcher.camera.getEntity(), 8);
		if(!npc.display.getTitle().isEmpty() && nearby) {
			Component title = Component.literal("<").append(Component.translatable(npc.display.getTitle())).append(">");
            float f3 = 0.6f;
			matrixStack.translate(0, 4f, 0);
			matrixStack.scale(f3, f3, f3);
			fontrenderer.drawInBatch(title, -fontrenderer.width(title) / 2, 0, color, false, matrix4f, buffer, Font.DisplayMode.NORMAL, j, light);
			matrixStack.scale(1 / f3, 1 / f3, 1 / f3);
			y = -10;
        }
		Component name = npc.getName();
		fontrenderer.drawInBatch(name, -fontrenderer.width(name) / 2, y, color, false, matrix4f, buffer, nearby ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, light);
        if(nearby){
			fontrenderer.drawInBatch(name, -fontrenderer.width(name) / 2, y, color, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, light);
        }
		matrixStack.popPose();
    }
    
    protected void renderColor(EntityNPCInterface npc){
    	if(npc.hurtTime <= 0 && npc.deathTime <= 0){
	        float red = (float)(npc.display.getTint() >> 16 & 255) / 255.0F;
	        float green = (float)(npc.display.getTint() >> 8 & 255) / 255.0F;
	        float blue = (float)(npc.display.getTint() & 255) / 255.0F;
	        RenderSystem.setShaderColor(red, green, blue, 1);
    	}
    }
    
    @Override
    protected void setupRotations(T npc, PoseStack matrixScale, float f, float f1, float f2){
        if(npc.isAlive() && npc.isSleeping())
        {
			matrixScale.mulPose(Axis.YP.rotationDegrees(npc.ais.orientation));
			matrixScale.mulPose(Axis.ZP.rotationDegrees(getFlipDegrees(npc)));
			matrixScale.mulPose(Axis.YP.rotationDegrees(270F));
        } 
        else if(npc.isAlive() && npc.currentAnimation == AnimationType.CRAWL){
			matrixScale.mulPose(Axis.YP.rotationDegrees(270.0F - f1));
            float scale = ((EntityCustomNpc)npc).display.getSize() / 5f;
			matrixScale.translate(-scale +((EntityCustomNpc)npc).modelData.getLegsY() * scale, 0.14f, 0);
			matrixScale.mulPose(Axis.ZP.rotationDegrees(270F));
			matrixScale.mulPose(Axis.YP.rotationDegrees(270F));
        }
        else {
        	super.setupRotations(npc, matrixScale, f, f1, f2);
        }
    }

    @Override
    protected void scale(T npc, PoseStack matrixScale, float f){
    	renderColor(npc);
    	int size = npc.display.getSize();
		matrixScale.scale((npc.scaleX / 5) * size, (npc.scaleY / 5) * size, (npc.scaleZ / 5) * size);
    }
    
    @Override
	public void render(T npc, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		if(npc.isKilled()){
			this.shadowRadius = 0;
		}
    	if(npc.isKilled() && npc.stats.hideKilledBody && npc.deathTime > 20){
    		return;
    	}

		float xOffset = 0;
		float yOffset = npc.currentAnimation == AnimationType.NONE ?npc.ais.bodyOffsetY / 10 - 0.5f:0;
		float zOffset = 0;

		if(npc.isAlive()){
			if(npc.isSleeping()){
				xOffset = (float) -Math.cos(Math.toRadians(180 - npc.ais.orientation));
				zOffset = (float) -Math.sin(Math.toRadians(npc.ais.orientation));
				yOffset += 0.14f;
			}
			else if(npc.currentAnimation == AnimationType.SIT || npc.isPassenger()){
				yOffset -= 0.5f - ((EntityCustomNpc)npc).modelData.getLegsY() * 0.8f;
			}
		}
		xOffset = (xOffset/ 5f) * npc.display.getSize();
		yOffset = (yOffset/ 5f) * npc.display.getSize();
		zOffset = (zOffset/ 5f) * npc.display.getSize();

    	if((npc.display.getBossbar() == 1 || npc.display.getBossbar() == 2 && npc.isAttacking()) && !npc.isKilled() && npc.deathTime <= 20 && npc.canNpcSee(Minecraft.getInstance().player)){

    		//BossStatus.setBossStatus(npc, true);
    	}

    	if(npc.ais.getStandingType() == 3 && !npc.isWalking() && !npc.isInteracting()){
    		npc.yBodyRotO = npc.yBodyRot = npc.ais.orientation;
    	}
		this.shadowRadius = npc.getBbWidth() * 0.8f;
		int stackSize = ((MatrixStackMixin)matrixStack).getStack().size();
		try {
			currentNpc = npc;
			super.render(npc, entityYaw, partialTicks, matrixStack, buffer, packedLight);
		}
		catch(Throwable e) {
			while(((MatrixStackMixin)matrixStack).getStack().size() > stackSize){
				matrixStack.popPose();
			}
			LogWriter.except(e);

		}
		finally{
			currentNpc = null;
		}
	}

//    @Override
//    protected void renderModel(T npc, float limbSwingAmount, float par3, float par4, float par5, float par6, float par7){
//    	super.renderModel(npc, limbSwingAmount, par3, par4, par5, par6, par7);
//    	 if (!npc.display.getOverlayTexture().isEmpty()){
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
//        	if(npc.textureGlowLocation == null){
//        		npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
//        	}
//        	bind((ResourceLocation) npc.textureGlowLocation);
//        	float f1 = 1.0F;
//            RenderSystem.enableBlend();
//            RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//            RenderSystem.disableLighting();
//            if (npc.isInvisible())
//            {
//                RenderSystem.depthMask(false);
//            }
//            else
//            {
//                RenderSystem.depthMask(true);
//            }
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            RenderSystem.pushMatrix();
//            RenderSystem.scalef(1.001f, 1.001f, 1.001f);
//	        model.render(npc, limbSwingAmount, par3, par4, par5, par6, par7);
//            RenderSystem.popMatrix();
//            RenderSystem.enableLighting();
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f1);
//
//
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
//            RenderSystem.disableBlend();
//         }
//    }
    
    @Override
    protected float getBob(T npc, float limbSwingAmount){
    	if(npc.isKilled() || !npc.display.getHasLivingAnimation())
    		return 0;
    	return super.getBob(npc, limbSwingAmount);
    }
    
	@Override
	public ResourceLocation getTextureLocation(T npc) {
		if(npc.textureLocation == null){
			if(npc.display.skinType == 0)// normal skin
				npc.textureLocation = new ResourceLocation(npc.display.getSkinTexture());
			else if(LastTextureTick < 5){ //fixes request flood somewhat
				return DefaultPlayerSkin.getDefaultSkin();
			}
			else if(npc.display.skinType == 1 && npc.display.playerProfile != null){ //player skin
	            Minecraft minecraft = Minecraft.getInstance();
	            Map map = minecraft.getSkinManager().getInsecureSkinInformation(npc.display.playerProfile);
	
	            if (map.containsKey(Type.SKIN)){
	            	npc.textureLocation = minecraft.getSkinManager().registerTexture((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
	            }else{
					npc.textureLocation = DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(npc.display.playerProfile));
				}
			}
			else if(npc.display.skinType == 2 && !npc.display.getSkinUrl().isEmpty()){ // url skin
				try{
					boolean fixSkin = npc instanceof EntityCustomNpc && ((EntityCustomNpc)npc).modelData.getEntity(npc) == null;
					File file = ResourceDownloader.getUrlFile(npc.display.getSkinUrl(), fixSkin);
					npc.textureLocation = ResourceDownloader.getUrlResourceLocation(npc.display.getSkinUrl(), fixSkin);
					loadSkin(file, npc.textureLocation, npc.display.getSkinUrl(), fixSkin);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		if(npc.textureLocation == null)
			return DefaultPlayerSkin.getDefaultSkin();
		return npc.textureLocation;
	}
	
	private void loadSkin(File file, ResourceLocation resource, String par1Str, boolean fix64){
		TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
		AbstractTexture object = texturemanager.getTexture(resource, null);
		if(object == null){
			ResourceDownloader.load(new ImageDownloadAlt(file, par1Str, resource, DefaultPlayerSkin.getDefaultSkin(), fix64, () -> {}));
		}
	}
}
