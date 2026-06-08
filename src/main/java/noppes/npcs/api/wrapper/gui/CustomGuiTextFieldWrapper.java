package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentUpdate;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ITextField;

import java.util.Objects;


public class CustomGuiTextFieldWrapper extends CustomGuiComponentWrapper implements ITextField {
    private int color = 14737632;
    private int type = 0;
    private String text = "";
    private boolean focused = false;

    private GuiComponentUpdate<ITextField> onChange = null;
    private GuiComponentUpdate<ITextField> onFocusLost = null;

    private int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;

    public CustomGuiTextFieldWrapper(){}

    public CustomGuiTextFieldWrapper(int id, int x, int y, int width, int height) {
        setID(id);
        setPos(x,y);
        setSize(width, height);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public CustomGuiTextFieldWrapper setText(String text) {
        String prevText = this.text;
        this.text = Objects.requireNonNull(text, "");
        if(!this.text.isEmpty() && (getCharacterType() == 1 || getCharacterType() == 2)){
            try {
                setInteger(getInteger());
            }catch (NumberFormatException e){
                this.text = prevText;
            }
        }
        return this;
    }

    @Override
    public int getInteger() {
        if(type == 0){
            throw new CustomNPCsException("Character Type 0 doesnt convert to integer");
        }
        if(text.isEmpty()){
            return Math.max(min, 0);
        }
        if(type == 1){
            return Integer.parseInt(text);
        }
        return Integer.parseInt(text, 16);
    }

    @Override
    public CustomGuiTextFieldWrapper setInteger(int i){
        if(type == 0){
            throw new CustomNPCsException("Character Type 0 doesnt support setInteger");
        }
        i = Math.max(min, i);
        i = Math.min(max, i);
        if(type == 1 || type == 3){
            this.text = i + "";
        }
        if(type == 2){
            this.text = String.format("%01x", i);
        }
        return this;
    }

    @Override
    public float getFloat() {
        if(type == 0){
            throw new CustomNPCsException("Character Type 0 doesnt convert to float");
        }
        if(text.isEmpty()){
            return Math.max(min, 0);
        }
        if(type == 1){
            return Integer.parseInt(text);
        }
        if(type == 2){
            return Integer.parseInt(text, 16);
        }
        return Float.parseFloat(text);
    }

    @Override
    public CustomGuiTextFieldWrapper setFloat(float f){
        if(type == 0 || type == 2){
            throw new CustomNPCsException("Character Type 0 doesnt support setFloat");
        }
        f = Math.max(min, f);
        f = Math.min(max, f);
        if(type == 1){
            this.text = f + "";
        }
        return this;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public CustomGuiTextFieldWrapper setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public CustomGuiTextFieldWrapper setFocused(boolean bo) {
        focused = bo;
        return this;
    }

    @Override
    public boolean getFocused() {
        return focused;
    }

    @Override
    public CustomGuiTextFieldWrapper setCharacterType(int type) {
        this.type = type;
        return this;
    }

    @Override
    public int getCharacterType() {
        return type;
    }

    @Override
    public CustomGuiTextFieldWrapper setMinMax(int min, int max) {
        if(type == 0){
            throw new CustomNPCsException("Character Type 0 doesnt support setInteger");
        }
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.TEXT_FIELD;
    }

    @Override
    public CompoundTag toNBT(CompoundTag nbt) {
        super.toNBT(nbt);
        nbt.putString("default", text);
        nbt.putBoolean("focused", focused);
        nbt.putInt("color", color);
        nbt.putInt("character_type", type);
        nbt.putInt("min", min);
        nbt.putInt("max", max);
        return nbt;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag nbt) {
        super.fromNBT(nbt);
        setText(nbt.getString("default"));
        setFocused(nbt.getBoolean("focused"));
        setColor(nbt.getInt("color"));
        setCharacterType(nbt.getInt("character_type"));
        min = nbt.getInt("min");
        max = nbt.getInt("max");
        return this;
    }

    public CustomGuiTextFieldWrapper setOnChange(GuiComponentUpdate<ITextField> onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    public CustomGuiTextFieldWrapper setOnFocusLost(GuiComponentUpdate<ITextField> onFocusChange) {
        this.onFocusLost = onFocusChange;
        return this;
    }

    public final void onChange(ICustomGui gui){
        if(onChange != null){
            onChange.onChange(gui, this);
        }
    }

    public final void onFocusLost(ICustomGui gui){
        if(onFocusLost != null){
            onFocusLost.onChange(gui, this);
        }
    }

}
