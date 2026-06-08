package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;

public class GuiMenuTopButton extends GuiButtonNop
{
	public static final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/menutopbutton.png");
    protected int height;
    public boolean active;
    public boolean hover = false;
    public boolean rotated = false;

    public GuiMenuTopButton(IGuiInterface gui, int i, int j, int k, String s) {
    	super(gui, i, j, k, s);
        active = false;

        width = Minecraft.getInstance().font.width(getMessage()) + 12;
        height = 20;
    }

    public GuiMenuTopButton(IGuiInterface gui, int i, GuiButtonNop parent, String s) {
    	this(gui, i, parent.getX() + parent.getWidth(), parent.getY(), s);
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float partialTicks) {
        if (!visible) {
            return;
        }
        PoseStack matrixStack = graphics.pose();
        Minecraft mc = Minecraft.getInstance();
        matrixStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        int height = this.height - (active?0:2);
        hover = i >= getX() && j >= getY() && i < getX() + getWidth() && j < getY() + height;

        int k = this.active ? 0 : (hover ? 2 : 1);
        graphics.blit(resource,getX(), getY(), 0,  k * 20, getWidth() / 2, height);
        graphics.blit(resource,getX() + getWidth() / 2, getY(), 200 - getWidth() / 2,  k * 20, getWidth() / 2, height);
        //mouseDragged(mc, i, j);
        Font fontrenderer = mc.font;
        if(rotated)
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        if (active)
        {
            graphics.drawCenteredString(fontrenderer, getMessage(), getX() + getWidth() / 2, getY() + (height - 8) / 2, 0xffffa0);
        }
        else if (hover)
        {
            graphics.drawCenteredString(fontrenderer, getMessage(), getX() + getWidth() / 2, getY() + (height - 8) / 2, 0xffffa0);
        }
        else
        {
            graphics.drawCenteredString(fontrenderer, getMessage(), getX() + getWidth() / 2, getY() + (height - 8) / 2, 0xe0e0e0);
        }
        matrixStack.popPose();
    }

	@Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        return false;
    }

    @Override
    public boolean mouseReleased(double i, double j, int button)
    {
        return false;
    }

    @Override
    public boolean mouseClicked(double i, double j, int button) {
    	boolean bo = !active && visible && hover;
    	if(bo){
            onClick(i, j);
    	}
        return bo;
    }

    @Override
    public void onClick(double x, double y) {
        gui.buttonEvent(this);
    }
}
