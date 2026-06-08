package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiFocusUpdate;
import noppes.npcs.packets.server.SPacketCustomGuiTextUpdate;

public class CustomGuiTextField extends EditBox implements IGuiComponent {

    private static CustomGuiTextField focused = null;

    private GuiCustom parent;
    private CustomGuiTextFieldWrapper component;

    public int id;

    public CustomGuiTextField(GuiCustom parent, CustomGuiTextFieldWrapper component) {
        super(Minecraft.getInstance().font, component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.translatable(component.getText()));
        setMaxLength(500);
        this.component = component;
        this.parent = parent;
        init();
    }

    @Override
    public void init(){
        this.id = component.getID();
        this.setX(component.getPosX());
        this.setY(component.getPosY());
        this.width = component.getWidth();
        this.height = component.getHeight();

        setTextColor(component.getColor());
        if(component.getText()!=null)
            setValue(component.getText());
        this.active = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
        this.setFocused(component.getFocused());
    }

    @Override
    public ICustomGuiComponent component() {
        return component;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(0, 0, id);
        boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        this.renderWidget(graphics, mouseX, mouseY, partialTicks);
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
        String text = getValue();
        boolean bo = super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        component.setText(getValue());
        if(!getValue().equals(component.getText())){
            this.setValue(component.getText());
        }
        if(!text.equals(getValue())){
            if(!component.disablePackets){
                Packets.sendServer(new SPacketCustomGuiTextUpdate(component.getUniqueID(), getValue()));
            }
            else{
                component.onChange(null);
            }
        }
        return bo;
    }

    @Override
    public boolean mouseClicked(double i, double j, int k){
        boolean flag = i >= (double)this.getX() && i < (double)(this.getX() + this.width) && j >= (double)this.getY() && j < (double)(this.getY() + this.height);
        this.setFocused(flag);
        return super.mouseClicked(i, j,k);
    }

    private boolean isValidChar(char c){
        if(component.getCharacterType() == 1){
            return Character.isDigit(c);
        }
        if(component.getCharacterType() == 2){
            return Character.isDigit(c) || (Character.toLowerCase(c) >= 'a' && Character.toLowerCase(c) <= 'f');
        }
        if(component.getCharacterType() == 3){
            return Character.isDigit(c) || c == '.' && !getValue().contains(".") || c == '-' && this.getCursorPosition() == 0;
        }
        return true;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if(!isValidChar(c)){
            return false;
        }
        String text = getValue();
        boolean bo = super.charTyped(c, i);
        if(!text.equals(getValue())){
            component.setText(getValue());
            if(!component.disablePackets){
                Packets.sendServer(new SPacketCustomGuiTextUpdate(component.getUniqueID(), getValue()));
            }
            else{
                component.onChange(null);
            }
        }
        return bo;
    }

    @Override
    public void setFocused(boolean bo){
        if(isFocused() == bo){
            return;
        }
        super.setFocused(bo);
        if(component.getFocused() != bo){
            if(!component.getText().isEmpty() && (component.getCharacterType() == 1 || component.getCharacterType() == 2)){
                component.setInteger(component.getInteger());
                setValue(component.getText());
                if(!component.disablePackets) {
                    Packets.sendServer(new SPacketCustomGuiTextUpdate(component.getUniqueID(), component.getText()));
                }
                component.onChange(null);
            }
            component.setFocused(bo);
            if(!component.disablePackets) {
                Packets.sendServer(new SPacketCustomGuiFocusUpdate(component.getUniqueID(), bo));
            }
            else{
                component.onFocusLost(null);
            }
        }
        if(isFocused() && focused != this){
            if(focused != null){
                focused.setFocused(false);
            }
            focused = this;
        }
        if(!isFocused() && focused != this)
        {
            focused = null;
        }
    }

    @Override
    public void playDownSound(SoundManager p_93665_) { }
}
