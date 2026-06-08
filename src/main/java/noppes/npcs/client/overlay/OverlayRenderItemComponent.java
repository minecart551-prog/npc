package noppes.npcs.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.overlay.IRenderItemOverlay;

public class OverlayRenderItemComponent implements IOverlayRenderComponent
{
    private final int x;
    private final int y;
    private final int id;
    private final ItemStack item;

    public OverlayRenderItemComponent(final IRenderItemOverlay item) {
        this.x = item.getPosX();
        this.y = item.getPosY();
        this.id = item.getId();
        this.item = item.getItem().getMCItemStack();
    }

    @Override
    public void render(GuiGraphics graphics, final int linkSide) {
        graphics.pose().pushPose();
        graphics.pose().translate((double)this.x, (double)this.y, (double)this.id);
        graphics.pose().scale(1.2f, 1.2f, 1f);
        final int width = (int) (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 1.2f);
        final int height = (int) (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 1.2f);
        renderItemOverlay(graphics, linkSide, this.item, this.x, this.y, width, height);
        graphics.pose().popPose();
    }

    public void renderItemOverlay(GuiGraphics graphics, int linkSide, ItemStack item, int x, int y, int width, int height) {
        int offsetX = width / 2 * ((linkSide - 1) % 3);
        int offsetY = height / 2 * ((linkSide - 1) / 3);
        graphics.renderItem(item, x + offsetX, y + offsetY);
        graphics.renderItemDecorations(Minecraft.getInstance().font, item, x + offsetX, y + offsetY);
    }
}
