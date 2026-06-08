package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IComponentsScrollableWrapper;
import noppes.npcs.api.gui.ICustomGuiComponent;

public class GuiComponentsScrollableWrapper extends GuiComponentsWrapper implements IComponentsScrollableWrapper {
    private boolean enabled = false;
    public int x, y, width, height;
    public int scrollAmount = 0;
    public GuiComponentsWrapper parent;

    public GuiComponentsScrollableWrapper(GuiComponentsWrapper parent, IPlayer player) {
        super(player);
        this.parent = parent;
    }

    @Override
    public GuiComponentsScrollableWrapper init(int x, int y, int width, int height) {
        enabled = true;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }


    @Override
    public CompoundTag getComponentNbt(){
        CompoundTag comp = super.getComponentNbt();
        comp.putBoolean("enabled", enabled);
        comp.putInt("x", x);
        comp.putInt("y", y);
        comp.putInt("width", width);
        comp.putInt("height", height);
        return comp;
    }

    @Override
    public void setComponentNbt(CompoundTag comp){
        super.setComponentNbt(comp);
        enabled = comp.getBoolean("enabled");
        x = comp.getInt("x");
        y = comp.getInt("y");
        width = comp.getInt("width");
        height = comp.getInt("height");
    }

    public boolean isVisible(ICustomGuiComponent component){
        return component.getPosY() >= scrollAmount && component.getPosY() + component.getHeight() <= height + scrollAmount;
    }
}
