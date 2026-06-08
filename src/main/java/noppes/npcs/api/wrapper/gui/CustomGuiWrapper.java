package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiComponentUpdate;
import noppes.npcs.packets.client.PacketGuiData;

import java.util.UUID;

public class CustomGuiWrapper extends GuiComponentsWrapper implements ICustomGui {

    private int id;
    private int width,height;
    private boolean pauseGame;

    private final CustomGuiTexturedRectWrapper background = new CustomGuiTexturedRectWrapper();
    private final GuiComponentsScrollableWrapper scrollingPanel;

    private ScriptContainer scriptHandler;

    private CustomGuiWrapper parent;
    private CustomGuiWrapper subgui;

    public EntityCustomNpc npc;

    public CustomGuiWrapper(IPlayer player){
        super(player);
        this.scrollingPanel = new GuiComponentsScrollableWrapper(this, player);
    }

    public CustomGuiWrapper(IPlayer player, int id, int width, int height, boolean pauseGame) {
        this(player);
        this.id = id;
        this.setSize(width, height);
        this.pauseGame = pauseGame;
        this.scriptHandler = ScriptContainer.Current;
        background.setID(-1);
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public ScriptContainer getScriptHandler() {
        return this.scriptHandler;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        if(background.getWidth() <= 0 || background.getHeight() <= 0){
            background.setSize(width, height);
        }
    }

    @Override
    public void setDoesPauseGame(boolean pauseGame) {
        this.pauseGame = pauseGame;
    }

    public boolean getDoesPauseGame() {
        return pauseGame;
    }

    @Override
    public void setBackgroundTexture(String resourceLocation) {
        background.texture = resourceLocation;
    }

    public String getBackgroundTexture() {
        return background.texture;
    }

    public ITexturedRect getBackgroundRect(){
        return background;
    }

    @Override
    public GuiComponentsScrollableWrapper getScrollingPanel(){
        return scrollingPanel;
    }

    @Override
    public void openSubGui(ICustomGui gui) {
        subgui = (CustomGuiWrapper)gui;
        subgui.parent = this;
        subgui.npc = npc;
        this.getRootGui().update();
    }

    @Override
    public CustomGuiWrapper getSubGui() {
        return subgui;
    }

    @Override
    public boolean hasSubGui() {
        return subgui != null;
    }

    @Override
    public CustomGuiWrapper closeSubGui() {
        if(subgui == null){
            throw new CustomNPCsException("Current gui has no subgui");
        }
        CustomGuiWrapper gui = subgui;
        subgui = null;
        player.showCustomGui(this.getRootGui());
        return gui;
    }

    @Override
    public void close() {
        if(parent == null){
            player.closeGui();
        }
        else{
            parent.subgui = null;
            this.getRootGui().update();
        }
    }

    @Override
    public CustomGuiWrapper getParentGui() {
        return parent;
    }

    @Override
    public CustomGuiWrapper getRootGui() {
        if(parent == null){
            return this;
        }
        return parent.getRootGui();
    }

    @Override
    public CustomGuiWrapper getActiveGui() {
        if(subgui == null){
            return this;
        }
        return subgui.getActiveGui();
    }

    @Override
    public IPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void update() {
        if(player.getMCEntity().containerMenu instanceof ContainerCustomGui c) {
            c.customGui = this.getRootGui();
            c.activeGui = this.getActiveGui();
            Packets.send(player.getMCEntity(), new PacketGuiData(this.getRootGui().toNBT()));
        }
    }

    @Override
    public void update(ICustomGuiComponent component) {
        if(player.getMCEntity().containerMenu instanceof ContainerCustomGui) {
            Packets.send(player.getMCEntity(), new PacketGuiComponentUpdate(component.getUniqueID(), ((CustomGuiComponentWrapper)component).toNBT(new CompoundTag())));
        }
    }

    public ICustomGui fromNBT(CompoundTag tag) {
        this.id = tag.getInt("id");
        this.width = tag.getIntArray("size")[0];
        this.height = tag.getIntArray("size")[1];
        this.pauseGame = tag.getBoolean("pause");
        background.fromNBT(tag.getCompound("backgroundRect"));

        setComponentNbt(tag.getCompound("components"));
        scrollingPanel.setComponentNbt(tag.getCompound("scrolling_components"));

        if(tag.contains("subgui")){
            if(subgui == null){
                subgui = new CustomGuiWrapper(player);
                subgui.fromNBT(tag.getCompound("subgui"));
            }
        }
        else{
            subgui = null;
        }

        return this;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("id", id);
        tag.putIntArray("size", new int[]{this.width,this.height});
        tag.putBoolean("pause", pauseGame);
        tag.put("backgroundRect", background.toNBT(new CompoundTag()));

        tag.put("components", getComponentNbt());
        tag.put("scrolling_components", scrollingPanel.getComponentNbt());


        if(parent == null){
            tag.putInt("slotSize", getActiveGui().getSlots().size());
        }

        if(subgui != null){
            tag.put("subgui", subgui.toNBT());
        }

        return tag;
    }

    @Override
    public ICustomGuiComponent getComponentUuid(UUID id) {
        if(subgui != null){
            ICustomGuiComponent comp = subgui.getComponentUuid(id);
            if(comp != null){
                return comp;
            }
        }
        ICustomGuiComponent comp = super.getComponentUuid(id);
        if(comp != null){
            return comp;
        }
        return scrollingPanel.getComponentUuid(id);
    }
}
