package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentUpdate;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ISlider;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class CustomGuiSliderWrapper extends CustomGuiComponentWrapper implements ISlider {

    private String format = "%s%%";
    private float min = 0;
    private float max = 100;
    private int decimals = 0;
    private float value = 100;

    private GuiComponentUpdate<ISlider> onChange = null;

    public CustomGuiSliderWrapper(){}

    public CustomGuiSliderWrapper(int id, String format, int x, int y, int width, int height) {
        setID(id);
        if(!format.isEmpty()){
            setFormat(format);
        }
        setPos(x,y);
        setSize(width,height);
    }

    @Override
    public float getValue() {
        return value;
    }

    @Override
    public CustomGuiSliderWrapper setValue(float value) {
        BigDecimal bd = new BigDecimal(value);
        this.value = bd.setScale(decimals, RoundingMode.FLOOR).floatValue();
        return this;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public CustomGuiSliderWrapper setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public CustomGuiSliderWrapper setMin(float min) {
        this.min = min;
        return this;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public CustomGuiSliderWrapper setMax(float max) {
        this.max = max;
        return this;
    }

    @Override
    public int getDecimals() {
        return decimals;
    }

    @Override
    public CustomGuiSliderWrapper setDecimals(int decimals) {
        if(decimals < 0){
            throw new CustomNPCsException("Decimals cant be lower then 0");
        }
        this.decimals = decimals;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.SLIDER;
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putString("format", format);
        compound.putInt("decimals", decimals);
        compound.putFloat("min", min);
        compound.putFloat("max", max);
        compound.putFloat("value", value);
        return compound;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        setFormat(compound.getString("format"));
        setDecimals(compound.getInt("decimals"));
        setMin(compound.getFloat("min"));
        setMax(compound.getFloat("max"));
        setValue(compound.getFloat("value"));
        return this;
    }

    @Override
    public CustomGuiSliderWrapper setOnChange(GuiComponentUpdate<ISlider> onChange) {
        this.onChange = onChange;
        return this;
    }

    public final void onChange(ICustomGui gui){
        if(onChange != null){
            onChange.onChange(gui, this);
        }
    }

}
