package noppes.npcs.client.gui.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.GuiComponentsScrollableWrapper;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.mixin.MouseHelperMixin;
import noppes.npcs.util.ValueUtil;

public class GuiCustomScrollingPanel extends GuiCustomComponents{
    public GuiComponentsScrollableWrapper comps;

    private int maxSize = 0;
    private int scrollMaxHeight = 0;
    private int scrollPercentage = 0;
    private GuiCustom gui;

    private boolean isScrolling = false;

    private final CustomGuiTexturedRect scrollbar = new CustomGuiTexturedRect( null,
            new CustomGuiTexturedRectWrapper( -1, resource.toString(),0, 0, 14, 64, 65, 0)
                    .setRepeatingTexture(14, 64, 1));
    private final CustomGuiTexturedRect button = new CustomGuiTexturedRect(null,
            new CustomGuiTexturedRectWrapper(-1, resource.toString(),0, 0, 12, 15, 0, 214));

    public void setComponents(GuiCustom gui, GuiComponentsScrollableWrapper comps){
        super.setComponents(gui, comps);
        this.gui = gui;
        this.comps = comps;
        this.button.x = comps.width - 13;
        this.scrollbar.x = comps.width - 14;
        this.scrollbar.height = comps.height;
        this.scrollMaxHeight = comps.height - 17;//15 for scroll element and 2 for scrollbar border
        this.maxSize = comps.getComponents().stream()
                .mapToInt(v -> v.getPosY() + v.getHeight())
                .max().orElse(0);
        if(!this.canScroll()){
            scrollPercentage = 0;
            comps.scrollAmount = 0;
        }
        else{
            setScrollAmount(scrollPercentage * (maxSize - comps.height) / 100);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        mouseX -= comps.x;
        mouseY -= comps.y;
        matrixStack.pushPose();
        //matrixStack.translate(comps.x, comps.y, 10);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(comps.x, comps.y, 10);
        RenderSystem.applyModelViewMatrix();
        if(canScroll()){
            scrollbar.onRender(graphics, mouseX, mouseY, partialTicks);

            if(isScrolling){
                if(((MouseHelperMixin)Minecraft.getInstance().mouseHandler).getActiveButton() == 0){
                    scrollPercentage = ValueUtil.CorrectInt((mouseY - 7) * 100 / scrollMaxHeight, 0, 100);
                }
                else{
                    isScrolling = false;
                }
            }

            button.textureX = 0;
            if(scrollButtonHovered(mouseX, mouseY) || isScrolling){
                button.textureX = 24;
            }
            button.y = 1 + (scrollPercentage * scrollMaxHeight / 100);
            button.onRender(graphics, mouseX, mouseY, partialTicks);

            setScrollAmount(scrollPercentage * (maxSize - comps.height) / 100);
            matrixStack.translate(0, -comps.scrollAmount, 0);

            for(ICustomGuiComponent component : comps.getComponents()) {
                if(comps.isVisible(component)){
                    components.get(component.getID()).onRender(graphics, mouseX, mouseY + comps.scrollAmount, partialTicks);
                }
            }
            for(IItemSlot slot : slots){
                if(comps.isVisible(slot) && slot.getGuiType() > 0){
                    renderSlot(graphics, slot);
                }
            }
            for(ICustomGuiComponent component : comps.getComponents()) {
                if(comps.isVisible(component)){
                    components.get(component.getID()).onRenderPost(graphics, mouseX, mouseY + comps.scrollAmount, partialTicks);
                }
            }
        }
        else{
            super.render(graphics, mouseX, mouseY, partialTicks);
        }

        matrixStack.popPose();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void setScrollAmount(int amount){
        if(amount == comps.scrollAmount){
            return;
        }
        comps.scrollAmount = amount;
        gui.getMenu().update();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)  {
        mouseX -= comps.x;
        mouseY -= comps.y;

        if(!canScroll()) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (scrollBarHovered(mouseX, mouseY) && mouseButton == 0) {
            isScrolling = true;
            scrollPercentage = ValueUtil.CorrectInt((int) (mouseY - 7) * 100 / scrollMaxHeight, 0, 100);
            return true;
        }
        boolean clicked = false;
        for(ICustomGuiComponent component : comps.getComponents()) {
            if(comps.isVisible(component)) {
                IGuiComponent comp = components.get(component.getID());
                if (comp instanceof GuiEventListener guiEvent) {
                    if (guiEvent.mouseClicked(mouseX, mouseY + comps.scrollAmount, mouseButton)) {
                        if(mouseButton == 0){
                            draggingId = comp.getID();
                        }
                        clicked = true;
                    }
                }
            }
        }
        return clicked;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)  {
        if(isScrolling || draggingId < 0)
            return false;
        mouseX -= comps.x;
        mouseY -= comps.y;

        if(!canScroll()) {
            return super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
        }

        for(ICustomGuiComponent component : comps.getComponents()) {
            if(comps.isVisible(component)) {
                IGuiComponent comp = components.get(component.getID());
                if (comp instanceof GuiEventListener guiEvent) {
                    if (component.getID() == draggingId && guiEvent.mouseDragged(mouseX, mouseY + comps.scrollAmount, mouseButton, dx, dy)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton)  {
        mouseX -= comps.x;
        mouseY -= comps.y;

        if(!canScroll()) {
            return super.mouseReleased(mouseX, mouseY, mouseButton);
        }

        for(ICustomGuiComponent component : comps.getComponents()) {
            if(comps.isVisible(component)) {
                IGuiComponent comp = components.get(component.getID());
                if (comp instanceof GuiEventListener guiEvent) {
                    if (component.getID() == draggingId && guiEvent.mouseReleased(mouseX, mouseY + comps.scrollAmount, mouseButton)) {
                        draggingId = -1;
                        return true;
                    }
                }
            }
        }
        draggingId = -1;
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double mouseScrolled) {
        if(mouseScrolled != 0 && panelHovered(mouseX - comps.x, mouseY - comps.y)){
            scrollPercentage += mouseScrolled > 0?-4:4;
            scrollPercentage = ValueUtil.CorrectInt(scrollPercentage, 0, 100);
            return true;
        }
        return false;
    }

    public boolean canScroll(){
        return maxSize > comps.height;
    }

    public boolean panelHovered(double x, double y){
        return canScroll() && x >= 0 && y >= 0 && x < comps.width && y < comps.height;
    }

    private boolean scrollBarHovered(double x, double y){
        return panelHovered(x, y) && x >= scrollbar.x && y >= scrollbar.y && x < scrollbar.x + scrollbar.width && y < scrollbar.y + scrollbar.height;
    }
    private boolean scrollButtonHovered(double x, double y){
        return scrollBarHovered(x, y) && y > button.y && y < button.y + 15;
    }

    public void setMaxSize(int size){
        this.maxSize = size;
    }
}
