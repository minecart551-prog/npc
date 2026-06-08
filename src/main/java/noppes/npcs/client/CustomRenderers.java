package noppes.npcs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import noppes.npcs.CustomBlocks;
import noppes.npcs.CustomEntities;
import noppes.npcs.client.model.*;
import noppes.npcs.client.renderer.*;
import noppes.npcs.client.renderer.blocks.*;

@Environment(EnvType.CLIENT)
public class CustomRenderers {

    @Environment(EnvType.CLIENT)
    public static void registerEntityRenderer(){
        EntityRendererRegistry.register(CustomEntities.entityNpcPony, (EntityRendererProvider.Context manager) -> new RenderNPCPony(manager, new ModelPony()));
        EntityRendererRegistry.register(CustomEntities.entityNpcCrystal, (EntityRendererProvider.Context manager) -> new RenderNpcCrystal(manager, new ModelNpcCrystal()));
        EntityRendererRegistry.register(CustomEntities.entityNpcDragon, (EntityRendererProvider.Context manager) -> new RenderNpcDragon(manager, new ModelNpcDragon(), 0.5F));
        EntityRendererRegistry.register(CustomEntities.entityNpcSlime, (EntityRendererProvider.Context manager) -> new RenderNpcSlime(manager, new ModelNpcSlime(16), new ModelNpcSlime(0), 0.25F));
        EntityRendererRegistry.register(CustomEntities.entityProjectile, (EntityRendererProvider.Context manager) -> new RenderProjectile(manager));
        EntityRendererRegistry.register(CustomEntities.entityCustomNpc, (EntityRendererProvider.Context manager) -> new RenderCustomNpc(manager, new PlayerModel(manager.getModelSet().bakeLayer(ModelLayers.PLAYER), false)));
        EntityRendererRegistry.register(CustomEntities.entityNPC64x32, (EntityRendererProvider.Context manager) -> new RenderCustomNpc(manager, new ModelPlayer64x32(manager.getModelSet().bakeLayer(ModelLayers.PLAYER))));
        EntityRendererRegistry.register(CustomEntities.entityNPCGolem, (EntityRendererProvider.Context manager) -> new RenderNPCInterface(manager, new ModelNPCGolem(0), 0));
        EntityRendererRegistry.register(CustomEntities.entityNpcAlex, (EntityRendererProvider.Context manager) -> new RenderCustomNpc(manager, new PlayerModel(manager.getModelSet().bakeLayer(ModelLayers.PLAYER_SLIM), true)));
        EntityRendererRegistry.register(CustomEntities.entityNpcClassicPlayer, (EntityRendererProvider.Context manager) -> new RenderCustomNpc(manager, new ModelClassicPlayer(manager.getModelSet().bakeLayer(ModelLayers.PLAYER), 0)));

        BlockEntityRenderers.register(CustomBlocks.tile_anvil, BlockCarpentryBenchRenderer::new);
        BlockEntityRenderers.register(CustomBlocks.tile_mailbox, BlockMailboxRenderer::new);
        BlockEntityRenderers.register(CustomBlocks.tile_scripted, BlockScriptedRenderer::new);
        BlockEntityRenderers.register(CustomBlocks.tile_scripteddoor, BlockDoorRenderer::new);
        BlockEntityRenderers.register(CustomBlocks.tile_copy, BlockCopyRenderer::new);
        BlockEntityRenderers.register(CustomBlocks.tile_builder, BlockBuilderRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.redstone_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.mailbox_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.mailbox2_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.mailbox3_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.waypoint_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.scripted_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.builder_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.copy_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.border_item, CustomTileEntityItemStackRenderer.instance());
        BuiltinItemRendererRegistry.INSTANCE.register(CustomBlocks.carpentry_item, CustomTileEntityItemStackRenderer.instance());
    }
}
