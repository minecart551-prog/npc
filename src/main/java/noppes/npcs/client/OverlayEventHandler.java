package noppes.npcs.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import noppes.npcs.client.controllers.OverlayController;

public class OverlayEventHandler implements HudRenderCallback {

    @Override
    public void onHudRender(GuiGraphics graphics,  float tickDelta) {
        //if(event.getOverlay().id() != VanillaGuiOverlay.FROSTBITE.id()) return;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        OverlayController.getInstance().renderOverlays(graphics);
        RenderSystem.disableBlend();
    }
}
