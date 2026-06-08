package noppes.npcs.client.renderer.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomBlocks;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.schematics.Schematic;

public class BlockBuilderRenderer extends BlockRendererInterface<TileBuilder>{
	private final static ItemStack item = new ItemStack(CustomBlocks.builder);
	public static Schematic schematic = null;
	public static BlockPos pos = null;

    public BlockBuilderRenderer(BlockEntityRendererProvider.Context dispatcher) {
        super(dispatcher);
    }

    @Override
	public void render(TileBuilder tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        matrixStack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        //RenderHelper.enableStandardItemLighting();
        RenderSystem.disableBlend();

        if(tile.getBlockPos().equals(TileBuilder.DrawPos)){
            ClientEventHandler.onRenderTick(matrixStack, tile.getBlockPos(), tile);
        }

        matrixStack.translate(0.5f, 0.5f, 0.5f);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180));
		//Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.popPose();
	}

	
    public void drawSelectionBox(PoseStack matrixStack, MultiBufferSource buffer, BlockPos pos){
        AABB bb = new AABB(BlockPos.ZERO, pos);
        matrixStack.translate(0.001f, 0.001f, 0.001f);
        LevelRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()), bb, 1, 0, 0, 1);
    }
}
