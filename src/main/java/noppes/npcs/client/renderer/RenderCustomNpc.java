package noppes.npcs.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.ModelData;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.client.layer.*;
import noppes.npcs.client.parts.MpmPart;
import noppes.npcs.client.parts.MpmPartData;
import noppes.npcs.constants.BodyPart;
import noppes.npcs.controllers.CobblemonHelper;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.ArmorLayerMixin;
import noppes.npcs.mixin.LivingRenderer2Mixin;
import noppes.npcs.mixin.LivingRenderer3Mixin;

import java.util.List;

public class RenderCustomNpc<T extends EntityCustomNpc, M extends HumanoidModel<T>> extends RenderNPCInterface<T, M> {
	private float partialTicks;
	private LivingEntity entity;
	private EntityNPCInterface npc;
	private LivingEntityRenderer renderEntity;
	public M npcmodel;
	public Model otherModel;
	public ArmorLayerMixin armorLayer;
	public List<RenderLayer<T, M>> npclayers = Lists.newArrayList();

	private RenderLayer renderLayer = new RenderLayer(null){

		@Override
		public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, Entity p_225628_4_, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
			for(Object layer : ((LivingRenderer2Mixin)renderEntity).layers()){
				((RenderLayer)layer).render(mStack, typeBuffer, lightmapUV, entity, limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
			}
		}
	};

	private final HumanoidModel renderModel;

	public RenderCustomNpc(EntityRendererProvider.Context manager, M model) {
		super(manager, model, 0.5f);
		npcmodel = model;
		addLayer(new CustomHeadLayer(this, manager.getModelSet(), manager.getItemInHandRenderer()));
		addLayer(new LayerHeadwear(this));
		addLayer(new LayerNpcCloak(this));
		addLayer(new LayerParts(this));

		addLayer(new ItemInHandLayer(this, manager.getItemInHandRenderer()));
		addLayer(new LayerGlow(this));
		HumanoidArmorLayer armorLayer = new HumanoidArmorLayer<>(this, new HumanoidModel(manager.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(manager.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), manager.getModelManager());
		addLayer(armorLayer);
		this.armorLayer = (ArmorLayerMixin) armorLayer;

		renderModel = new HumanoidModel(manager.bakeLayer(ModelLayers.PLAYER)) {
			@Override
			public void renderToBuffer(PoseStack mStack, VertexConsumer iVertex, int lightmapUV, int packedOverlayIn, float red, float green, float blue, float alpha) {
				int color = npc.display.getTint();
				if(color < 0xFFFFFF){
					red = (color >> 16 & 255) / 255f;
					green = (color >> 8  & 255) / 255f;
					blue = (color & 255) / 255f;
				}
				otherModel.renderToBuffer(mStack, iVertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
			}

			@Override
			public void setupAnim(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				if(otherModel instanceof EntityModel) {
					EntityModel em = (EntityModel) otherModel;
					em.setupAnim(entity, limbSwing, limbSwingAmount, ((LivingRenderer3Mixin)renderEntity).callGetBob(entity, Minecraft.getInstance().getFrameTime()), netHeadYaw, headPitch);
				}
			}

			@Override
			public void prepareMobModel(Entity npc, float animationPos, float animationSpeed, float partialTicks) {
				if (PixelmonHelper.isPixelmon(entity)) {
					Model pixModel = (Model) PixelmonHelper.getModel(entity);
					if (pixModel != null) {
						otherModel = pixModel;
						PixelmonHelper.setupModel(entity, pixModel);
					}
				}

				if(otherModel instanceof HumanoidModel){
					HumanoidModel bm = (HumanoidModel) otherModel;
					bm.swimAmount = ((EntityCustomNpc) npc).getSwimAmount(partialTicks);
					bm.crouching = RenderCustomNpc.this.npcmodel.crouching;
				}

				if(otherModel instanceof EntityModel){
					EntityModel em = (EntityModel) otherModel;
					em.riding = entity.isPassenger() && (entity.getVehicle() != null);
					em.young = entity.isBaby();
					em.attackTime = getAttackAnim((T) npc, partialTicks);
					em.prepareMobModel(entity, animationPos, animationSpeed, partialTicks);
				}
			}
		};
	}

	@Override
	public Vec3 getRenderOffset(T npc, float partialTicks) {
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
				yOffset -= 0.5f - npc.modelData.getLegsY() * 0.8f;
			}
			else if(npc.isCrouching()){
				yOffset -= 0.125D;
			}
		}
		return new Vec3(xOffset, yOffset * (npc.display.getSize() / 5f), zOffset);
	}

