package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.*;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiComponentsWrapper implements IComponentsWrapper {
    private List<ICustomGuiComponent> components = new ArrayList<>();
    private List<IItemSlot> slots = new ArrayList<>();
    private List<IItemSlot> playerSlots = new ArrayList<>();
    public int slotId = 0;

    protected IPlayer player;
    public GuiComponentsWrapper(IPlayer player){
        this.player = player;
    }

    @Override
    public CustomGuiButtonWrapper addButton(int id, String label, int x, int y) {
        CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiButtonWrapper addButton(int id, String label, int x, int y, int width, int height) {
        CustomGuiButtonWrapper component  = new CustomGuiButtonWrapper(id, label, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiButtonListWrapper addButtonList(int id, int x, int y, int width, int height) {
        CustomGuiButtonListWrapper component = new CustomGuiButtonListWrapper(id, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiButtonWrapper addTexturedButton(int id, String label, int x, int y, int width, int height, String texture) {
        CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiButtonWrapper addTexturedButton(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture, textureX, textureY);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiLabelWrapper addLabel(int id, String label, int x, int y, int width, int height) {
        CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, label, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiLabelWrapper addLabel(int id, String label, int x, int y, int width, int height, int color) {
        CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, label, x, y, width, height, color);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiTextFieldWrapper addTextField(int id, int x, int y, int width, int height) {
        CustomGuiTextFieldWrapper component = new CustomGuiTextFieldWrapper(id, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiTextAreaWrapper addTextArea(int id, int x, int y, int width, int height) {
        CustomGuiTextAreaWrapper component = new CustomGuiTextAreaWrapper(id, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiTexturedRectWrapper addTexturedRect(int id, String texture, int x, int y, int width, int height) {
        CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiTexturedRectWrapper addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height, textureX,textureY);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiScrollWrapper addScroll(int id, int x, int y, int width, int height, String... list) {
        CustomGuiScrollWrapper component = new CustomGuiScrollWrapper(id, x, y, width, height, list);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiSliderWrapper addSlider(int id, int x, int y, int width, int height, String format) {
        CustomGuiSliderWrapper component = new CustomGuiSliderWrapper(id, format, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiEntityDisplayWrapper addEntityDisplay(int id, int x, int y, IEntity entity) {
        CustomGuiEntityDisplayWrapper component = new CustomGuiEntityDisplayWrapper(id, entity, x, y);
        addComponent(component);
        return component;
    }

    @Override
    public CustomGuiAssetsSelectorWrapper addAssetsSelector(int id, int x, int y, int width, int height) {
        CustomGuiAssetsSelectorWrapper component = new CustomGuiAssetsSelectorWrapper(id, x, y, width, height);
        addComponent(component);
        return component;
    }

    @Override
    public IColoredLine addColoredLine(int id, int xStart, int yStart, int xEnd, int yEnd, int color, float thickness){
        IColoredLine line = new CustomGuiColoredLineWrapper(id, xStart, yStart, xEnd, yEnd, color, thickness);
        this.components.add(line);
        return line;
    }

    @Override
    public IItemRenderer addItemRenderer(int id, final int x, final int y, int width, int height, final IItemStack stack) {
        final CustomGuiItemRendererWrapper rendererWrapper = new CustomGuiItemRendererWrapper(id, x, y, width, height, stack);
        this.components.add(rendererWrapper);
        return rendererWrapper;
    }

    @Override
    public ICustomGuiComponent getComponent(int componentID) {
        for(ICustomGuiComponent component : this.components) {
            if(component.getID() == componentID)
                return component;
        }
        return null;
    }

    @Override
    public void addComponent(ICustomGuiComponent component) {
        if(this.components.stream().anyMatch(t -> t.getID() == component.getID())){
            throw new CustomNPCsException("This gui already contains component id:" + component.getID());
        }
        this.components.add(component);
    }

    @Override
    public void removeComponent(int componentID) {
        components.removeIf(c -> c.getID() == componentID);
    }

    @Override
    public List<ICustomGuiComponent> getComponents() {
        return components;
    }

    public CompoundTag getComponentNbt(){
        CompoundTag comp = new CompoundTag();
        ListTag list = new ListTag();
        for(ICustomGuiComponent c : this.components) {
            list.add(((CustomGuiComponentWrapper)c).toNBT(new CompoundTag()));
        }
        comp.put("components", list);

        list = new ListTag();
        for(ICustomGuiComponent c : this.slots) {
            list.add(((CustomGuiComponentWrapper)c).toNBT(new CompoundTag()));
        }
        comp.put("slots", list);

        list = new ListTag();
        for(ICustomGuiComponent c : this.playerSlots) {
            list.add(((CustomGuiComponentWrapper)c).toNBT(new CompoundTag()));
        }
        comp.put("playerSlots", list);
        return comp;
    }

    private List<IItemSlot> getNbtSlots(CompoundTag tag, String key){
        List<IItemSlot> slots = new ArrayList<>();
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for(Tag b : list) {
            CustomGuiItemSlotWrapper component = (CustomGuiItemSlotWrapper)CustomGuiComponentWrapper.createFromNBT((CompoundTag)b);
            slots.add(component);
        }
        return slots;
    }

    public void setComponentNbt(CompoundTag comp){
        List<ICustomGuiComponent> components = new ArrayList<>();
        ListTag list = comp.getList("components", Tag.TAG_COMPOUND);
        for(Tag b : list) {
            components.add(CustomGuiComponentWrapper.createFromNBT((CompoundTag)b));
        }
        this.components = components;

        this.slots = getNbtSlots(comp, "slots");
        this.playerSlots = getNbtSlots(comp, "playerSlots");
    }

    public ICustomGuiComponent getComponentUuid(UUID id) {
        for(ICustomGuiComponent comp : components){
            if(comp.getUniqueID().equals(id)){
                return comp;
            }
        }
        return null;
    }


    @Override
    public List<IItemSlot> getSlots() {
        return slots;
    }

    @Override
    public List<IItemSlot> getPlayerSlots() {
        return playerSlots;
    }


    @Override
    public IItemSlot addItemSlot(int x, int y) {
        return this.addItemSlot(x, y, ItemScriptedWrapper.AIR);
    }

    @Override
    public IItemSlot addItemSlot(int x, int y, IItemStack stack) {
        CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x, y, stack);
        GuiComponentsWrapper w =  this;
        if(this instanceof GuiComponentsScrollableWrapper scroll){
            w = scroll.parent;
        }
        slot.setID(w.slotId++);
        this.slots.add(slot);
        return slot;
    }

    @Override
    public void removeItemSlot(IItemSlot slot) {
        this.slots.removeIf((s) -> s.getID() == slot.getID());
    }

    @Override
    public void showPlayerInventory(int x, int y) {
        this.showPlayerInventory(x, y, true);
    }

    @Override
    public IItemSlot[] showPlayerInventory(int x, int y, boolean full) {
        List<IItemSlot> playerSlots = new ArrayList<>();

        if(full){
            for (int row = 0; row < 3; ++row){
                for (int col = 0; col < 9; ++col){
                    CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x + col * 18, y + row * 18, player.getMCEntity());
                    slot.setID(9 + row * 9 + col);
                    playerSlots.add(slot);
                }
            }
            y += 58;
        }
        for (int col = 0; col < 9; ++col){
            CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x + col * 18, y, player.getMCEntity());
            slot.setID(col);
            playerSlots.add(slot);
        }
        this.playerSlots = playerSlots;
        return this.playerSlots.toArray(new IItemSlot[0]);
    }
}
