package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentClicked;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IScroll;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CustomGuiScrollWrapper extends CustomGuiComponentWrapper implements IScroll {
    private int[] selection = new int[0];
    private String[] list;
    private boolean multiSelect = false;
    private boolean hasSearch = true;

    private GuiComponentClicked<IScroll> onClick, onDoubleClick;

    public CustomGuiScrollWrapper(){}

    public CustomGuiScrollWrapper(int id, int x, int y, int width, int height, String[] list) {
        setID(id);
        setPos(x,y);
        setSize(width,height);
        setList(list);
    }

    @Override
    public String[] getList() {
        return list;
    }

    @Override
    public CustomGuiScrollWrapper setList(String[] list) {
        this.list = list;
        return this;
    }

    @Override
    public int getDefaultSelection() {
        if(this.selection.length == 0){
            return -1;
        }
        if(this.selection.length > 1){
            throw new CustomNPCsException("You have multiple selections, use getSelection instead");
        }
        return this.selection[0];
    }

    @Override
    public CustomGuiScrollWrapper setDefaultSelection(int selection) {
        this.selection = new int[] {selection};
        return this;
    }

    @Override
    public int[] getSelection() {
        return this.selection;
    }

    @Override
    public CustomGuiScrollWrapper setSelection(int... selection) {
        if(selection == null){
            selection = new int[0];
        }
        this.selection = selection;
        return this;
    }

    @Override
    public String[] getSelectionList() {
        if(this.selection.length == 0){
            return new String[0];
        }
        return Arrays.stream(this.selection)
                .filter(i -> i >= 0 && i < this.list.length)
                .mapToObj(i -> this.list[i])
                .toArray(String[]::new);
    }

    @Override
    public CustomGuiScrollWrapper setSelectionList(String... list) {
        this.selection = IntStream.range(0, list.length)
                .map(i -> Arrays.asList(this.list).indexOf(list[i]))
                .toArray();
        return this;
    }

    @Override
    public boolean isMultiSelect() {
        return multiSelect;
    }

    @Override
    public CustomGuiScrollWrapper setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.SCROLL;
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putIntArray("selection", this.selection);
        compound.put("list", Arrays.stream(this.list).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new)));
        compound.putBoolean("multiSelect", multiSelect);
        compound.putBoolean("hasSearch", hasSearch);
        return compound;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        setSelection(compound.getIntArray("selection"));
        setList(compound.getList("list", Tag.TAG_STRING).stream().map(Tag::getAsString).toArray(String[]::new));
        setMultiSelect(compound.getBoolean("multiSelect"));
        setHasSearch(compound.getBoolean("hasSearch"));
        return this;
    }

    @Override
    public CustomGuiScrollWrapper setOnClick(GuiComponentClicked<IScroll> onClick){
        this.onClick = onClick;
        return this;
    }

    @Override
    public CustomGuiScrollWrapper setOnDoubleClick(GuiComponentClicked<IScroll> onDoubleClick){
        this.onDoubleClick = onDoubleClick;
        return this;
    }

    @Override
    public boolean getHasSearch() {
        return this.hasSearch;
    }

    @Override
    public CustomGuiScrollWrapper setHasSearch(boolean bo) {
        this.hasSearch = bo;
        return this;
    }


    public final void onClick(ICustomGui gui){
        if(onClick != null){
            onClick.onClick(gui, this);
        }
    }

    public final void onDoubleClick(ICustomGui gui){
        if(onDoubleClick != null){
            onDoubleClick.onClick(gui, this);
        }
    }

}
