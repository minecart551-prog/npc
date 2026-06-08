package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentClicked;
import noppes.npcs.api.function.gui.GuiComponentUpdate;
import noppes.npcs.api.gui.*;

public class CustomGuiAssetsSelectorWrapper extends CustomGuiComponentWrapper implements IAssetsSelector {

    private String selected = "";
    private String root = "textures";
    private String type = "png";
    private GuiComponentUpdate<IAssetsSelector> onChange = null;
    private GuiComponentClicked<IAssetsSelector> onPress = null;

    public CustomGuiAssetsSelectorWrapper(){}

    public CustomGuiAssetsSelectorWrapper(int id, int x, int y, int width, int height) {
        setID(id);
        setPos(x,y);
        setSize(width, height);
    }
    @Override
    public String getSelected() {
        return selected;
    }

    @Override
    public CustomGuiAssetsSelectorWrapper setSelected(String selected) {
        this.selected = selected;
        return this;
    }

    @Override
    public String getRoot() {
        return root;
    }

    @Override
    public CustomGuiAssetsSelectorWrapper setRoot(String root) {
        this.root = root;
        return this;
    }

    @Override
    public String getFileType() {
        return type;
    }

    @Override
    public CustomGuiAssetsSelectorWrapper setFileType(String type) {
        this.type = type;
        return this;
    }

    public final void onPress(ICustomGui gui){
        if(onPress != null){
            onPress.onClick(gui, this);
        }
    }

    @Override
    public CustomGuiAssetsSelectorWrapper setOnPress(GuiComponentClicked<IAssetsSelector> onPress) {
        this.onPress = onPress;
        return this;
    }

    public final void onChange(ICustomGui gui){
        if(onChange != null){
            onChange.onChange(gui, this);
        }
    }

    @Override
    public CustomGuiAssetsSelectorWrapper setOnChange(GuiComponentUpdate<IAssetsSelector> onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.ASSETS_SELECTOR;
    }

    @Override
    public CompoundTag toNBT(CompoundTag nbt) {
        super.toNBT(nbt);
        nbt.putString("selected", selected);
        nbt.putString("filetype", type);
        nbt.putString("root", root);
        return nbt;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag nbt) {
        super.fromNBT(nbt);
        setSelected(nbt.getString("selected"));
        setFileType(nbt.getString("filetype"));
        setRoot(nbt.getString("root"));
        return this;
    }
}
