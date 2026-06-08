package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiColoredLineWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class CustomGuiColoredLine extends AbstractWidget implements IGuiComponent {
    private GuiCustom parent;
    public CustomGuiColoredLineWrapper component;
    public int id;

    public CustomGuiColoredLine(GuiCustom parent, CustomGuiColoredLineWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getXEnd() - component.getPosX(), component.getYEnd() - component.getPosY(), Component.empty());
        this.component = component;
        this.parent = parent;
        this.init();
    }

    public void init() {
        this.id = this.component.getID();
        this.setX(this.component.getPosX());
        this.setY(this.component.getPosY());
        this.setWidth(component.getXEnd() - component.getPosX());
        this.height = (component.getYEnd() - component.getPosY());
        this.active = true;
        this.visible = true;
    }

    public int getID() {
        return this.id;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int color = component.getColor();
            int r = color >> 24 & 0xff;
            int g = color >> 16 & 0xff;
            int b = color >> 8 & 0xff;
            int a = color & 0xff;

            double dx = component.getXEnd() - getX();
            double dy = component.getYEnd() - getY();
            double length = Math.sqrt(dx * dx + dy * dy);
            double nx = -dy / length * component.getThickness() / 2;
            double ny = dx / length * component.getThickness() / 2;

            VertexConsumer builder = graphics.bufferSource().getBuffer(RenderType.gui());
            builder.vertex(component.getXEnd() + nx, component.getYEnd() + ny, id).color(r,g,b,a).endVertex();
            builder.vertex(component.getXEnd() - nx, component.getYEnd() - ny,   id).color(r,g,b,a).endVertex();
            builder.vertex(getX() - nx, getY() - ny,   id).color(r,g,b,a).endVertex();
            builder.vertex(getX() + nx, getY() + ny, id).color(r,g,b,a).endVertex();
            graphics.flush();
        }
    }

    protected int getYImage(boolean p_getYImage_1_) {
        return 0;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        return true;
    }

    public static CustomGuiColoredLine fromComponent(GuiCustom parent, CustomGuiColoredLineWrapper component) {
        CustomGuiColoredLine line = new CustomGuiColoredLine(parent, component);
        return line;
    }

    public ICustomGuiComponent component() {
        return this.component;
    }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_169152_) {}

}
