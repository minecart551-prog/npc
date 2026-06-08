package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentClicked;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;

public class CustomGuiButtonWrapper extends CustomGuiComponentWrapper implements IButton {

    String label = "";
    int textureHoverOffset = -1;
    IItemStack item = ItemStackWrapper.AIR;
    private CustomGuiTexturedRectWrapper texture = new CustomGuiTexturedRectWrapper();
    GuiComponentClicked<IButton> onPress = null;

    public CustomGuiButtonWrapper(){}

    public CustomGuiButtonWrapper(int id, String label, int x, int y) {
        setID(id);
        setLabel(label);
        setPos(x,y);

        texture.setID(id);
        texture.setSize(getWidth(), getHeight());
        texture.setRepeatingTexture(200, 20, 3);
        texture.setTexture("textures/gui/widgets.png");
        texture.setTextureOffset(0, 46);
        setTextureHoverOffset(20);
    }

    public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height) {
        this(id,label,x,y);
        setSize(width,height);

    }

    public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height, String texture) {
        this(id,label,x,y,width,height);
        setTexture(texture);
        this.texture.setRepeatingTexture(width, height, 3);
        this.texture.setTextureOffset(0, 0);
        setTextureHoverOffset(height);
    }

    public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        this(id,label,x,y,width,height,texture);
        setTextureOffset(textureX, textureY);
    }

    @Override
    public CustomGuiButtonWrapper setSize(int width, int height) {
        super.setSize(width, height);
        texture.setSize(width, height);

        if(textureHoverOffset <= 0){
            textureHoverOffset = height;
        }
        return this;
    }

    @Override
    public int getTextureHoverOffset() {
        return textureHoverOffset;
    }

    @Override
    public IButton setTextureHoverOffset(int height){
        this.textureHoverOffset = height;
        return this;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public IButton setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public CustomGuiTexturedRectWrapper getTextureRect() {
        return this.texture;
    }

    @Override
    public void setTextureRect(ITexturedRect rect) {
        this.texture = (CustomGuiTexturedRectWrapper)rect;
    }

    @Override
    public String getTexture() {
        return texture.getTexture();
    }

    @Override
    public boolean hasTexture() {
        return this.texture!=null;
    }

    @Override
    public IButton setTexture(String texture) {
        this.texture.setTexture(texture);
        return this;
    }

    @Override
    public int getTextureX() {
        return texture.getTextureX();
    }

    @Override
    public int getTextureY() {
        return texture.getTextureY();
    }

    @Override
    public IButton setTextureOffset(int textureX, int textureY) {
        this.texture.setTextureOffset(textureX, textureY);
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.BUTTON;
    }

    @Override
    public IItemStack getDisplayItem(){
        return item;
    }

    @Override
    public IButton setDisplayItem(IItemStack item){
        if(item == null){
            this.item = ItemStackWrapper.AIR;
        }
        else{
            this.item = item;
        }
        return this;
    }

    @Override
    public CompoundTag toNBT(CompoundTag nbt) {
        super.toNBT(nbt);
        nbt.put("texture", texture.toNBT(new CompoundTag()));
        nbt.putInt("textureHoverOffset", textureHoverOffset);
        nbt.putString("label", label);
        nbt.put("item", item.getItemNbt().getMCNBT());
        return nbt;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag nbt) {
        super.fromNBT(nbt);
        setSize(nbt.getIntArray("size")[0],nbt.getIntArray("size")[1]);
        setTextureHoverOffset(nbt.getInt("textureHoverOffset"));
        setLabel(nbt.getString("label"));

        texture.fromNBT(nbt.getCompound("texture"));

        ItemStack it = ItemStack.of(nbt.getCompound("item"));
        item = NpcAPI.Instance().getIItemStack(it);
        return this;
    }

    @Override
    public CustomGuiButtonWrapper setOnPress(GuiComponentClicked<IButton> onPress) {
        this.onPress = onPress;
        return this;
    }

    public final void onPress(ICustomGui gui){
        if(onPress != null){
            onPress.onClick(gui, this);
        }
    }
}
