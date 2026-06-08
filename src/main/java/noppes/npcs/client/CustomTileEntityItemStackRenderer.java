package noppes.npcs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import noppes.npcs.items.ItemNpcBlock;

import java.util.HashMap;

public class CustomTileEntityItemStackRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static CustomTileEntityItemStackRenderer i = null;

    private HashMap<Block, BlockEntity> data = new HashMap<Block, BlockEntity>();
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public CustomTileEntityItemStackRenderer(BlockEntityRenderDispatcher dispatcher) {
        blockEntityRenderDispatcher = dispatcher;
    }

    public static CustomTileEntityItemStackRenderer instance(){
        if(i != null){
            return i;
        }
        Minecraft mc = Minecraft.getInstance();
        i = new CustomTileEntityItemStackRenderer(mc.getBlockEntityRenderDispatcher());
        return i;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if(stack.getItem() instanceof ItemNpcBlock){
            ItemNpcBlock item = (ItemNpcBlock) stack.getItem();
            BlockEntity tile = data.get(item.block);
            if(tile == null) {
                data.put(item.block, tile = ((BaseEntityBlock)item.block).newBlockEntity(BlockPos.ZERO, item.block.defaultBlockState()));
            }
            blockEntityRenderDispatcher.renderItem(tile, matrices, vertexConsumers, light, overlay);
        }
    }
}
