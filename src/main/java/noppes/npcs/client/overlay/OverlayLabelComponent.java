package noppes.npcs.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import noppes.npcs.api.overlay.ILabel;

public class OverlayLabelComponent implements IOverlayRenderComponent
{
    private final String text;
    private final int x;
    private final int y;
    private final int id;
    private final float scale;

    public OverlayLabelComponent(final ILabel label) {
        final String text = label.getText();
        this.x = label.getPosX();
        this.y = label.getPosY();
        this.id = label.getId();
        this.scale = label.getScale();
        final StringBuilder stringBuilder = new StringBuilder();
        final String[] split;
        final String[] values = split = text.split("&t");
        for (final String s : split) {
            if (I18n.exists(s)) {
                stringBuilder.append(I18n.get(s, new Object[0]));
            }
            else {
                stringBuilder.append(s);
            }
        }
        this.text = stringBuilder.toString();
    }

    @Override
    public void render(GuiGraphics graphics, final int linkSide) {
        graphics.pose().pushPose();
        graphics.pose().translate((double)this.x, (double)this.y, (double)this.id);
        graphics.pose().scale(this.scale, this.scale, this.scale);
        final int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        final int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        renderString(graphics, this.text, this.x, this.y, linkSide, width, height);
        graphics.pose().popPose();
    }

    public void renderString(GuiGraphics graphics, String text, int x, int y, int linkSide, int width, int height) {
        int offsetX = width / 2 * ((linkSide - 1) % 3);
        int offsetY = height / 2 * ((linkSide - 1) / 3);
        graphics.drawString(Minecraft.getInstance().font, text, x + offsetX, y + offsetY, 16777215);
    }
}
