package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ITexturedRect;

public class CustomGuiTexturedRectWrapper extends CustomGuiComponentWrapper implements ITexturedRect {

    int textureX,textureY = -1;
    float scale = 1.0f;
    String texture = "";
    public boolean hasRepeatingTexture = false;
    public int texRepWidth, texRepHeight, texRepBorderSize = 0;

    public CustomGuiTexturedRectWrapper(){}

    public CustomGuiTexturedRectWrapper(int id, String texture, int x, int y, int width, int height) {
        setID(id);
        setTexture(texture);
        setPos(x,y);
        setSize(width, height);
    }

    public CustomGuiTexturedRectWrapper(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        this(id, texture, x, y, width, height);
        setTextureOffset(textureX, textureY);
    }

    @Override
    public String getTexture() {
        return texture;
    }

    @Override
    public CustomGuiTexturedRectWrapper setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public CustomGuiTexturedRectWrapper setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public int getTextureX() {
        return textureX;
    }

    @Override
    public int getTextureY() {
        return textureY;
    }

    @Override
    public CustomGuiTexturedRectWrapper setTextureOffset(int offsetX, int offsetY) {
        this.textureX = offsetX;
        this.textureY = offsetY;
        return this;
    }

    public CustomGuiTexturedRectWrapper setRepeatingTexture(int width, int height, int borderSize){
        hasRepeatingTexture = true;
        texRepWidth = width;
        texRepHeight = height;
        texRepBorderSize = borderSize;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.TEXTURED_RECT;
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putFloat("scale", scale);
        compound.putString("texture", texture);
        if(textureX >=0 && textureY >=0) {
            compound.putIntArray("texPos", new int[] {textureX, textureY});
        }
        compound.putBoolean("hasRepeatingTexture", hasRepeatingTexture);
        if(hasRepeatingTexture){
            compound.putIntArray("repeatingTexture", new int[] {texRepWidth, texRepHeight, texRepBorderSize});
        }
        return compound;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        setScale(compound.getFloat("scale"));
        setTexture(compound.getString("texture"));
        if(compound.contains("texPos")) {
            int[] arr = compound.getIntArray("texPos");
            setTextureOffset(arr[0], arr[1]);
        }
        hasRepeatingTexture = compound.getBoolean("hasRepeatingTexture");
        if(hasRepeatingTexture){
            int[] arr = compound.getIntArray("repeatingTexture");
            setRepeatingTexture(arr[0], arr[1], arr[2]);
        }
        return this;
    }

}
