package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.gui.custom.interfaces.*;
import noppes.npcs.client.gui.custom.*;
import net.minecraft.client.*;
import noppes.npcs.api.wrapper.gui.*;
import noppes.npcs.api.gui.*;

public class CustomGuiItemRenderer extends AbstractWidget implements IGuiComponent {
    private GuiCustom parent;
    public CustomGuiItemRendererWrapper component;
    private ItemStack stack;
    public int id;
    Minecraft minecraft;
    public CustomGuiItemRenderer(GuiCustom parent, CustomGuiItemRendererWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.empty());
        this.component = component;
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();
        this.init();
    }

    public void init() {
        this.id = this.component.getID();
        this.setX(this.component.getPosX());
        this.setY(this.component.getPosY());
        this.setWidth(this.component.getWidth());
        this.height = (this.component.getHeight());
        if(component.hasStack()){
            this.stack = component.getStack().getMCItemStack();
        }else {
            this.stack = ItemStack.EMPTY;
        }

        this.active = true;
        this.visible = true;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            if (!NoppesUtilServer.IsItemStackNull(stack)) {
                double scale = component.getScale();
                graphics.pose().pushPose();
                graphics.pose().scale((float) scale, (float) scale, 1);
                graphics.pose().translate(0,0,id);
                graphics.renderItem(stack, (int) (getX()/scale), (int) (getY()/scale));
                graphics.renderItemDecorations(minecraft.font, stack, (int) (getX()/scale), (int) (getY()/scale));
                graphics.pose().popPose();
            }

            boolean hovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + this.width && mouseY < getY() + this.height;
            if (hovered && this.component.hasHoverText()) {
                this.parent.hoverText = this.component.getHoverTextList();
            }

        }
    }

    protected int getYImage(boolean p_getYImage_1_) {
        return 0;
    }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    public static CustomGuiEntityDisplay fromComponent(GuiCustom parent, CustomGuiEntityDisplayWrapper component) {
        CustomGuiEntityDisplay btn = new CustomGuiEntityDisplay(parent, component);
        return btn;
    }

    public ICustomGuiComponent component() {
        return this.component;
    }
}
