package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ILabel;

public class CustomGuiLabelWrapper extends CustomGuiComponentWrapper implements ILabel {
    private String label = "";
    private int color = 0x404040;
    private float scale = 1.0f;
    private boolean centered = false;

    public CustomGuiLabelWrapper(){}

    public CustomGuiLabelWrapper(int id, String label, int x, int y, int width, int height) {
        setID(id);
        setText(label);
        setPos(x,y);
        setSize(width,height);
    }

    public CustomGuiLabelWrapper(int id, String label, int x, int y, int width, int height, int color) {
        this(id, label, x, y, width, height);
        setColor(color);
    }

    @Override
    public String getText() {
        return label;
    }

    @Override
    public ILabel setText(String label) {
        this.label = label;
        return this;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public ILabel setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public ILabel setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public boolean getCentered() {
        return centered;
    }

    @Override
    public ILabel setCentered(boolean bo) {
        this.centered = bo;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.LABEL;
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putString("label", label);
        compound.putInt("color", color);
        compound.putFloat("scale", scale);
        compound.putBoolean("centered", centered);
        return compound;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        setText(compound.getString("label"));
        setColor(compound.getInt("color"));
        setScale(compound.getFloat("scale"));
        setCentered(compound.getBoolean("centered"));
        return this;
    }

}
