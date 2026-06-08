package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentClicked;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.IButtonList;
import noppes.npcs.api.gui.ITexturedRect;

public class CustomGuiButtonListWrapper extends CustomGuiButtonWrapper implements IButtonList {
    CustomGuiTexturedRectWrapper left = new CustomGuiTexturedRectWrapper();
    CustomGuiTexturedRectWrapper right = new CustomGuiTexturedRectWrapper();
    private int selected = 0;
    private String[] values = new String[0];

    public CustomGuiButtonListWrapper(){}

    public CustomGuiButtonListWrapper(int id, int x, int y, int width, int height) {
        super(id,"",x,y,width,height);

        ITexturedRect rect = getTextureRect();
        rect.setTexture("customnpcs:textures/gui/components.png");
        rect.setRepeatingTexture(64, 22, 3).setTextureOffset(0, 64).setPos(7, 0);
        setTextureHoverOffset(22);

        left.setTexture("customnpcs:textures/gui/components.png").setTextureOffset(0, 130);
        left.setSize(10, 20).setPos(0, 0);

        right.setTexture("customnpcs:textures/gui/components.png").setTextureOffset(12, 130);
        right.setSize(10, 20).setPos(width - 10, 0);
    }

    @Override
    public CustomGuiButtonListWrapper setSize(int width, int height) {
        super.setSize(width, height);
        getTextureRect().setSize(width - 14, height);
        return this;
    }

    @Override
    public CustomGuiButtonListWrapper setValues(String... values){
        if(values == null || values.length == 0){
            this.values = new String[0];
            setLabel("");
        }
        else{
            this.values = values;
            this.selected %= values.length;
            setLabel(this.values[this.selected]);
        }
        return this;
    }

    @Override
    public String[] getValues(){
        return this.values;
    }

    @Override
    public CustomGuiButtonListWrapper setSelected(int selected){
        if(selected < 0){
            selected = values.length + selected;
        }
        if(selected >= values.length){
            selected %= values.length;
        }
        this.selected = selected;
        setLabel(this.values[this.selected]);
        return this;
    }

    @Override
    public int getSelected(){
        return selected;
    }

    @Override
    public CustomGuiTexturedRectWrapper getLeftTexture(){
        return left;
    }

    @Override
    public CustomGuiTexturedRectWrapper getRightTexture(){
        return right;
    }

    @Override
    public int getType() {
        return GuiComponentType.BUTTON_LIST;
    }

    @Override
    public CustomGuiButtonListWrapper setOnPress(GuiComponentClicked<IButton> onPress) {
        super.setOnPress(onPress);
        return this;
    }

    @Override
    public CompoundTag toNBT(CompoundTag nbt) {
        super.toNBT(nbt);
        nbt.putInt("selected", selected);
        ListTag list = new ListTag();
        for(String s : values){
            list.add(StringTag.valueOf(s));
        }
        nbt.put("values", list);
        nbt.put("left", left.toNBT(new CompoundTag()));
        nbt.put("right", right.toNBT(new CompoundTag()));
        return nbt;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag nbt) {
        super.fromNBT(nbt);
        this.selected = nbt.getInt("selected");
        this.values = nbt.getList("values", Tag.TAG_STRING).stream().map(Tag::getAsString).toArray(String[]::new);
        left.fromNBT(nbt.getCompound("left"));
        right.fromNBT(nbt.getCompound("right"));
        return this;
    }
}
