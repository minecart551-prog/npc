package noppes.npcs.client.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class AniWaving implements AnimationBase {

	@Override
	public void animatePre(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Entity entity, HumanoidModel model, int animationStart) {

	}

	@Override
	public void animatePost(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Entity entity, HumanoidModel model, int animationStart) {
		float f = Mth.sin(entity.tickCount * 0.27f);
		float f2 = Mth.sin((entity.tickCount + 1) * 0.27f);
		f += (f2 - f) * Minecraft.getInstance().getDeltaFrameTime();

		model.rightArm.xRot = -0.1f;
		model.rightArm.yRot = 0;
		model.rightArm.zRot = (float) (Math.PI - 1f  - f * 0.5f );
	}
}
