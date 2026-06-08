package noppes.npcs.shared.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.client.gui.custom.components.CustomGuiEntityDisplay;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.client.gui.listeners.IGui;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiWrapper {
    public Map<Integer, GuiButtonNop> npcbuttons = new ConcurrentHashMap<Integer, GuiButtonNop>();
    public Map<Integer,GuiMenuTopButton> topbuttons = new ConcurrentHashMap<Integer,GuiMenuTopButton>();
    public Map<Integer,GuiMenuSideButton> sidebuttons = new ConcurrentHashMap<Integer,GuiMenuSideButton>();
    public Map<Integer, GuiTextFieldNop> textfields = new ConcurrentHashMap<Integer, GuiTextFieldNop>();
    public Map<Integer, GuiLabel> labels = new ConcurrentHashMap<Integer,GuiLabel>();
    public Map<Integer, GuiCustomScrollNop> scrolls = new ConcurrentHashMap<Integer, GuiCustomScrollNop>();
    public Map<Integer, GuiSliderNop> sliders = new ConcurrentHashMap<Integer, GuiSliderNop>();
    public Map<Integer,Screen> extra = new ConcurrentHashMap<Integer,Screen>();
    public List<IGui> components = new ArrayList<IGui>();


    public Screen parent;
    public Screen gui;
    public Screen subgui;
    public int mouseX, mouseY;

    public GuiWrapper(Screen gui){
        this.gui = gui;
    }

    public void init(Minecraft mc, int width, int height){
        GuiTextFieldNop.unfocus();
        if(subgui != null){
            subgui.init(mc, width, height);
        }

        npcbuttons = new ConcurrentHashMap<Integer, GuiButtonNop>();
        topbuttons = new ConcurrentHashMap<Integer,GuiMenuTopButton>();
        sidebuttons = new ConcurrentHashMap<Integer,GuiMenuSideButton>();
        textfields = new ConcurrentHashMap<Integer, GuiTextFieldNop>();
        labels = new ConcurrentHashMap<Integer,GuiLabel>();
        scrolls = new ConcurrentHashMap<Integer, GuiCustomScrollNop>();
        sliders = new ConcurrentHashMap<Integer, GuiSliderNop>();
        extra = new ConcurrentHashMap<Integer, Screen>();
        components = new ArrayList<IGui>();
    }

    public void tick(){
        if(subgui != null)
            subgui.tick();
        else{
            for(GuiTextFieldNop tf : new ArrayList<GuiTextFieldNop>(textfields.values())){
                if(tf.enabled)
                    tf.tick();
            }

            for(IGui comp : new ArrayList<IGui>(components)){
                comp.tick();
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrolled){
        if(subgui != null) {
            subgui.mouseScrolled(mouseX, mouseY, scrolled);
            return true;
        }
        for(IGui comp : new ArrayList<IGui>(components)){
            if(comp instanceof GuiEventListener){
                if(comp.isActive() && ((GuiEventListener)comp).mouseScrolled(mouseX, mouseY, scrolled)){
                    return true;
                }
            }
        }
        for(GuiCustomScrollNop scroll : scrolls.values()){
            if(scroll.visible && scroll.mouseScrolled(mouseX, mouseY, scrolled)){
                return true;
            }
        }
        return false;
    }

    public boolean mouseClicked(double i, double j, int k){
        if(subgui != null) {
            subgui.mouseClicked(i, j, k);
            return true;
        }

        boolean clickedAnyTF = false;
        for(GuiTextFieldNop tf : new ArrayList<GuiTextFieldNop>(textfields.values())){
            if(tf.mouseClicked(i, j, k))
                clickedAnyTF = true;
        }
        if(clickedAnyTF)
            return true;

        for(IGui comp : new ArrayList<IGui>(components)){
            if(comp instanceof GuiEventListener){
                if(((GuiEventListener)comp).mouseClicked(i, j, k)){
                    return true;
                }
            }
        }

        if (k == 0){
            for(GuiCustomScrollNop scroll : new ArrayList<GuiCustomScrollNop>(scrolls.values())){
                if(scroll.mouseClicked(i, j, k)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if(subgui != null) {
            subgui.mouseDragged(x, y, button, dx, dy);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double x, double y, int button) {
        if(subgui != null) {
            subgui.mouseReleased(x, y, button);
            return true;
        }
        return false;
    }

    public boolean charTyped(char c, int i){
        if(subgui != null){
            subgui.charTyped(c,i);
            return true;
        }

        for(GuiTextFieldNop tf : new ArrayList<GuiTextFieldNop>(textfields.values())){
            tf.charTyped(c, i);
        }

        for(GuiCustomScrollNop scroll : new ArrayList<GuiCustomScrollNop>(scrolls.values())){
            scroll.charTyped(c, i);
        }

        for(IGui comp : new ArrayList<IGui>(components)){
            if(comp instanceof GuiEventListener){
                ((GuiEventListener)comp).charTyped(c, i);
            }
        }
        return true;
    }

    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if(subgui != null) {
            subgui.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
            return true;
        }


        boolean active = GuiTextFieldNop.isAnyActive();
        for(IGui gui : components){
            if(gui.isActive()){
                active = true;
                break;
            }
        }

        if (gui.shouldCloseOnEsc() && (key == GLFW.GLFW_KEY_ESCAPE || !active && Minecraft.getInstance().options.keyInventory.key.getValue() == key)){
            gui.onClose();
            return true;
        }

        for(GuiTextFieldNop tf : textfields.values()) {
            tf.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }

        for(GuiCustomScrollNop scroll : new ArrayList<GuiCustomScrollNop>(scrolls.values())){
            scroll.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }

        for(IGui comp : new ArrayList<IGui>(components)){
            if(comp instanceof GuiEventListener){
                if(((GuiEventListener)comp).keyPressed(key, p_keyPressed_2_, p_keyPressed_3_)){
                    return true;
                }
            }
        }
        return active;
    }
    public void drawNpc(GuiGraphics graphics, LivingEntity entity, int x, int y, float zoomed, int rotation, int guiLeft, int guiTop){
        CustomGuiEntityDisplay.drawEntity(graphics, entity, x, y, zoomed, rotation, mouseX, mouseY, guiLeft, guiTop);
    }

    public void changeFocus(GuiEventListener old, GuiEventListener gui) {
        if(old instanceof GuiSliderNop && gui != old){
            ((GuiSliderNop)old).onRelease(0, 0);
        }
    }

    public void setSubgui(Screen subgui) {
        gui.setFocused(null);
        this.subgui = subgui;
        subgui.init(Minecraft.getInstance(), gui.width, gui.height);
        if(subgui instanceof IGuiInterface){
            ((IGuiInterface)subgui).getWrapper().parent = gui;
        }
    }

    public Screen getSubGui() {
        if (subgui instanceof IGuiInterface && ((IGuiInterface)subgui).hasSubGui()) {
            return ((IGuiInterface) subgui).getSubGui();
        }
        return subgui;
    }

    public Screen getParent() {
        if (parent != null)
            return ((IGuiInterface)parent).getParent();
        return gui;
    }

    public void close() {
        GuiTextFieldNop.unfocus();
        ((IGuiInterface) gui).save();
        if(parent != null){

            if(parent instanceof IGuiInterface) {
                parent.setFocused(null);

                ((IGuiInterface) parent).getWrapper().subgui = null;
                ((IGuiInterface) parent).subGuiClosed(gui);
                ((IGuiInterface) parent).initGui();
            }
            else
                gui.onClose();
        }
        else{
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(gui);
            minecraft.mouseHandler.grabMouse();
        }
    }
}
