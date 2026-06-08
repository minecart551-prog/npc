package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import noppes.npcs.client.CustomNpcResourceListener;

public class GuiLabel extends AbstractWidget implements GuiEventListener {
    public int id;
    private boolean centered = false;
    public boolean enabled = true;
    private boolean labelBgEnabled;
    private int textColor;
    private int backColor;
    private int ulColor;
    private int brColor;
    private int border;

    public GuiLabel(int id, Component label, int color, int x, int y, int width, int height) {
        super(x, y, width, height, label);
        this.id = id;
        this.textColor = color;
        this.width = Minecraft.getInstance().font.width(getMessage());
    }

    public GuiLabel(int id, String s, int x, int y) {
        this(id, Component.translatable(s), CustomNpcResourceListener.DefaultTextColor, x, y, 40, 0);
    }

    public GuiLabel(int id, String s, int x, int y, String tooltip) {
        this(id, Component.translatable(s), CustomNpcResourceListener.DefaultTextColor, x, y, 40, 10);
        setTooltip(Tooltip.create(Component.translatable(tooltip).setStyle(Style.EMPTY.withColor(0xffc65c))));
    }

    public GuiLabel(int id, String s, int x, int y, int color) {
        this(id, Component.translatable(s), color, x, y, 40, 0);
    }

    public GuiLabel(int id, String s, int x, int y, int width, int height) {
        this(id, Component.translatable(s), CustomNpcResourceListener.DefaultTextColor, x, y, width, height);
        centered = true;
    }

    public GuiLabel(int id, String s, int x, int y, int color, int width, int height) {
        this(id, Component.translatable(s), color, x, y, width, height);
        centered = true;
    }

    public void setColor(int color){
        this.textColor = color;
    }

    public void setCentered(boolean bo){
        this.centered = bo;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.enabled) {
            //RenderSystem.enableBlend();
            //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.drawBox(graphics);
            int i = this.getY() + this.height / 2 + this.border / 2;

            if (this.centered) {
                graphics.drawString(Minecraft.getInstance().font, getMessage(), (int)(getX() + (width - Minecraft.getInstance().font.width(getMessage())) / 2f), getY(), textColor, false);
                //this.drawCenteredString(stack, Minecraft.getInstance().font, getMessage(), this.x + this.width / 2, i, this.textColor);
            } else {
                graphics.drawString(Minecraft.getInstance().font, getMessage(), getX(), getY(), textColor, false);
                //this.drawString(stack, Minecraft.getInstance().font, getMessage(), this.x, i, this.textColor);
            }
            super.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    protected void drawBox(GuiGraphics graphics) {
        if (this.labelBgEnabled) {
            int i = this.width + this.border * 2;
            int j = this.height + this.border * 2;
            int k = this.getX() - this.border;
            int l = this.getY() - this.border;
            graphics.fill(k, l, k + i, l + j, this.backColor);
            graphics.hLine(k, k + i, l, this.ulColor);
            graphics.hLine(k, k + i, l + j, this.brColor);
            graphics.hLine(k, l, l + j, this.ulColor);
            graphics.hLine(k + i, l, l + j, this.brColor);
        }

    }
}
