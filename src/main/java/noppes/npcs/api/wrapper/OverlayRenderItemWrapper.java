package noppes.npcs.api.wrapper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.IRenderItemOverlay;

public class OverlayRenderItemWrapper extends OverlayComponentWrapper implements IRenderItemOverlay
{
    private ItemStack item;
    
    public OverlayRenderItemWrapper(final int id, final int x, final int y, final IItemStack item) {
        super(id, x, y);
        if (item == null) {
            this.item = ItemStack.EMPTY;
        }
        else {
            this.item = item.getMCItemStack();
        }
    }
    
    @Override
    public IItemStack getItem() {
        return NpcAPI.Instance().getIItemStack(this.item);
    }
    
    @Override
    public IRenderItemOverlay setItem(final IItemStack item) {
        this.item = item.getMCItemStack();
        return this;
    }
    
    @Override
    public int getType() {
        return 2;
    }
    
    @Override
    public void toNbt(final CompoundTag compound) {
        super.toNbt(compound);
        compound.put("item", this.item.save(new CompoundTag()));
    }
    
    @Override
    public void fromNbt(final CompoundTag compound) {
        super.fromNbt(compound);
        this.item = ItemStack.of(compound.getCompound("item"));
    }
}
