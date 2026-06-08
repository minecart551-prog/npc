package noppes.npcs.client.gui.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IComponentsWrapper;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.wrapper.gui.*;
import noppes.npcs.client.gui.custom.components.*;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;

import java.util.*;

public class GuiCustomComponents {
    public static final ResourceLocation resource = new ResourceLocation("customnpcs:textures/gui/components.png");

    public Map<Integer, IGuiComponent> components = new HashMap<>();
    protected List<IItemSlot> slots = new ArrayList<>();

    protected int draggingId = -1;

    public void setComponents(GuiCustom gui, IComponentsWrapper comps){
        Map<Integer, IGuiComponent> components = new HashMap<>();

        for(ICustomGuiComponent comp : comps.getComponents()){
            switch (comp.getType()) {
                case GuiComponentType.BUTTON:
                    CustomGuiButton button = new CustomGuiButton(gui, (CustomGuiButtonWrapper) comp);
                    components.put(button.getID(), button);
                    break;
                case GuiComponentType.BUTTON_LIST:
                    components.put(comp.getID(), new CustomGuiButtonList(gui, (CustomGuiButtonListWrapper) comp));
                    break;
                case GuiComponentType.LABEL:
                    CustomGuiLabel lbl = new CustomGuiLabel(gui, (CustomGuiLabelWrapper)comp);
                    components.put(lbl.getID(), lbl);
                    break;
                case GuiComponentType.TEXT_FIELD:
                    CustomGuiTextField textField = new CustomGuiTextField(gui, (CustomGuiTextFieldWrapper)comp);
                    components.put(textField.id, textField);
                    break;
                case GuiComponentType.TEXT_AREA:
                    CustomGuiTextArea textArea = new CustomGuiTextArea(gui, (CustomGuiTextAreaWrapper)comp);
                    components.put(textArea.id, textArea);
                    break;
                case GuiComponentType.TEXTURED_RECT:
                    CustomGuiTexturedRect rect = new CustomGuiTexturedRect(gui, (CustomGuiTexturedRectWrapper)comp);
                    components.put(rect.getID(), rect);
                    break;
                case GuiComponentType.SCROLL:
                    CustomGuiScroll scroll = new CustomGuiScroll(gui, (CustomGuiScrollWrapper) comp);
                    components.put(scroll.getID(), scroll);
                    break;
                case GuiComponentType.SLIDER:
                    CustomGuiSlider slider = new CustomGuiSlider(gui, (CustomGuiSliderWrapper) comp);
                    components.put(slider.getID(), slider);
                    break;
                case GuiComponentType.ENTITY_DISPLAY:
                    CustomGuiEntityDisplay display = new CustomGuiEntityDisplay(gui, (CustomGuiEntityDisplayWrapper)comp);
                    components.put(display.getID(), display);
                    break;
                case GuiComponentType.ASSETS_SELECTOR:
                    CustomGuiAssetsSelector assets = new CustomGuiAssetsSelector(gui, (CustomGuiAssetsSelectorWrapper) comp);
                    components.put(assets.getID(), assets);
                    break;
                case GuiComponentType.COLORED_LINE:
                    CustomGuiColoredLine coloredLine = new CustomGuiColoredLine(gui, (CustomGuiColoredLineWrapper) comp);
                    components.put(coloredLine.getID(), coloredLine);
                    break;
                case GuiComponentType.ITEM_RENDERER: {
                    final CustomGuiItemRenderer itemRenderer = new CustomGuiItemRenderer(gui, (CustomGuiItemRendererWrapper) comp);
                    components.put(itemRenderer.getID(), itemRenderer);
                    break;
                }
            }
        }
        this.components = components;
        List<IItemSlot> slots = new ArrayList<>();
        slots.addAll(comps.getSlots());
        slots.addAll(comps.getPlayerSlots());
        this.slots = slots;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        for(IGuiComponent component : components.values()) {
            component.onRender(graphics, mouseX, mouseY, partialTicks);
        }
        for(IItemSlot slot : slots){
            if(slot.getGuiType() > 0){
                renderSlot(graphics, slot);
            }
        }
        for(IGuiComponent component : components.values()) {
            component.onRenderPost(graphics, mouseX, mouseY, partialTicks);
        }
    }

    public void renderSlot(GuiGraphics graphics, IItemSlot slot){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        graphics.blit(resource, slot.getPosX() - 1, slot.getPosY() - 1, 0, 80, (slot.getGuiType() - 1) * 18, 18, 18, 256, 256);
    }

    public void containerTick() {
        for(IGuiComponent component : components.values()) {
            if(component instanceof EditBox editBox) {
                editBox.tick();
            }
            if(component instanceof CustomGuiSlider slider) {
                slider.tick();
            }
        }
    }

    public boolean charTyped(char typedChar, int keyCode) {
        for(IGuiComponent comp : components.values()){
            if(comp instanceof GuiEventListener guiEvent){
                if(guiEvent.charTyped(typedChar, keyCode)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        for(IGuiComponent comp : components.values()){
            if(comp instanceof GuiEventListener guiEvent){
                if(guiEvent.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)  {
        boolean hasClickedAny = false;
        for(IGuiComponent comp : components.values()){
            if(comp instanceof GuiEventListener guiEvent){
                if(guiEvent.mouseClicked(mouseX, mouseY, mouseButton)){
                    if(mouseButton == 0){
                        draggingId = comp.getID();
                    }
                    hasClickedAny = true;
                }
            }
        }
        return hasClickedAny;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy)  {
        for(IGuiComponent comp : components.values()){
            if(comp instanceof GuiEventListener guiEvent){
                if(comp.getID() == draggingId && guiEvent.mouseDragged(x, y, button, dx, dy)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(double x, double y, int button)  {
        for(IGuiComponent comp : components.values()){
            if(comp.getID() == draggingId && comp instanceof GuiEventListener guiEvent){
                if(guiEvent.mouseReleased(x, y, button)){
                    draggingId = -1;
                    return true;
                }
            }
        }
        draggingId = -1;
        return false;
    }
}
