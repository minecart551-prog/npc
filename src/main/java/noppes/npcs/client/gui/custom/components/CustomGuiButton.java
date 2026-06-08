package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiButton;

public class CustomGuiButton extends Button implements IGuiComponent {
    protected GuiCustom parent;
    private CustomGuiTexturedRect background;
    public CustomGuiButtonWrapper component;
    protected boolean hovered;
    private int colour = 0xffffff;

    protected Button.OnPress onPress;


    public int id;

    public CustomGuiButton(GuiCustom parent, CustomGuiButtonWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.translatable(component.getLabel()), (btn)->{}, null);
        this.onPress = (button) -> {
            if(!component.disablePackets){
                Packets.sendServer(new SPacketCustomGuiButton(component.getUniqueID()));
            }
            else{
                component.onPress(parent.guiWrapper);
            }
        };
        this.parent = parent;
        this.component = component;
        this.init();
    }

    @Override
    public void onPress() {
        onPress.onPress(this);
    }

    @Override
    public void init() {
        this.id = component.getID();
        this.setX(component.getPosX());
        this.setY(component.getPosY());
        this.setWidth(component.getWidth());
        this.height = (component.getHeight());
        this.background = new CustomGuiTexturedRect(parent, component.getTextureRect());
        this.setMessage(Component.translatable(component.getLabel()));

        this.active = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        return false;
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
        matrixStack.pushPose();
        matrixStack.translate(getX(), getY(), 0);
        Minecraft mc = Minecraft.getInstance();

        this.hovered = isHovered(mouseX, mouseY);
        int i = 0;
        if(component.getTexture().equals("textures/gui/widgets.png")) {
            i = !this.active ? 0 : (this.hovered ? 2 : 1);
        }else{
            i = this.hovered ? 1 : 0;
        }
        background.textureY = component.getTextureY() + i * component.getTextureHoverOffset();
        background.onRender(graphics, mouseX - getX(), mouseY - getY(), partialTicks);
        //matrixStack.translate(0, 0, 10);

        renderLabel(graphics);

        if(!component.getDisplayItem().isEmpty()){
            int xx = (int)((width - 16f) / 2f);
            int yy = (int)((height - 16f) / 2f) + 1;
            graphics.pose().pushPose();
            graphics.pose().translate(0,0,-90);
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate(getX(), getY(), -90);
            RenderSystem.applyModelViewMatrix();
            graphics.renderItem(component.getDisplayItem().getMCItemStack(), xx, yy);
            graphics.renderItemDecorations(mc.font, component.getDisplayItem().getMCItemStack(), xx, yy);

            posestack.popPose();
            graphics.pose().popPose();
            RenderSystem.applyModelViewMatrix();
        }

        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
        matrixStack.popPose();
    }

    public void renderLabel(GuiGraphics graphics){
        if(!component.getLabel().isEmpty()){
            int j = 14737632;
            if (colour != 0) {
                j = colour;
            }
            else if (!this.active){
                j = 10526880;
            }
            else if (this.hovered){
                j = 16777120;
            }
            Minecraft mc = Minecraft.getInstance();
            graphics.pose().translate(0, 0, id);

            graphics.drawCenteredString(mc.font, this.getMessage(), this.width / 2, (this.height - 8) / 2, j);
        }
    }

    public boolean isHovered(int mouseX, int mouseY){
        return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }


    @Override
    public ICustomGuiComponent component() {
        return component;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    protected int hoverState(boolean mouseOver)
    {
        int i = 0;

        if (mouseOver)
        {
            i = 1;
        }

        return i;
    }

}
