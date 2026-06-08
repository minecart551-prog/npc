package noppes.npcs.client.gui.custom.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiAssetsSelectorWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiButton;
import noppes.npcs.packets.server.SPacketCustomGuiTextUpdate;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;
import noppes.npcs.shared.client.util.AssetsFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomGuiAssetsSelector extends AbstractWidget implements IGuiComponent {

    private String up = "..<" + I18n.get("gui.up") + ">..";

    private GuiCustom parent;
    private CustomGuiAssetsSelectorWrapper component;
    private GuiCustomScrollNop folders;
    private GuiCustomScrollNop items;
    private CustomGuiLabel label;

    public int id;

    private static final HashMap<String, List<ResourceLocation>> domains = new HashMap<>();
    private static final HashMap<String, ResourceLocation> textures = new HashMap<>();
    private String location = "";
    private String path = "";
    private String selectedDomain;

    public ResourceLocation prevResource = null;
    public ResourceLocation selectedResource = null;

    public CustomGuiAssetsSelector(GuiCustom parent, CustomGuiAssetsSelectorWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.empty());
        this.parent = parent;
        this.component = component;

        this.folders = new GuiCustomScrollNop(parent, 101);
        this.items = new GuiCustomScrollNop(parent, 102);
        this.label = new CustomGuiLabel(parent, (CustomGuiLabelWrapper)new CustomGuiLabelWrapper().setCentered(true));

        this.init();

        if(!component.getSelected().isEmpty()){
            selectedResource = prevResource = new ResourceLocation(component.getSelected());
        }

        List<ResourceLocation> resources = AssetsFinder.find(component.getRoot(), "." + component.getFileType());
        for(ResourceLocation loc : resources){
            domains.computeIfAbsent(loc.getNamespace(), k -> new ArrayList<>()).add(loc);
        }

        if(selectedResource != null && !selectedResource.getPath().isEmpty() ){
            selectedDomain = selectedResource.getNamespace();
            if(!domains.containsKey(selectedDomain)) {
                selectedDomain = null;
            }
            int i = selectedResource.getPath().lastIndexOf('/');
            location = path = selectedResource.getPath().substring(0, i + 1);
            i = path.lastIndexOf('/', path.length() - 2);
            if(i > 0){
                location = path.substring(0, i + 1);
            }
            label.setText(selectedDomain + ":" + location);
        }

        setFolders();
        setItems();

        folders.listener = new ICustomScrollListener() {
            @Override
            public void scrollClicked(double x, double y, int k, GuiCustomScrollNop scroll) {
                if(scroll.getSelected().equals(up)){
                    return;
                }
                path = location + scroll.getSelected() + '/';
                setItems();
            }

            @Override
            public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {
                if(selectedDomain == null) {
                    selectedDomain = scroll.getSelected();
                    if(!component.getRoot().isEmpty()){
                        path = location = component.getRoot() + '/';
                    }
                }
                else if(scroll.getSelected().equals(up)) {
                    int i = location.lastIndexOf('/', location.length() - 2);
                    if(i > 0) {
                        path = location;
                        location = location.substring(0, i + 1);
                    }
                    else {
                        path = location = "";
                    }
                    if(location.isEmpty()) {
                        selectedDomain = null;
                    }

                }
                else {
                    path = location = location + scroll.getSelected() + '/';
                }
                setFolders();
                setItems();
                label.setText(selectedDomain + ":" + location);
            }
        };
        items.listener = new ICustomScrollListener() {
            @Override
            public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
                selectedResource = textures.get(scroll.getSelected());
                component.setSelected(textures.get(scroll.getSelected()).toString());
                if(!component.disablePackets){
                    Packets.sendServer(new SPacketCustomGuiTextUpdate(component.getUniqueID(), component.getSelected()));
                }
                else{
                    component.onChange(null);
                }
            }

            @Override
            public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {
                if(!component.disablePackets){
                    Packets.sendServer(new SPacketCustomGuiButton(component.getUniqueID()));
                }
                else{
                    component.onPress(null);
                }
                //parent.close();
            }
        };
    }

    @Override
    public void init() {
        this.id = component.getID();
        this.setX(component.getPosX());
        this.setY(component.getPosY());
        this.folders.guiTop = this.items.guiTop = this.getY() + 10;
        this.setWidth(component.getWidth());
        this.height=(component.getHeight());

        this.folders.setSize(component.getWidth() / 2 - 1, component.getHeight() - 10);
        this.items.setSize(component.getWidth() / 2 - 1, component.getHeight() - 10);
        this.folders.guiLeft = this.getX();
        this.items.guiLeft = this.getX() + component.getWidth() / 2 + 1;

        this.label.setWidth(component.getWidth());
        this.label.setX(this.getX());
        this.label.setY(this.getY());
        this.label.setHeight(10);

        if(!component.getSelected().isEmpty()){
            selectedResource = new ResourceLocation(component.getSelected());
        }
    }

    private void setFolders(){
        if(selectedDomain == null) {
            folders.setList(Lists.newArrayList(domains.keySet()));
            if(selectedResource != null){
                selectedDomain = selectedResource.getNamespace();
                folders.setSelected(selectedDomain);
            }
            return;
        }
        List<String> list = new ArrayList<String>();
        list.add(up);
        for(ResourceLocation td : domains.get(selectedDomain)) {
            String fullPath = td.getPath();
            if(fullPath.indexOf('/') >= 0){
                fullPath = fullPath.substring(0, fullPath.lastIndexOf('/') + 1);
            }
            if(location.isEmpty() || fullPath.startsWith(location) && !fullPath.equals(location)) {
                String path = fullPath.substring(location.length());
                int i = path.indexOf('/');
                if(i < 0)
                    continue;
                path = path.substring(0, i);
                if(!path.isEmpty() && !list.contains(path)) {
                    list.add(path);
                }
            }
        }
        folders.clearSelection();
        folders.setList(list);

        if(selectedResource != null && selectedResource.getPath().startsWith(location) && !location.equals(path)) {
            folders.setSelected(path.substring(location.length(), path.length() - 1));
            folders.scrollTo(folders.getSelected());

        }
    }

    private void setItems(){
        if(selectedDomain == null){
            return;
        }
        textures.clear();
        List<ResourceLocation> data = domains.get(selectedDomain);
        List<String> list = new ArrayList<String>();
        for(ResourceLocation td : data) {
            String name = td.getPath();
            String path = td.getPath();
            if(name.indexOf('/') >= 0){
                name = name.substring(name.lastIndexOf('/') + 1);
                path = path.substring(0, path.lastIndexOf('/') + 1);
            }
            if(path.equals(this.path) && !list.contains(name)) {
                list.add(name);
                textures.put(name, td);
            }
        }
        items.clearSelection();
        items.setList(list);
        if(selectedResource != null) {
            int i = selectedResource.getPath().lastIndexOf('/');
            String name = selectedResource.getPath().substring(i + 1);
            String path = selectedResource.getPath().substring(0, i + 1);
            if(path.equals(this.path)){
                items.setSelected(name);
            }
        }
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void onRenderPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(!visible)
            return;
        label.onRender(graphics, mouseX, mouseY, partialTicks);
        folders.render(graphics, mouseX, mouseY, partialTicks);
        items.render(graphics, mouseX, mouseY, partialTicks);
        boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseScrolled) {
        return folders.mouseScrolled(mouseX, mouseY, mouseScrolled) || items.mouseScrolled(mouseX, mouseY, mouseScrolled);
    }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return folders.mouseClicked(mouseX, mouseY, button) || items.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        return folders.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_) || items.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean charTyped(char p_231042_1_, int p_231042_2_) {
        return folders.charTyped(p_231042_1_, p_231042_2_) || items.charTyped(p_231042_1_, p_231042_2_);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_169152_) {}
    @Override
    public ICustomGuiComponent component() {
        return component;
    }
}
