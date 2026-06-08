package noppes.npcs.api.overlay;

import net.minecraft.nbt.CompoundTag;

public interface IOverlayComponent
{
    int getId();
    
    int getPosX();
    
    int getPosY();
    
    IOverlayComponent setPos(final int x, final int y);
    
    int getType();
    
    void toNbt(final CompoundTag compound);
    
    void fromNbt(final CompoundTag compound);
}
