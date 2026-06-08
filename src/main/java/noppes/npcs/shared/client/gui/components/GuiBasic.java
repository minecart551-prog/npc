package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.shared.client.gui.listeners.IGui;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;
import java.net.URI;
import java.util.ArrayList;

public class GuiBasic  extends Screen implements IGuiInterface {
    public LocalPlayer player;
    public boolean drawDefaultBackground = true;
    public String title;
    public ResourceLocation background = null;
    public boolean closeOnEsc = true;
    public int guiLeft, guiTop, imageWidth, imageHeight;

    public float bgScale = 1;

    public GuiWrapper wrapper = new GuiWrapper(this);

    public GuiBasic() {
        super(Component.empty());
        this.player = Minecraft.getInstance().player;
        this.minecraft = Minecraft.getInstance();
        title = "";
        imageWidth = 200;
        imageHeight = 222;

        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.font;
    }

    public void setBackground(String texture) {
        background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture) {
        return new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
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

    @Override
    public GuiWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public void initGui() {
        init();
    }

    @Override
    public void tick() {
        wrapper.tick();
    }

    public void buttonEvent(GuiButtonNop guibutton) {
    }

    ;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrolled) {
        if (wrapper.mouseScrolled(mouseX, mouseY, scrolled))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrolled);
    }

    @Override
    public boolean mouseClicked(double i, double j, int k) {
        if (wrapper.mouseClicked(i, j, k)) {
            return true;
        }
        return super.mouseClicked(i, j, k);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (wrapper.mouseDragged(x, y, button, dx, dy))
            return true;
        return super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (wrapper.mouseReleased(x, y, button))
            return true;
        return super.mouseReleased(x, y, button);
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

    public void elementClicked() {
        if (wrapper.subgui != null && wrapper.subgui instanceof GuiBasic){
            ((GuiBasic)wrapper.subgui).elementClicked();
        }
    }

    @Override
    public void subGuiClosed(Screen subgui) {

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
        return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
    }

    public boolean isInventoryKey(int i) {
        return minecraft.options.keyInventory.key.getValue() == i; //inventory key
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return closeOnEsc;
    }

    public void close() {
        onClose();
    }

    @Override
    public void onClose() {
        wrapper.close();
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

    public void save() {

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        this.wrapper.mouseX = mouseX;
        this.wrapper.mouseY = mouseY;
        int x = mouseX;
        int y = mouseY;
        if (wrapper.subgui != null) {
            x = y = 0;
        }
        if (drawDefaultBackground && wrapper.subgui == null) {
            renderBackground(graphics);
        }

        if (background != null) {
            matrixStack.pushPose();
            matrixStack.translate(guiLeft, guiTop, 0);
            matrixStack.scale(bgScale, bgScale, bgScale);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, background);
            if (imageWidth > 256) {
                graphics.blit(background, 0, 0, 0, 0, 250, imageHeight);
                graphics.blit(background, 250, 0, 256 - (imageWidth - 250), 0, imageWidth - 250, imageHeight);
            } else
                graphics.blit(background, 0, 0, 0, 0, imageWidth, imageHeight);
            matrixStack.popPose();
        }

        graphics.drawCenteredString(this.font, title, width / 2, 8, 0xffffff);
        for (GuiLabel label : new ArrayList<GuiLabel>(wrapper.labels.values()))
            label.render(graphics, mouseX, mouseY, partialTicks);
        for (GuiTextFieldNop tf : new ArrayList<GuiTextFieldNop>(wrapper.textfields.values())) {
            tf.renderWidget(graphics, x, y, partialTicks);
        }
        for (GuiCustomScrollNop scroll : new ArrayList<GuiCustomScrollNop>(wrapper.scrolls.values()))
            scroll.render(graphics, x, y, partialTicks);
        for (IGui comp : new ArrayList<IGui>(wrapper.components)) {
            comp.render(graphics, x, y);
        }
        for (Screen gui : new ArrayList<Screen>(wrapper.extra.values()))
            gui.render(graphics, x, y, partialTicks);
        super.render(graphics, x, y, partialTicks);
        if (wrapper.subgui != null) {
            matrixStack.translate(0, 0, 60F);
            wrapper.subgui.render(graphics, mouseX, mouseY, partialTicks);
            matrixStack.translate(0, 0, -60F);
        }
    }

    public Font getFontRenderer() {
        return this.font;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void doubleClicked() {
    }

    public void setScreen(Screen gui) {
        minecraft.setScreen(gui);
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

    public void drawNpc(GuiGraphics graphics, LivingEntity entity, int x, int y, float zoomed, int rotation) {
        this.wrapper.drawNpc(graphics, entity, x, y, zoomed, rotation, guiLeft, guiTop);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void openLink(String link) {
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new Object[]{new URI(link)});
        } catch (Throwable throwable) {
        }
    }

    public Screen getParent(){
        return wrapper.getParent();
    }
}
