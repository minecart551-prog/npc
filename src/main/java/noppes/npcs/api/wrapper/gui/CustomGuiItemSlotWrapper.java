package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.function.gui.GuiComponentClicked;
import noppes.npcs.api.function.gui.GuiItemSlotUpdate;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;

public class CustomGuiItemSlotWrapper extends CustomGuiComponentWrapper implements IItemSlot {

    private IItemStack stack = ItemStackWrapper.AIR;
    private int guiType = 1;
    private Player player;
    private GuiItemSlotUpdate onSlotUpdate = null;

    public CustomGuiItemSlotWrapper(){}

    public CustomGuiItemSlotWrapper(int x, int y, IItemStack stack) {
        setPos(x,y);
        setSize(14, 14);
        setStack(stack);
    }

    public CustomGuiItemSlotWrapper(int x, int y, Player player) {
        this.player = player;
        setPos(x,y);
        setSize(14, 14);
    }

    @Override
    public boolean hasStack() {
        return !stack.isEmpty();
    }

    @Override
    public IItemStack getStack() {
        if(player != null){
            stack = NpcAPI.Instance().getIItemStack(player.getInventory().getItem(getID()));
        }
        return stack;
    }

    @Override
    public IItemSlot setStack(IItemStack itemStack) {
        if(itemStack == null) {
            this.stack = ItemStackWrapper.AIR;
        }
        else {
            this.stack = itemStack;
        }
        if(player != null) {
            player.getInventory().setItem(getID(), this.stack.getMCItemStack());
        }
        return this;
    }

    @Override
    public int getGuiType() {
        return guiType;
    }

    @Override
    public CustomGuiItemSlotWrapper setGuiType(int type) {
        this.guiType = type;
        return this;
    }

    @Override
    public Slot getMCSlot() {
        return null;
    }

    @Override
    public int getType() {
        return GuiComponentType.ITEM_SLOT;
    }

    @Override
    public CompoundTag toNBT(CompoundTag nbt) {
        super.toNBT(nbt);
        nbt.put("stack", stack.getMCItemStack().save(new CompoundTag()));
        nbt.putInt("guiType", guiType);
        nbt.putBoolean("playerSlot", isPlayerSlot());
        return nbt;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag nbt) {
        super.fromNBT(nbt);
        setStack(NpcAPI.Instance().getIItemStack(ItemStack.of(nbt.getCompound("stack"))));
        setGuiType(nbt.getInt("guiType"));
        if(nbt.getBoolean("playerSlot")){
            player = CustomNpcs.proxy.getPlayer();
        }
        return this;
    }

    @Override
    public boolean isPlayerSlot(){
        return player != null;
    }

    @Override
    public CustomGuiItemSlotWrapper setOnUpdate(GuiItemSlotUpdate onPress) {
        this.onSlotUpdate = onPress;
        return this;
    }

    public final void onUpdate(ICustomGui gui){
        if(onSlotUpdate != null){
            onSlotUpdate.onUpdate(gui, this);
        }
    }
}
