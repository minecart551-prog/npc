package noppes.npcs.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.client.gui.player.tabs.AbstractTab;
import noppes.npcs.client.gui.player.tabs.InventoryTabFactions;
import noppes.npcs.client.gui.player.tabs.InventoryTabQuests;
import noppes.npcs.client.gui.player.tabs.InventoryTabVanilla;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketPlayerSoundPlays;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.shared.common.util.LogWriter;

import java.util.List;

public class ClientEventHandler implements ScreenEvents.AfterInit {
	
	//private int displayList = -1;
	private VertexBuffer cache = null;

	//@SubscribeEvent
	public static void onRenderTick(PoseStack matrixStack, BlockPos rpos, BlockEntity te){
		MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		LocalPlayer player = Minecraft.getInstance().player;
		if(rpos == null || rpos == BlockPos.ZERO || rpos.distSqr(player.blockPosition()) > 1000000)
			return;
		TileBuilder tile = (TileBuilder) te;
        SchematicWrapper schem = tile.getSchematic();
        if(schem == null)
        	return;
		matrixStack.pushPose();
        //RenderHelper.enableStandardItemLighting();
		//matrixStack.translate(-1, 0, -1);

		//Vec3 cpos =  BlockEntityRendererProvider.Context.instance.camera.getPosition();
		//Vec3 cpos = player.position();
		//matrixStack.translate(rpos.getX() - cpos.x(), rpos.getY() - cpos.y() + 0.01, rpos.getZ() - cpos.z());
		matrixStack.translate(1, tile.yOffest, 1);
		if(TileBuilder.Compiled){
			//cache.draw(matrixStack.last().pose(), 7);
    	}
    	else{
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
			//cache = new VertexBuffer(DefaultVertexFormat.BLOCK);
            try{
				//PoseStack stack = new PoseStack();
                for(int i = 0; i < schem.size && i < 25000; i++){

                	BlockState state = schem.schema.getBlockState(i);
                	if(state.getRenderShape() == RenderShape.INVISIBLE || state.getRenderShape() != RenderShape.MODEL)
                		continue;
					int posX = (int) (i % schem.schema.getWidth());
					int posZ = (int)((i - posX)/schem.schema.getWidth()) % schem.schema.getLength();
					int posY = (int)(((i - posX)/schem.schema.getWidth()) - posZ) / schem.schema.getLength();
        			BlockPos pos = schem.rotatePos(posX, posY, posZ, tile.rotation);
					matrixStack.pushPose();
					//matrixStack.enableRescaleNormal();
					matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    //Minecraft.getInstance().getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
					//matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                    
        			state = schem.rotationState(state, tile.rotation);
        			try{
						BakedModel ibakedmodel = dispatcher.getBlockModel(state);
						BufferBuilder builder = (BufferBuilder)buffer.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
						dispatcher.getModelRenderer().renderModel(matrixStack.last(), builder, state, ibakedmodel, 1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY);
						//cache.upload(builder);
        			}
        			catch(Exception e){
						e.printStackTrace();
        			}
        			finally{
						matrixStack.popPose();
						//TileBuilder.Compiled = true;
        			}
        			
                }
            }
            catch(Exception e){
            	LogWriter.error("Error preview builder block", e);
            }
            finally{
				//RenderSystem.endList();
//                if(GL11.glGetError() == 0) {
//					TileBuilder.Compiled = true;
//				}
            }
    	
        }
		if(tile.rotation % 2 == 0)
			drawSelectionBox(matrixStack, buffer, new BlockPos(schem.schema.getWidth(), schem.schema.getHeight(), schem.schema.getLength()));
		else
			drawSelectionBox(matrixStack, buffer, new BlockPos(schem.schema.getLength(), schem.schema.getHeight(), schem.schema.getWidth()));
        //RenderHelper.disableStandardItemLighting();
        //RenderSystem.translatef(-1, 0, -1);
		matrixStack.popPose();
	}

	public static void post(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight){
		MarkData data = MarkData.get(entity);
		Player player = Minecraft.getInstance().player;
		for(Mark m : data.marks){
			if(m.getType() != MarkType.NONE && m.availability.isAvailable(player)){
				MarkRenderer.render(entity, poseStack, buffer, packedLight, m);
				break;
			}
		}
	}

//	@SubscribeEvent
//	public void playSound(PlaySoundEvent event){
//		Minecraft mc = Minecraft.getInstance();
//		if(mc == null || mc.level == null || mc.getConnection() == null || event==null || event.getSound()==null){
//			return;
//		}
//		SoundInstance sound = event.getSound();
//		Packets.sendServer(new SPacketPlayerSoundPlays(sound.getLocation().toString(), sound.getSource().getName(), sound.isLooping()));
//	}


	public static void drawSelectionBox(PoseStack matrixStack, MultiBufferSource buffer, BlockPos pos){
		matrixStack.pushPose();
		AABB bb = new AABB(BlockPos.ZERO, pos);
		matrixStack.translate(0.001f, 0.001f, 0.001f);
		LevelRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()), bb, 1, 0, 0, 1);
		matrixStack.popPose();
    }

	//@SubscribeEvent(priority=EventPriority.LOWEST)
	@Override
	public void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (screen instanceof InventoryScreen && CustomNpcs.InventoryGuiEnabled) {
			((List)screen.children()).add(new InventoryTabVanilla().init(screen));
			((List)screen.children()).add(new InventoryTabFactions().init(screen));
			((List)screen.children()).add(new InventoryTabQuests().init(screen));
		}
	}
}
