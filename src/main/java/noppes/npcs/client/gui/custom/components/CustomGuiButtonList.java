package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonListWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiButtonList;

public class CustomGuiButtonList extends CustomGuiButton {
    private CustomGuiTexturedRect left;
    private CustomGuiTexturedRect right;
    private CustomGuiTexturedRectWrapper leftWrapper;
    private CustomGuiTexturedRectWrapper rightWrapper;

    private boolean isRight = false;

    public CustomGuiButtonList(GuiCustom parent, CustomGuiButtonListWrapper component) {
        super(parent, component);
        onPress = (button) -> {
            CustomGuiButtonList list = (CustomGuiButtonList)button;
            component.setSelected(component.getSelected() + (list.isRight ? 1 : -1));
            list.setMessage(Component.translatable(component.getLabel()));
            sendPacket();
            if(!component.disablePackets){
                Packets.sendServer(new SPacketCustomGuiButtonList(component.getUniqueID(), list.isRight));
            }
            else{
                component.onPress(parent.guiWrapper);
            }
        };
    }

    private void sendPacket(){
        Packets.sendServer(new SPacketCustomGuiButtonList(component.getUniqueID(), isRight));
    }

    public CustomGuiButtonList(GuiCustom parent, CustomGuiButtonListWrapper component, Button.OnPress onPress) {
        super(parent, component);
        this.component = component;
        this.onPress = onPress;
        init();
    }

    @Override
    public void init() {
        super.init();
        leftWrapper = ((CustomGuiButtonListWrapper)component).getLeftTexture();
        rightWrapper = ((CustomGuiButtonListWrapper)component).getRightTexture();
        this.left = new CustomGuiTexturedRect(parent, leftWrapper);
        this.right = new CustomGuiTexturedRect(parent, rightWrapper);
    }

    protected int getYImage(boolean p_93668_) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (p_93668_) {
            i = 2;
        }

        return i;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        super.onRender(graphics, mouseX, mouseY, partialTicks);
        matrixStack.pushPose();
        matrixStack.translate(getX(), getY(), 10);

        isRight = mouseX >=  getX() + width / 2;
        left.textureY = leftWrapper.getTextureY() + this.getYImage(this.hovered && !isRight) * leftWrapper.getHeight();
        left.onRender(graphics, mouseX - getX(), mouseY - getY(), partialTicks);

        right.textureY = rightWrapper.getTextureY() + this.getYImage(this.hovered && isRight) * rightWrapper.getHeight();
        right.onRender(graphics, mouseX - getX(), mouseY - getY(), partialTicks);

        renderLabel(graphics);

        matrixStack.popPose();
    }
}
