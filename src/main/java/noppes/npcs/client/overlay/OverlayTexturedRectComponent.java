package noppes.npcs.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.api.overlay.ITexturedRect;

import java.util.Objects;

public class OverlayTexturedRectComponent implements IOverlayRenderComponent
{
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String texture;
    private final int textureX;
    private final int textureY;
    private final int textureMaxX;
    private final int textureMaxY;
    private final int id;

    public OverlayTexturedRectComponent(final ITexturedRect component) {
        this.x = component.getPosX();
        this.y = component.getPosY();
        this.id = component.getId();
        this.width = component.getWidth();
        this.height = component.getHeight();
        this.texture = component.getTexture();
        this.textureX = component.getTextureX();
        this.textureY = component.getTextureY();
        this.textureMaxX = component.getTextureMaxX();
        this.textureMaxY = component.getTextureMaxY();
    }

    @Override
    public void render(GuiGraphics graphics, final int linkSide) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, (double)this.id);
        final int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();// - this.width;
        final int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();// - this.height;
        final int i = width / 2;
        if (Objects.equals(this.texture, "")) {
            renderGradientRect(graphics, this.x, this.y, linkSide, width, height, this.width, this.height, i, -1072689136, -804253680);
        }
        else {
            ResourceLocation resLoc = new ResourceLocation(this.texture);
            if (this.textureX >= 0 && this.textureY >= 0) {
                if (this.textureMaxX >= 0 && this.textureMaxY >= 0) {
                    renderRectTextureCustomSize(graphics, resLoc, this.x, this.y, linkSide, width, height, this.width, this.height, this.textureX, this.textureY, this.textureMaxX, this.textureMaxY);
                }
                else {
                    renderRectTextureSize(graphics, resLoc, this.x, this.y, linkSide, width, height, this.width, this.height, this.textureX, this.textureY);
                }
            }
            else {
                renderRectTexture(graphics, resLoc, this.x, this.y, linkSide, width, height, this.width, this.height);
            }
        }
        graphics.pose().popPose();
    }

    public void renderGradientRect(GuiGraphics graphics, int x, int y, int linkSide, int widthScaled, int heightScaled, int width, int height, int i, int startColor, int endColor) {
        int offsetX = widthScaled / 2 * ((linkSide - 1) % 3);
        int offsetY = heightScaled / 2 * ((linkSide - 1) / 3);
        graphics.fillGradient(x + offsetX, y + offsetY, x + offsetX + width, y + offsetY + height, startColor, endColor);
    }

    public void renderRectTexture(GuiGraphics graphics, ResourceLocation resLoc, int x, int y, int linkSide, int widthScaled, int heightScaled, int width, int height) {
        renderRectTextureCustomSize(graphics, resLoc, x, y, linkSide, widthScaled, heightScaled, width, height, 0, 0, 256, 256);
    }

    public void renderRectTextureSize(GuiGraphics graphics, ResourceLocation resLoc, int x, int y, int linkSide, int widthScaled, int heightScaled, int width, int height, int textureX, int textureY) {
        renderRectTextureCustomSize(graphics, resLoc, x, y, linkSide, widthScaled, heightScaled, width, height, textureX, textureY, 256, 256);
    }

    public void renderRectTextureCustomSize(GuiGraphics graphics, ResourceLocation resLoc, int x, int y, int linkSide, int widthScaled, int heightScaled, int width, int height, int textureX, int textureY, int textureMaxX, int textureMaxY) {
        int offsetX = widthScaled / 2 * ((linkSide - 1) % 3);
        int offsetY = heightScaled / 2 * ((linkSide - 1) / 3);
        graphics.blit(resLoc, x + offsetX, y + offsetY, textureX, textureY, width, height, textureMaxX, textureMaxY);
    }
}