	void hideParts(){
		if(npc instanceof EntityCustomNpc) {
			ModelData data = ModelData.get((EntityCustomNpc) npc);
			npcmodel.leftLeg.visible = !data.hiddenParts.contains(BodyPart.LEFT_LEG);
			npcmodel.rightLeg.visible = !data.hiddenParts.contains(BodyPart.RIGHT_LEG);
			npcmodel.leftArm.visible = !data.hiddenParts.contains(BodyPart.LEFT_ARM);
			npcmodel.rightArm.visible = !data.hiddenParts.contains(BodyPart.RIGHT_ARM);
			npcmodel.body.visible = !data.hiddenParts.contains(BodyPart.BODY);
			npcmodel.head.visible = !data.hiddenParts.contains(BodyPart.HEAD);
			npcmodel.hat.visible = !data.hiddenParts.contains(BodyPart.HEAD);
			if(npcmodel instanceof PlayerModel<?>){
				PlayerModel playerModel = (PlayerModel) npcmodel;
				playerModel.jacket.visible = !data.hiddenParts.contains(BodyPart.BODY);
				playerModel.leftSleeve.visible = !data.hiddenParts.contains(BodyPart.LEFT_ARM);
				playerModel.rightSleeve.visible = !data.hiddenParts.contains(BodyPart.RIGHT_ARM);
				playerModel.leftPants.visible = !data.hiddenParts.contains(BodyPart.LEFT_LEG);
				playerModel.rightPants.visible = !data.hiddenParts.contains(BodyPart.RIGHT_LEG);
			}
		}
	}

	@Override
	public void render(T npc, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		this.npc = npc;
		this.partialTicks = partialTicks;
		Entity prevEntity = entity;
		entity = npc.modelData.getEntity(npc);
		if(prevEntity != null && entity == null){
			model = npcmodel;
			renderEntity = null;
			layers.clear();
			layers.addAll(npclayers);
		}
		if (entity != null) {
			EntityRenderer render = entityRenderDispatcher.getRenderer(entity);
			if(npc.modelData.simpleRender){
				renderEntity = null;
				matrixStack.pushPose();
				render.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
				renderNameTag(npc, Component.empty(), matrixStack, buffer, packedLight);
				matrixStack.popPose();
				return;
			}
			if (render instanceof LivingEntityRenderer) {
				renderEntity = (LivingEntityRenderer) render;
				otherModel = renderEntity.getModel();
				if(CobblemonHelper.Enabled && CobblemonHelper.isPokemon(entity)){
					otherModel = CobblemonHelper.getPokemonModel(entity);
				}
				model = (M) renderModel;
				layers.clear();
				layers.add(renderLayer);
				layers.add(new LayerGlow(this));
				if(render instanceof RenderCustomNpc){
					for(Object layer : ((LivingRenderer2Mixin)renderEntity).layers()){
						if (layer instanceof LayerPreRender) {
							((LayerPreRender) layer).preRender((EntityCustomNpc) entity);
						}
					}
				}
			}
			else {
				renderEntity = null;
				entity = null;
				model = npcmodel;
				layers.clear();
				layers.addAll(npclayers);
			}

		} else {
			hideParts();
			List<RenderLayer<T, M>> list = this.layers;
			for (RenderLayer<T, M> layer : list) {
				if (layer instanceof LayerPreRender) {
					((LayerPreRender) layer).preRender(npc);
				}
			}
		}

		npcmodel.rightArmPose = getPose(npc, npc.getMainHandItem());
		npcmodel.leftArmPose = getPose(npc, npc.getOffhandItem());
		super.render(npc, entityYaw, partialTicks, matrixStack, buffer, packedLight);
		RenderSystem.setShaderColor(1,1,1,1);
	}


	@Override
	protected RenderType getRenderType(T p_230496_1_, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
		ResourceLocation resourcelocation = this.getTextureLocation(p_230496_1_);
		if (p_230496_2_ && model == renderModel) {
			return this.otherModel.renderType(resourcelocation);
		}
		if(entity==null){
			return this.model.renderType(resourcelocation);
		}else {
			return super.getRenderType(p_230496_1_, p_230496_2_, p_230496_3_, p_230496_4_);
		}
	}

