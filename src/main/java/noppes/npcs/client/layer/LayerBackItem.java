package noppes.npcs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.*;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.wrapper.ItemStackWrapper;

public class LayerBackItem extends LayerInterface{

	public LayerBackItem(LivingEntityRenderer render) {
		super(render);
	}

	@Override
	public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
		Minecraft minecraft = Minecraft.getInstance();
		ItemStack itemstack = ItemStackWrapper.MCItem(npc.inventory.getRightHand());
		if(NoppesUtilServer.IsItemStackNull(itemstack) || npc.isAttacking())
			return;
		Item item = itemstack.getItem();
		if (item instanceof BlockItem) {
			return;
		}
		mStack.pushPose();
		base.body.translateAndRotate(mStack);
		mStack.translate(0, 0.36f, 0.14f);
		mStack.mulPose(Axis.XP.rotationDegrees(180));
		if (item instanceof SwordItem) {
			mStack.mulPose(Axis.XN.rotationDegrees(180));
		}
		BakedModel model = minecraft.getItemRenderer().getItemModelShaper().getItemModel(itemstack);
		ItemTransform p_175034_1_ = model.getTransforms().thirdPersonRightHand;
		mStack.scale(p_175034_1_.scale.x(), p_175034_1_.scale.y(), p_175034_1_.scale.z());

		minecraft.getItemRenderer().renderStatic(npc, itemstack, ItemDisplayContext.NONE, false, mStack, typeBuffer, npc.level(), lightmapUV, LivingEntityRenderer.getOverlayCoords(npc, 0.0F), 0);
		mStack.popPose();
	}

	@Override
	public void rotate(PoseStack matrixStack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		
	}
}
