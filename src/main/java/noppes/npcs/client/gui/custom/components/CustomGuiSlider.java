package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiSliderWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiButton;
import noppes.npcs.packets.server.SPacketCustomGuiSliderUpdate;
import noppes.npcs.packets.server.SPacketCustomGuiTextUpdate;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CustomGuiSlider extends AbstractWidget implements IGuiComponent {

    private GuiCustom parent;
    private CustomGuiSliderWrapper component;
    private CustomGuiTextFieldWrapper tfcomponent;

    private CustomGuiTextField textfield;

    private float sliderValue;
    private float startValue;
    private long lastClickedTime = 0;

    private float total;

    public int id;

    private boolean disablePackets = false;
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

    public CustomGuiSlider(GuiCustom parent, CustomGuiSliderWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.translatable(component.getFormat(), component.getValue()));
        this.component = component;
        this.parent = parent;

        tfcomponent = new CustomGuiTextFieldWrapper(id, this.getX(), this.getY(), this.width, this.height).setCharacterType(3);
        init();
    }

    public void init(){
        this.id = component.getID();
        this.setX(component.getPosX());
        this.setY(component.getPosY());
        this.setWidth(component.getWidth());
        this.height = (component.getHeight());

        total = component.getMax() - component.getMin();
        this.startValue = this.sliderValue = (component.getValue() - component.getMin()) / total;
        tfcomponent.setID(this.id);
        tfcomponent.setPos(component.getPosX(), component.getPosY());
        tfcomponent.setSize(component.getWidth(), component.getHeight());
        this.active = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
        setMessage(Component.translatable(component.getFormat(), component.getValue()));
    }


    public CustomGuiSlider disablePackets(){
        disablePackets = true;
        return this;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        if(!this.visible){
            return;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
        if(textfield != null){
            textfield.onRender(graphics, mouseX, mouseY, partialTicks);
            if(!textfield.isFocused()){
                closeTextfield();
            }
        }
        boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
    }

    private void setSliderValue(float value){
        value = Mth.clamp(value, 0.0f, 1.0f);
        if(value == this.sliderValue)
            return;
        this.sliderValue = value;
        component.setValue(value * total + component.getMin());
        setMessage(Component.translatable(component.getFormat(), component.getValue()));
        if(!disablePackets){
            Packets.sendServer(new SPacketCustomGuiSliderUpdate(component.getUniqueID(), component.getValue()));
        }
        else {
            component.onChange(null);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(textfield != null && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)){
            closeTextfield();
        }
        if(textfield != null) {
            return textfield.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeTextfield(){
        setSliderValue((tfcomponent.getFloat() + component.getMin()) / total);
        textfield = null;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if(textfield != null){
            return textfield.charTyped(c, i);
        }
        return super.charTyped(c, i);
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

        long time = System.currentTimeMillis();
        if(time - lastClickedTime < 500) {
            tfcomponent.setText(component.getValue() + "");
            textfield = new CustomGuiTextField(parent, tfcomponent);
            textfield.setFocused(true);
        }
        else if(textfield != null){
            textfield.mouseClicked(x, y, 0);
            return;
        }
        lastClickedTime = time;
        setSliderValue((float)(x - (this.getX() + 4)) / (float)(this.width - 8));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(textfield != null){
            return textfield.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)  {
        setSliderValue((float)(mouseX - (this.getX() + 4)) / (float)(this.width - 8));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    public void tick(){
        if(textfield != null){
            textfield.tick();
        }
    }

    @Override
    public void onRelease(double x, double y) {
        if(sliderValue == startValue){
            return;
        }
        super.playDownSound(Minecraft.getInstance().getSoundManager());
        //listener.mouseReleased(this);
        this.startValue = sliderValue;
    }



    public void renderBg(GuiGraphics graphics, Minecraft mc, int p_146119_2_, int p_146119_3_) {
        if(!visible)
            return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int lvt_4_1_ = (this.isHovered ? 2 : 1) * 20;
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,10);
        graphics.blit(WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (double)(this.width - 8)), this.getY(), 0, 46 + lvt_4_1_, 4, height / 2);
        graphics.blit(WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (double)(this.width - 8)), this.getY() + height / 2, 0, 46 + lvt_4_1_ + 20 - height / 2, 4, height / 2);
        graphics.blit(WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (double)(this.width - 8)) + 4, this.getY(), 196, 46 + lvt_4_1_, 4, height / 2);
        graphics.blit(WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (double)(this.width - 8)) + 4, this.getY() + height / 2, 196, 46 + lvt_4_1_ + 20 - height / 2, 4, height / 2);
        graphics.pose().popPose();
        //this.blit(this.x + (int)(this.sliderValue * (double)(this.width - 8)), this.y, 0, 66, 4, 20);
        //this.blit(this.x + (int)(this.sliderValue * (double)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
    }

    @Override
    public ICustomGuiComponent component() {
        return component;
    }
}
