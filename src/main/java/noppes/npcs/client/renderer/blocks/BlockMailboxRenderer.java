package noppes.npcs.client.renderer.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.blocks.tiles.TileMailbox;
import noppes.npcs.client.model.blocks.ModelMailboxUS;
import noppes.npcs.client.model.blocks.ModelMailboxWow;

public class BlockMailboxRenderer<T extends TileMailbox> implements BlockEntityRenderer<T> {

	private final ModelMailboxUS model = new ModelMailboxUS();
	private final ModelMailboxWow model2 = new ModelMailboxWow();

    private static final ResourceLocation text1 = new ResourceLocation("customnpcs","textures/models/mailbox1.png");
    private static final ResourceLocation text2 = new ResourceLocation("customnpcs","textures/models/mailbox2.png");
    private static final ResourceLocation text3 = new ResourceLocation("customnpcs","textures/models/mailbox3.png");

	private static final RenderType type1 = RenderType.entityCutout(text1);
	private static final RenderType type2 = RenderType.entityCutout(text2);
	private static final RenderType type3 = RenderType.entityCutout(text3);


	public BlockMailboxRenderer(BlockEntityRendererProvider.Context dispatcher) {

	}

	@Override
	public void render(TileMailbox te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
		int meta = 0;
		int type = te.getModel();
		matrixStack.pushPose();
		matrixStack.translate(0.5f, 1.5f, 0.5f);
		matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
		matrixStack.mulPose(Axis.YP.rotationDegrees(90 * meta));
		if (type == 0) {
			model.renderToBuffer(matrixStack, buffer.getBuffer(type1), light, overlay, 1, 1, 1, 1);
		}
		else if (type == 1) {
			model2.renderToBuffer(matrixStack, buffer.getBuffer(type2), light, overlay, 1, 1, 1, 1);
		}
		else if (type == 2) {
			model2.renderToBuffer(matrixStack, buffer.getBuffer(type3), light, overlay, 1, 1, 1, 1);
		}
		matrixStack.popPose();
	}

}
