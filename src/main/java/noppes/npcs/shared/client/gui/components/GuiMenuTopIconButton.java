package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;

import java.util.Arrays;
import java.util.List;

public class GuiMenuTopIconButton extends GuiMenuTopButton{
    private static final ResourceLocation resource = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    protected static ItemRenderer itemRenderer;
    private ItemStack item;
    
	public GuiMenuTopIconButton(IGuiInterface gui, int i, int x, int y, String s, ItemStack item) {
		super(gui, i, x, y, s);
		width = 28;
		height = 28;
		this.item = item;
		itemRenderer = Minecraft.getInstance().getItemRenderer();
	}
	
    public GuiMenuTopIconButton(IGuiInterface gui, int i, GuiButtonNop parent, String s, ItemStack item){
    	super(gui, i, parent, s);
		width = 28;
		height = 28;
		this.item = item;
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

	@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible){
            return;
        }
        if(item.isEmpty())
        	item = new ItemStack(Blocks.DIRT);
        hover = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + height;
        Minecraft mc = Minecraft.getInstance();
        if(hover){

            drawHoveringText(graphics, Arrays.asList(getMessage()), mouseX, mouseY, Minecraft.getInstance().font);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        graphics.pose().pushPose();
        //RenderSystem.enableBlend();
        //RenderSystem.disableLighting();
        graphics.blit(resource, getX(), getY() + (active?2:0), 0, active?32:0, 28, 28);
        graphics.pose().translate(0,0,100);
        //RenderSystem.enableLighting();
        //RenderSystem.enableRescaleNormal();
        //RenderHelper.enableGUIStandardItemLighting();
        graphics.renderItem(item, getX() + 6, getY() + 10);
        graphics.renderItemDecorations(mc.font, item, getX() + 6, getY() + 10);
        ////RenderHelper.disableStandardItemLighting();
        //RenderSystem.disableLighting();
        graphics.pose().popPose();
    }

    protected void drawHoveringText(GuiGraphics graphics, List<Component> list, int x, int y, Font font) {
        if (list.isEmpty())
            return;

        //RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int k = 0;

        for (Component o : list) {
            int l = font.width(o);

            if (l > k) {
                k = l;
            }
        }

        int j2 = x;
        int k2 = y;
        int i1 = 8;

        if (list.size() > 1) {
            i1 += 2 + (list.size() - 1) * 10;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,300);
        int j1 = -267386864;
        graphics.fillGradient(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
        graphics.fillGradient( j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
        graphics.fillGradient( j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
        graphics.fillGradient( j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
        graphics.fillGradient( j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
        int k1 = 1347420415;
        int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
        graphics.fillGradient(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
        graphics.fillGradient( j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
        graphics.fillGradient( j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
        graphics.fillGradient( j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

        for (int i2 = 0; i2 < list.size(); ++i2) {
            Component s1 = list.get(i2);
            graphics.drawString(font, s1, j2, k2, -1);

            if (i2 == 0) {
                k2 += 2;
            }

            k2 += 10;
        }

        graphics.pose().popPose();
        RenderSystem.enableDepthTest();
        //RenderSystem.enableRescaleNormal();
    }
}
