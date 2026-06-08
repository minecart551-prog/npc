package noppes.npcs.client.overlay;

import net.minecraft.client.gui.GuiGraphics;
import noppes.npcs.api.overlay.*;

import java.util.ArrayDeque;
import java.util.Queue;

public class Overlay
{
    private final Queue<IOverlayRenderComponent> components;
    private final int linkSide;

    public Overlay(final IOverlay overlay) {
        this.components = new ArrayDeque<>();
        this.linkSide = overlay.getLinkSide();
        for (final IOverlayComponent component : overlay.getComponents()) {
            if (component instanceof ILabel) {
                this.components.add(new OverlayLabelComponent((ILabel)component));
            }
            else if (component instanceof IRenderItemOverlay) {
                this.components.add(new OverlayRenderItemComponent((IRenderItemOverlay)component));
            }
            else {
                if (!(component instanceof ITexturedRect)) {
                    continue;
                }
                this.components.add(new OverlayTexturedRectComponent((ITexturedRect)component));
            }
        }
    }

    public void render(GuiGraphics graphics) {
        graphics.pose().pushPose();
        for (final IOverlayRenderComponent component : this.components) {
            component.render(graphics, this.linkSide);
        }
        graphics.pose().popPose();
    }
}
