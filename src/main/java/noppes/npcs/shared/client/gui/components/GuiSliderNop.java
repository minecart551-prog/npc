package noppes.npcs.shared.client.gui.components;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import noppes.npcs.shared.client.gui.listeners.ISliderListener;

public class GuiSliderNop extends AbstractWidget {
    private ISliderListener listener;
    public int id;

    public float sliderValue = 1.0F;
    public float startValue = 1.0F;

    public static final int UNSET_FG_COLOR = -1;
    protected int packedFGColor = UNSET_FG_COLOR;
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;
        return this.active ? 16777215 : 10526880; // White : Light Grey
    }
    public void setFGColor(int color) {
        this.packedFGColor = color;
    }
    public void clearFGColor() {
        this.packedFGColor = UNSET_FG_COLOR;
    }

    public GuiSliderNop(Screen parent, int id, int xPos, int yPos, String displayString, float sliderValue) {
        super(xPos, yPos, 150, 20, Component.translatable(displayString));
        this.id = id;
        this.sliderValue = sliderValue;
        this.startValue = sliderValue;
        listener = (ISliderListener) parent;
    }

    public GuiSliderNop(Screen parent, int id, int xPos, int yPos, float sliderValue) {
        this(parent, id, xPos, yPos, "", sliderValue);
        listener.mouseDragged(this);
    }

    public GuiSliderNop(Screen parent, int id, int xPos, int yPos, int width, int height, float sliderValue) {
        this(parent, id, xPos, yPos, "", sliderValue);
        this.width = width;
        this.height = height;
        listener.mouseDragged(this);
    }

    public void setString(String str) {
        setMessage(Component.translatable(str));
    }

    private void setSliderValue(float value){
        value = Mth.clamp(value, 0.0f, 1.0f);
        if(value == this.sliderValue)
            return;
        this.sliderValue = value;
        listener.mouseDragged(this);
    }

    @Override
    protected void renderWidget(GuiGraphics p_93676_, int p_93677_, int p_93678_, float p_93679_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = 0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        p_93676_.blit(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + i * 20, this.width / 2, this.height);
        p_93676_.blit(WIDGETS_LOCATION, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(p_93676_, minecraft, p_93677_, p_93678_);
        int j = getFGColor();
        p_93676_.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void onClick(double x, double y){
        if(!visible || !active)
            return;
        setSliderValue((float)(x - (this.getX() + 4)) / (float)(this.width - 8));
        super.onClick(x, y);
    }

    @Override
    protected void onDrag(double x, double y, double p_onDrag_5_, double p_onDrag_7_) {
        setSliderValue((float)(x - (this.getX() + 4)) / (float)(this.width - 8));
        super.onDrag(x, y, p_onDrag_5_, p_onDrag_7_);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    @Override
    public void onRelease(double x, double y) {
        if(sliderValue == startValue){
            return;
        }
        super.playDownSound(Minecraft.getInstance().getSoundManager());
        listener.mouseReleased(this);
        this.startValue = sliderValue;
    }

    public void renderBg(GuiGraphics graphics, Minecraft mc, int p_146119_2_, int p_146119_3_) {
        if(!visible)
            return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int lvt_4_1_ = (this.isHovered ? 2 : 1) * 20;
        graphics.blit(WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (double)(this.width - 8)), this.getY(), 0, 46 + lvt_4_1_, 4, 20);
        graphics.blit(WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (double)(this.width - 8)) + 4, this.getY(), 196, 46 + lvt_4_1_, 4, 20);
        //this.blit(this.x + (int)(this.sliderValue * (double)(this.width - 8)), this.y, 0, 66, 4, 20);
        //this.blit(this.x + (int)(this.sliderValue * (double)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
    }
}
