package noppes.npcs;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import noppes.npcs.entity.EntityNPCInterface;

public interface IChatMessages {

	public void addMessage(String message, EntityNPCInterface npc);
	public void renderMessages(PoseStack matrixStack, MultiBufferSource typeBuffer, float scale, boolean inRange, int lightmapUV);
	
}
