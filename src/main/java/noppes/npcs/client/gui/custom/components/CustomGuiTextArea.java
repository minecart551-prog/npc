package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiTextAreaWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.shared.client.gui.components.GuiTextArea;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiTextUpdate;

public class CustomGuiTextArea extends GuiTextArea implements IGuiComponent {

    GuiCustom parent;

    CustomGuiTextFieldWrapper component;

    public CustomGuiTextArea(GuiCustom parent, CustomGuiTextAreaWrapper component) {
        super(component.getID(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), "");
        this.component = component;
        this.parent = parent;
        this.init();
    }

    @Override
    public void init(){
        this.id = component.getID();
        this.x = component.getPosX();
        this.y = component.getPosY();
        this.width = component.getWidth();
        this.height = component.getHeight();
        if(component.getText()!=null && !component.getText().isEmpty())
            setText(component.getText());
        enabled = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        if(!this.visible){
            return;
        }
        matrixStack.pushPose();
        matrixStack.translate(0, 0, id);
        boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        super.render(graphics, mouseX, mouseY);
        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
        matrixStack.popPose();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        String text = getText();
        boolean bo = super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        if(!text.equals(getText())){
            component.setText(getText());
            Packets.sendServer(new SPacketCustomGuiTextUpdate(component.getUniqueID(), getText()));
        }
        return bo;
    }

    @Override
    public boolean charTyped(char c, int i) {
        String text = getText();
        boolean bo = super.charTyped(c, i);
        if(!text.equals(getText())){
            component.setText(getText());
            Packets.sendServer(new SPacketCustomGuiTextUpdate(component.getUniqueID(), getText()));
        }
        return bo;
    }

    @Override
    public ICustomGuiComponent component() {
        return component;
    }
}
