package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiTooltipUtils;
import noppes.npcs.shared.client.gui.listeners.IGui;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;
import java.util.ArrayList;

public class GuiBasicContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IGuiInterface {
    public boolean drawDefaultBackground = true;
    public int guiLeft, guiTop;
    public LocalPlayer player;

    public GuiWrapper wrapper = new GuiWrapper(this);

    public String title;
    public boolean closeOnEsc = true;
    public int mouseX, mouseY;

    public GuiBasicContainer(T cont, Inventory inv, Component titleIn) {
        super(cont, inv, titleIn);
        this.player = Minecraft.getInstance().player;
        title = "";

        this.minecraft = Minecraft.getInstance();
        this.font = minecraft.font;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return closeOnEsc;
    }

    @Override
    public void init() {
        super.init();
        setFocused(null);
        guiLeft = (width - imageWidth) / 2;
        guiTop = (height - imageHeight) / 2;
        renderables.clear();
        children().clear();

        wrapper.init(minecraft, width, height);
    }

    public ResourceLocation getResource(String texture) {
        return new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
    }
//
//	@Override
//	public GuiEventListener getFocused(){
//		return null;
//	}

    @Override
    public void containerTick() {
        wrapper.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrolled) {
        if (wrapper.mouseScrolled(mouseX, mouseY, scrolled))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrolled);
    }

    @Override
    public boolean mouseClicked(double i, double j, int k) {
        if (wrapper.mouseClicked(i, j, k))
            return true;
        return super.mouseClicked(i, j, k);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (wrapper.mouseDragged(x, y, button, dx, dy))
            return true;
        if (this.getFocused() != null && this.isDragging() && button == 0) {
            this.getFocused().mouseDragged(x, y, button, dx, dy);
            return true;
        }
        return super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (wrapper.mouseReleased(x, y, button))
            return true;
        return super.mouseReleased(x, y, button);
    }

    @Override
    public void elementClicked() {
        if (wrapper.subgui != null){
            ((IGuiInterface)wrapper.subgui).elementClicked();
        }
    }

    @Override
    public void subGuiClosed(Screen subgui) {

    }

    @Override
    public GuiWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public void initGui() {
        init();
    }

    public boolean isInventoryKey(int i) {
        return minecraft.options.keyInventory.key.getValue() == i; //inventory key
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (wrapper.charTyped(c, i)) {
            return true;
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (wrapper.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_))
            return true;
//		InputConstants.Input mouseKey = InputConstants.getKey(key, p_keyPressed_2_);
//		if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)){
//			return true;
//		}
        return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
    }



    @Override
    public void setFocused(GuiEventListener gui) {
        if (wrapper.subgui != null) {
            wrapper.subgui.setFocused(gui);
        } else {
            if (gui != null && !this.children().contains(gui)) {
                return;
            }
            wrapper.changeFocus(getFocused(), gui);
            super.setFocused(gui);
        }
    }

    @Override
    public GuiEventListener getFocused() {
        if (wrapper.subgui != null) {
            return wrapper.subgui.getFocused();
        }
        return super.getFocused();
    }

    public void buttonEvent(Button guibutton) {
    }

    ;

    public void close() {
        save();
        player.closeContainer();
        setScreen(null);
        minecraft.mouseHandler.grabMouse();
    }

    @Override
    public void onClose() {
        this.close();
        GuiTextFieldNop.unfocus();
    }

    public void addButton(GuiButtonNop button) {
        wrapper.npcbuttons.put(button.id, button);
        super.addRenderableWidget(button);
    }

    public void addTopButton(GuiMenuTopButton button) {
        wrapper.topbuttons.put(button.id, button);
        super.addRenderableWidget(button);
    }

    public void addSideButton(GuiMenuSideButton button) {
        wrapper.sidebuttons.put(button.id, button);
        super.addRenderableWidget(button);
    }

    public GuiButtonNop getButton(int i) {
        return wrapper.npcbuttons.get(i);
    }

    public GuiMenuSideButton getSideButton(int i) {
        return wrapper.sidebuttons.get(i);
    }

    public GuiMenuTopButton getTopButton(int i) {
        return wrapper.topbuttons.get(i);
    }

    public void addTextField(GuiTextFieldNop tf) {
        wrapper.textfields.put(tf.id, tf);
    }

    public GuiTextFieldNop getTextField(int i) {
        return wrapper.textfields.get(i);
    }

    public void add(IGui gui) {
        wrapper.components.add(gui);
    }

    public IGui get(int id) {
        for (IGui comp : wrapper.components) {
            if (comp.getID() == id)
                return comp;
        }
        return null;
    }

    public void addLabel(GuiLabel label) {
        wrapper.labels.put(label.id, label);
    }

    public GuiLabel getLabel(int i) {
        return wrapper.labels.get(i);
    }

    public void addSlider(GuiSliderNop slider) {
        wrapper.sliders.put(slider.id, slider);
        addRenderableWidget(slider);
    }

    public GuiSliderNop getSlider(int i) {
        return wrapper.sliders.get(i);
    }

    public void addScroll(GuiCustomScrollNop scroll) {
        scroll.init(minecraft, scroll.width, scroll.height);
        wrapper.scrolls.put(scroll.id, scroll);
    }

    public GuiCustomScrollNop getScroll(int id) {
        return wrapper.scrolls.get(id);
    }


    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        //super.renderLabels(p_281635_, p_282681_, p_283686_);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y){

    }

    @Override
    public void buttonEvent(GuiButtonNop button) {

    }

    public void save(){

    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.wrapper.mouseX = mouseX;
        this.wrapper.mouseY = mouseY;
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        ArrayList<Slot> slots = new ArrayList(this.menu.slots);
        if (wrapper.subgui != null) {
            this.menu.slots.clear();
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(getFontRenderer(), I18n.get(title), width / 2, guiTop - 8, 0xffffff);
        for (GuiLabel label : new ArrayList<GuiLabel>(wrapper.labels.values()))
            label.render(graphics, mouseX, mouseY, partialTicks);
        for (GuiTextFieldNop tf : new ArrayList<GuiTextFieldNop>(wrapper.textfields.values()))
            tf.renderWidget(graphics, mouseX, mouseY, partialTicks);
        for (GuiCustomScrollNop scroll : new ArrayList<GuiCustomScrollNop>(wrapper.scrolls.values()))
            scroll.render(graphics, mouseX, mouseY, partialTicks);
        for (IGui comp : new ArrayList<IGui>(wrapper.components)) {
            comp.render(graphics, mouseX, mouseY);
            for (Screen gui : new ArrayList<Screen>(wrapper.extra.values()))
                gui.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (wrapper.subgui != null) {
            this.menu.slots.addAll(slots);
            graphics.pose().pushPose();
            graphics.pose().translate(0,0,100);
            //RenderHelper.disableStandardItemLighting();
            wrapper.subgui.render(graphics, mouseX, mouseY, partialTicks);
            graphics.pose().popPose();
        } else {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    public void renderTooltip(GuiGraphics p_283594_, int p_282171_, int p_281909_) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();
            GuiTooltipUtils.renderTooltip(p_283594_, this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, p_282171_, p_281909_);
        }

    }


    @Override
    public void renderBackground(GuiGraphics graphics) {
        if (drawDefaultBackground && wrapper.subgui == null)
            super.renderBackground(graphics);
    }

    public Font getFontRenderer() {
        return this.font;
    }

    public void setScreen(Screen gui) {
        this.minecraft.setScreen(gui);
    }


    public void setSubGui(Screen gui) {
        wrapper.setSubgui(gui);
        init();
    }

    public boolean hasSubGui() {
        return wrapper.subgui != null;
    }

    public Screen getSubGui() {
        return wrapper.getSubGui();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Screen getParent() {
        return wrapper.getParent();
    }
}
