package noppes.npcs.client.model.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import noppes.npcs.shared.client.model.NopModelPart;

public class ModelMailboxWow extends Model {
	// fields
	NopModelPart Shape4;
	NopModelPart Shape1;
	NopModelPart Shape2;
	NopModelPart Shape3;

	public ModelMailboxWow() {
		super(RenderType::entityCutout);

		Shape4 = new NopModelPart(128, 64, 59, 0);
		Shape4.addBox(0F, 0F, 0F, 8, 6, 0);
		Shape4.setPos(-4F, -4F, 0F);
		
		Shape1 = new NopModelPart(128, 64, 0, 39);
		Shape1.addBox(0F, 0F, 0F, 8, 5, 8);
		Shape1.setPos(-4F, 19F, -4F);
		
		Shape2 = new NopModelPart(128, 64, 0, 21);
		Shape2.addBox(0F, 0F, 0F, 6, 9, 6);
		Shape2.setPos(-3F, 10F, -3F);
		
		Shape3 = new NopModelPart(128, 64, 0, 0);
		Shape3.addBox(0F, 0F, 0F, 12, 8, 12);
		Shape3.setPos(-6F, 2F, -6F);
	}

	@Override
	public void renderToBuffer(PoseStack mStack, VertexConsumer iVertex, int lightmapUV, int packedOverlayIn, float red, float green, float blue, float alpha) {
		Shape4.render(mStack, iVertex, lightmapUV, packedOverlayIn);
		Shape1.render(mStack, iVertex, lightmapUV, packedOverlayIn);
		Shape2.render(mStack, iVertex, lightmapUV, packedOverlayIn);
		Shape3.render(mStack, iVertex, lightmapUV, packedOverlayIn);
	}
}