	public ArmPose getPose(T npc, ItemStack item) {
		if (NoppesUtilServer.IsItemStackNull(item))
			return ArmPose.EMPTY;

		if (npc.getUseItemRemainingTicks() > 0) {
			UseAnim enumaction = item.getUseAnimation();

			if (enumaction == UseAnim.BLOCK) {
				return HumanoidModel.ArmPose.BLOCK;
			} else if (enumaction == UseAnim.BOW) {
				return HumanoidModel.ArmPose.BOW_AND_ARROW;
			}
		}
		return ArmPose.ITEM;
	}

//	protected void renderModel(PoseStack mStack, VertexConsumer iVertex, int lightmapUV, int packedOverlayIn, float red, float green, float blue, float alpha) {
//		if (renderEntity != null) {
//			boolean flag = !npc.isInvisible();
//			boolean flag1 = !flag && !npc.isInvisibleTo(Minecraft.getInstance().player);
//			if (!flag && !flag1)
//				return;
//
//			if (flag1) {
//				RenderSystem.pushMatrix();
//				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.15F);
//				RenderSystem.depthMask(false);
//				RenderSystem.enableBlend();
//				RenderSystem.blendFunc(770, 771);
//				RenderSystem.alphaFunc(516, 0.003921569F);
//			}
//
//			NPCRendererHelper.renderModel(entity, limbSwingAmount, par3, par4, par5, par6, par7, renderEntity, model, getTextureLocation(npc));

//			if (!npc.display.getOverlayTexture().isEmpty()) {
//				RenderSystem.depthFunc(GL11.GL_LEQUAL);
//				if (npc.textureGlowLocation == null) {
//					npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
//				}
//				float f1 = 1.0F;
//				RenderSystem.enableBlend();
//				RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//				RenderSystem.disableLighting();
//				if (npc.isInvisible()) {
//					RenderSystem.depthMask(false);
//				} else {
//					RenderSystem.depthMask(true);
//				}
//				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//				RenderSystem.pushMatrix();
//				RenderSystem.scalef(1.001f, 1.001f, 1.001f);
//				NPCRendererHelper.renderModel(entity, limbSwingAmount, par3, par4, par5, par6, par7, renderEntity, model, npc.textureGlowLocation);
//				RenderSystem.popMatrix();
//				RenderSystem.enableLighting();
//				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f1);
//
//				RenderSystem.depthFunc(GL11.GL_LEQUAL);
//				RenderSystem.disableBlend();
//			}
//
//			if (flag1) {
//				RenderSystem.disableBlend();
//				RenderSystem.alphaFunc(516, 0.1F);
//				RenderSystem.popMatrix();
//				RenderSystem.depthMask(true);
//			}
//		}
//	}
//
//	@Override
//	protected void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
//		if (entity != null && renderEntity != null) {
//			NPCRendererHelper.drawLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn, renderEntity);
//		} else {
//			super.renderLayers(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);
//		}
//	}

	@Override
    protected void scale(T npc, PoseStack matrixScale, float f){
		if(renderEntity != null){
	    	renderColor(npc);
			int size = npc.display.getSize();
			if(entity instanceof EntityNPCInterface){
				((EntityNPCInterface)entity).display.setSize(5);
			}
			EntityRenderer render = entityRenderDispatcher.getRenderer(entity);
			if(!npc.modelData.simpleRender && render instanceof LivingEntityRenderer){
				((LivingRenderer3Mixin)render).callScale(entity, matrixScale, partialTicks);
			}
			//NPCRendererHelper.scale(entity, f, matrixScale, renderEntity);
			npc.display.setSize(size);
			matrixScale.scale(0.2f * npc.display.getSize(), 0.2f * npc.display.getSize(), 0.2f * npc.display.getSize());
		}
		else
			super.scale(npc, matrixScale, f);
    }
//
//	@Override
//    protected float getBob(T par1LivingEntity, float limbSwingAmount){
//		if(renderEntity != null){
//			return NPCRendererHelper.getBob(entity, limbSwingAmount, renderEntity);
//		}
//        return super.getBob(par1LivingEntity, limbSwingAmount);
//    }
}