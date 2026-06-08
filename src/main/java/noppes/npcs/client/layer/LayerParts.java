package noppes.npcs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.client.parts.*;
import noppes.npcs.constants.BodyPart;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.shared.common.util.NopVector3f;

public class LayerParts<T extends EntityCustomNpc, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

	public LayerParts(LivingEntityRenderer<T, M> render) {
		super(render);
	}

	@Override
	public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, EntityCustomNpc player, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch){
		ModelData data = ModelData.get(player);
		for(MpmPartData part : data.mpmParts){
			MpmPart mp = part.getPart();
			if(mp == null || mp.renderType == PartRenderType.NONE || !mp.isEnabled){
				continue;
			}
			MpmPartAbstractClient partc = (MpmPartAbstractClient) mp;
			rotate(data, partc, player, getParentModel(), limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
			renderPart(part, partc, mStack, typeBuffer, lightmapUV, player, getParentModel(), data);
		}
		data.startMoveAnimation = false;
		data.startAnimation = false;
	}

	public static void renderPart(MpmPartData data, MpmPartAbstractClient partc, PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, EntityCustomNpc player, HumanoidModel model, ModelData pdata){
		mStack.pushPose();
		boolean shouldRender = true;
		if(partc.bodyPart == BodyPart.HEAD){
			model.head.translateAndRotate(mStack);
		}
		if(partc.bodyPart == BodyPart.BODY){
			model.body.translateAndRotate(mStack);
		}
		if(partc.bodyPart == BodyPart.LEGS){
			ModelPartWrapper rmodelPart = partc.getPart("right_leg");
			ModelPartWrapper lmodelPart = partc.getPart("left_leg");
			if(rmodelPart != null){
				shouldRender = false;
				mStack.pushPose();
				ModelPartConfig config = pdata.getPartConfig(EnumParts.LEG_RIGHT);
				mStack.translate(0, config.transY * 2, 0);
				mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
				if(lmodelPart != null){
					lmodelPart.setVisible(false);
				}
				rmodelPart.setVisible(true);
				partc.render(data, mStack, typeBuffer, lightmapUV, player);
				mStack.popPose();
			}
			if(lmodelPart != null){
				shouldRender = false;
				mStack.pushPose();
				ModelPartConfig config = pdata.getPartConfig(EnumParts.LEG_LEFT);
				mStack.translate(0, config.transY * 2, 0);
				mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
				if(rmodelPart != null){
					rmodelPart.setVisible(false);
				}
				lmodelPart.setVisible(true);
				partc.render(data, mStack, typeBuffer, lightmapUV, player);
				mStack.popPose();
			}

			if(shouldRender){
				ModelPartConfig config = pdata.getPartConfig(EnumParts.LEG_LEFT);
				mStack.translate(0, config.transY * 2, 0);
				mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
			}
		}
		if(partc.bodyPart == BodyPart.ARMS){
			ModelPartWrapper rmodelPart = partc.getPart("right_arm");
			ModelPartWrapper lmodelPart = partc.getPart("left_arm");
			if(rmodelPart != null){
				shouldRender = false;
				mStack.pushPose();
				ModelPartConfig config = pdata.getPartConfig(EnumParts.ARM_RIGHT);
				mStack.translate(0.125f*2*(config.scaleX-1), config.transY + (1 - config.scaleY) * 0.125f, 0);
				mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
				if(lmodelPart != null){
					lmodelPart.setVisible(false);
				}
				rmodelPart.setVisible(true);
				partc.render(data, mStack, typeBuffer, lightmapUV, player);
				mStack.popPose();
			}
			if(lmodelPart != null){
				shouldRender = false;
				mStack.pushPose();
				ModelPartConfig config = pdata.getPartConfig(EnumParts.ARM_LEFT);
				mStack.translate(-0.125f*2*(config.scaleX-1), config.transY + (1 - config.scaleY) * 0.125f, 0);
				mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
				if(rmodelPart != null){
					rmodelPart.setVisible(false);
				}
				lmodelPart.setVisible(true);
				partc.render(data, mStack, typeBuffer, lightmapUV, player);
				mStack.popPose();
			}

			if(shouldRender){
				ModelPartConfig config = pdata.getPartConfig(EnumParts.ARM_LEFT);
				mStack.translate(0, config.transY + (1 - config.scaleY) * 0.125f, 0);
				mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
			}
		}

		if(shouldRender){
			partc.render(data, mStack, typeBuffer, lightmapUV, player);
		}
		mStack.popPose();
	}

	private void rotate(ModelData playerdata, MpmPartAbstractClient part, EntityCustomNpc player, HumanoidModel base, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
		part.animationData.animation(AnimationType.STATIC, (int)age, partialTicks);
		if(limbSwingAmount>0.01){
			if(player.onGround()) {
				if(player.ais.getAnimation()==AnimationType.CRAWL){
					playerdata.setAnimation(AnimationType.CRAWL);
				}else{
					playerdata.setAnimation(AnimationType.WALK);
				}
			}else{
				playerdata.setAnimation(AnimationType.FLY);
			}
		}else{
			if(player.ais.getAnimation()==AnimationType.NONE){
				playerdata.setAnimation(AnimationType.IDLE);
			}else{
				playerdata.setAnimation(player.ais.getAnimation());
			}
		}
		int moveAnimation = playerdata.getMoveAnimtion(player);
		if(playerdata.startMoveAnimation){
			part.animationData.start(moveAnimation);
		}
		boolean didAnimation = false;
		if(playerdata.animation != AnimationType.NONE){
			if(playerdata.startAnimation){
				part.animationData.start(playerdata.animation);
			}
			didAnimation = part.animationData.animation(playerdata.animation, (int)age, partialTicks);
		}

		if(!didAnimation && (moveAnimation == AnimationType.IDLE || moveAnimation == AnimationType.FLY_IDLE)){
			part.animationData.animation(moveAnimation, (int)age, partialTicks);
		}
		else{
			part.animationData.animation(moveAnimation, Mth.cos(limbSwing * 0.6662F) * limbSwingAmount / 2 + 0.5f);
		}

		if(part.animationType == PartBehaviorType.LEGS){
			HumanoidModel model = getParentModel();
			ModelPartWrapper modelPart = part.getPart("right_leg");
			if(modelPart != null){
				modelPart.setRot(new NopVector3f(model.rightLeg.xRot, model.rightLeg.yRot, model.rightLeg.zRot));
				modelPart.setPos(new NopVector3f(model.rightLeg.x, model.rightLeg.y, model.rightLeg.z));
			}

			modelPart = part.getPart("left_leg");
			if(modelPart != null){
				modelPart.setRot(new NopVector3f(model.leftLeg.xRot, model.leftLeg.yRot, model.leftLeg.zRot));
				modelPart.setPos(new NopVector3f(model.leftLeg.x, model.leftLeg.y, model.leftLeg.z));
			}
		}
		if(part.animationType == PartBehaviorType.ARMS){
			HumanoidModel model = getParentModel();
			ModelPartWrapper modelPart = part.getPart("right_arm");
			if(modelPart != null){
				modelPart.setRot(new NopVector3f(model.rightArm.xRot, model.rightArm.yRot, model.rightArm.zRot));
				modelPart.setPos(new NopVector3f(model.rightArm.x, model.rightArm.y, model.rightArm.z));
			}

			modelPart = part.getPart("left_arm");
			if(modelPart != null){
				modelPart.setRot(new NopVector3f(model.leftArm.xRot, model.leftArm.yRot, model.leftArm.zRot));
				modelPart.setPos(new NopVector3f(model.leftArm.x, model.leftArm.y, model.leftArm.z));
			}
		}
		if(part.animationType == PartBehaviorType.BEARD){
			part.rot = part.rot.set(base.head.xRot < 0 ? 0 : -base.head.xRot, part.rot.y, part.rot.z);
		}
		if(part.animationType == PartBehaviorType.HAIR){
			ModelPart head = base.head;
			if(head.xRot < 0){
				part.rot = part.rot.set(-head.xRot * 1.2f, part.rot.y, part.rot.z);
				if(head.xRot > -1){
					part.pos = part.pos.set(part.pos.x, -head.xRot * 1.5f, -head.xRot * 1.5f);
				}
			}
			else{
				part.pos = NopVector3f.ZERO;
			}
		}
		if(part.animationType == PartBehaviorType.WINGS){
			ModelPartWrapper modelPart = part.getPart("right_wing");
			ModelPartWrapper modelPartL = part.getPart("left_wing");
			float xRot;
			float zRot;

			if(player.level().isEmptyBlock(player.blockPosition().below())){
				float motion = Math.abs(Mth.sin(limbSwing * 0.033F + (float)Math.PI) * 0.4F) * limbSwingAmount;
				float speed = (float) (0.55f + 0.5f * motion);
				float y = Mth.sin(age * 0.35F);
				xRot = zRot = y * 0.5f * speed;
			}
			else{
				zRot = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
				xRot = Mth.sin(age * 0.067F) * 0.05F;
			}
			modelPart.setRot(modelPart.oriRot.add(xRot, xRot, zRot));
			modelPartL.setRot(modelPartL.oriRot.add(xRot, -xRot, -zRot));
		}
		if(part.animationType == PartBehaviorType.WINGS2){
			ModelPartWrapper modelPart = part.getPart("right_wing");
			ModelPartWrapper modelPartL = part.getPart("left_wing");
			float yRot;

			if(player.level().isEmptyBlock(player.blockPosition().below())){
				float motion = Math.abs(Mth.sin(limbSwing * 0.033F + (float)Math.PI) * 0.4F) * limbSwingAmount;
				float speed = (float) (0.55f + 0.5f * motion);
				float y = Mth.sin(age * 0.35F);
				yRot = y * 0.5f * speed;

			}
			else{
				yRot = Mth.sin(age * 0.07F) * 0.44F;
			}
			modelPart.setRot(modelPart.oriRot.add(0, yRot, 0));
			modelPartL.setRot(modelPartL.oriRot.add(0, -yRot, 0));
		}
	}
}
