package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.shared.client.gui.components.GuiLabel;

import java.util.List;

public class CustomGuiLabel extends AbstractWidget implements IGuiComponent {
    private CustomGuiLabelWrapper component;
    private int id;
    private GuiCustom parent;
    private String[] lines;

    public CustomGuiLabel(GuiCustom parent, CustomGuiLabelWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.literal(component.getText()));
        this.component = component;
        this.parent = parent;
        init();
    }

    @Override
    public void init() {
        this.id = component.getID();
        this.setX(component.getPosX());
        this.setY(component.getPosY());
        this.setWidth(component.getWidth());
        this.height = (component.getHeight());
        this.active = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
        setMessage(Component.literal(component.getText()));
        this.lines = component.getText().split("\n");
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        if(!this.active){
            return;
        }
        matrixStack.pushPose();
        matrixStack.translate(0, 0, id);
        matrixStack.scale(component.getScale(), component.getScale(), 0);
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 2;
        boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        for (int i = 0; i < lines.length; i++) {
            Component line = Component.literal(lines[i]);
            int yPos = (int) (getY() / component.getScale()) + i * lineHeight;
            if (this.component.getCentered()) {
                graphics.drawString(font, line, (int)((getX() + (width - font.width(line)) / 2f) / component.getScale()), yPos, this.component.getColor());
            } else {
                graphics.drawString(font, line, (int) (getX() / component.getScale()), yPos, this.component.getColor());
            }
        }
        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
        matrixStack.popPose();
    }

    @Override
    public int getID() {
        return id;
    }


    public void setText(String s) {
        setMessage(Component.literal(s));
        this.lines = s.split("\n");
    }

    @Override
    public ICustomGuiComponent component() {
        return component;
    }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    @Override
    public void playDownSound(SoundManager p_93665_) { }

    public void setHeight(int height){
        this.height = height;
    }
}
