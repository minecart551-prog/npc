package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.ILabel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CustomGuiComponentWrapper implements ICustomGuiComponent {

    public UUID uniqueId = UUID.randomUUID();
    private int id;
    private int posX,posY;
    private int width,height;
    private List<Component> hoverText = new ArrayList<>();
    private boolean enabled = true;
    private boolean visible = true;
    public boolean disablePackets = false;

    public CustomGuiComponentWrapper setDisablePackets(){
        disablePackets = true;
        return this;
    }
    @Override
    public int getID() {
        return id;
    }

    @Override
    public CustomGuiComponentWrapper setID(int id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }
    @Override
    public CustomGuiComponentWrapper setEnabled(boolean bo) {
        enabled = bo;
        return this;
    }

    @Override
    public boolean getVisible() {
        return visible;
    }
    @Override
    public CustomGuiComponentWrapper setVisible(boolean bo) {
        visible = bo;
        return this;
    }

    @Override
    public UUID getUniqueID() {
        return uniqueId;
    }

    @Override
    public int getPosX() {
        return posX;
    }

    @Override
    public int getPosY() {
        return posY;
    }

    @Override
    public CustomGuiComponentWrapper setPos(int x, int y) {
        this.posX = x;
        this.posY = y;
        return this;
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
    public CustomGuiComponentWrapper setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public boolean hasHoverText() {
        return hoverText.size()>0;
    }

    @Override
    public String[] getHoverText() {
        String[] ht = new String[hoverText.size()];
        for(int i = 0; i < hoverText.size(); i++){
            ht[i] = ((TranslatableContents)hoverText.get(i).getContents()).getKey();
        }
        return ht;
    }

    public List<Component> getHoverTextList() {
        return hoverText;
    }

    @Override
    public CustomGuiComponentWrapper setHoverText(String text) {
        this.hoverText = new ArrayList<>();
        this.hoverText.add(Component.translatable(text));
        return this;
    }

    @Override
    public CustomGuiComponentWrapper setHoverText(String[] text) {
        this.hoverText = new ArrayList<>();
        for(Object obj: text){
            if(obj instanceof Component){
                hoverText.add((Component) obj);
            }else{
                hoverText.add(Component.translatable(String.valueOf(obj)));
            }
        }
        return this;
    }

    public CustomGuiComponentWrapper setHoverText(List<Component> list) {
        this.hoverText = list;
        return this;
    }

    public CompoundTag toNBT(CompoundTag nbt) {
        nbt.putInt("id", id);
        nbt.putBoolean("enabled", enabled);
        nbt.putBoolean("visible", visible);
        nbt.putUUID("uniqueId", uniqueId);
        nbt.putIntArray("pos", new int[]{posX,posY});
        nbt.putIntArray("size", new int[] {width,height});
        if(hoverText!=null) {
            ListTag list = new ListTag();
            for (Component s : hoverText){
                list.add(StringTag.valueOf(((TranslatableContents)s.getContents()).getKey()));
            }
            if(list.size() > 0)
                nbt.put("hover", list);
        }
        nbt.putInt("type", getType());
        return nbt;
    }

    public CustomGuiComponentWrapper fromNBT(CompoundTag nbt) {
        setID(nbt.getInt("id"));
        setEnabled(nbt.getBoolean("enabled"));
        setVisible(nbt.getBoolean("visible"));
        uniqueId = nbt.getUUID("uniqueId");
        setPos(nbt.getIntArray("pos")[0],nbt.getIntArray("pos")[1]);
        setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        if(nbt.contains("hover")) {
            ListTag list = nbt.getList("hover", Tag.TAG_STRING);
            String[] hoverText = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                hoverText[i] =  list.get(i).getAsString();
            }
            setHoverText(hoverText);
        }
        return this;
    }

    public static CustomGuiComponentWrapper createFromNBT(CompoundTag nbt) {
        switch (nbt.getInt("type")) {
            case GuiComponentType.BUTTON:
                return new CustomGuiButtonWrapper().fromNBT(nbt);
            case GuiComponentType.BUTTON_LIST:
                return new CustomGuiButtonListWrapper().fromNBT(nbt);
            case GuiComponentType.LABEL:
                return new CustomGuiLabelWrapper().fromNBT(nbt);
            case GuiComponentType.TEXTURED_RECT:
                return new CustomGuiTexturedRectWrapper().fromNBT(nbt);
            case GuiComponentType.TEXT_FIELD:
                return new CustomGuiTextFieldWrapper().fromNBT(nbt);
            case GuiComponentType.SCROLL:
                return new CustomGuiScrollWrapper().fromNBT(nbt);
            case GuiComponentType.ITEM_SLOT:
                return new CustomGuiItemSlotWrapper().fromNBT(nbt);
            case GuiComponentType.TEXT_AREA:
                return new CustomGuiTextAreaWrapper().fromNBT(nbt);
            case GuiComponentType.SLIDER:
                return new CustomGuiSliderWrapper().fromNBT(nbt);
            case GuiComponentType.ENTITY_DISPLAY:
                return new CustomGuiEntityDisplayWrapper().fromNBT(nbt);
            case GuiComponentType.ASSETS_SELECTOR:
                return new CustomGuiAssetsSelectorWrapper().fromNBT(nbt);
            case GuiComponentType.COLORED_LINE:
                return new CustomGuiColoredLineWrapper().fromNBT(nbt);
            case GuiComponentType.ITEM_RENDERER:
                return new CustomGuiItemRendererWrapper().fromNBT(nbt);
            default:
                return null;
        }
    }

}
