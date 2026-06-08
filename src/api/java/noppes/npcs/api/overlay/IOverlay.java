package noppes.npcs.api.overlay;

import net.minecraft.nbt.CompoundTag;
import noppes.npcs.api.item.IItemStack;

import java.util.Collection;

public interface IOverlay
{
    int getId();
    
    void setLinkSide(final int side);
    
    int getLinkSide();

    ILabel addLabel(final int id, final String text, final int x, final int y);

    ITexturedRect addTexturedRect(final int id, final String texture, final int x, final int y, final int width, final int height);

    ITexturedRect addTexturedRectCrop(final int id, final String texture, final int x, final int y, final int width, final int height, final int textureX, final int textureY);

    ITexturedRect addTexturedRectCrop(final int id, final String texture, final int x, final int y, final int width, final int height, final int textureX, final int textureY, final int textureMaxX, final int textureMaxY);
    
    IOverlayComponent getComponent(final int id);

    IRenderItemOverlay addRenderItem(final int id, final int x, final int y, final IItemStack item);
    
    Collection<IOverlayComponent> getComponents();
    
    void removeComponent(final int id);
    
    void clear();
    
    CompoundTag toNbt();
    
    void fromNbt(final CompoundTag tagCompound);
}
