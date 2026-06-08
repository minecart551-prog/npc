package noppes.npcs.client.gui.custom.components;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiItemSlotWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerCustomGui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CustomGuiSlot extends Slot {

    private final Player player;
    public final IItemSlot slot;
    private final CustomGuiWrapper gui;

    private static Field xField;
    private static Field yField;

    public CustomGuiSlot(CustomGuiWrapper gui, Container inventoryIn, int id, IItemSlot slot, Player player) {
        super(inventoryIn, id, -666667, -666666);
        this.gui = gui;
        this.player = player;
        this.slot = slot;

        if(yField == null){
            for(Field f : Slot.class.getDeclaredFields()){
                if(Modifier.isPrivate(f.getModifiers())){
                    continue;
                }
                try{
                    if(f.get(this) instanceof Integer i && i == -666666){
                        yField = f;
                        yField.setAccessible(true);
                    }
                    if(f.get(this) instanceof Integer i && i == -666667){
                        xField = f;
                        xField.setAccessible(true);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        update(0, 0);
    }

    public CustomGuiSlot update(int x, int y){
        try {
            xField.set(this, x + slot.getPosX());
            yField.set(this, y + slot.getPosY());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void set(ItemStack is) {
        super.set(is);
        if(!player.level().isClientSide) {
            if (getItem() != slot.getStack().getMCItemStack()) {
                if(!slot.isPlayerSlot()){
                    slot.setStack(NpcAPI.Instance().getIItemStack(getItem()));
                    ((CustomGuiItemSlotWrapper)slot).onUpdate(gui);

                }
                if(player.containerMenu instanceof ContainerCustomGui container){
                    EventHooks.onCustomGuiSlot((PlayerWrapper)NpcAPI.Instance().getIEntity(player), container.customGui, slot);
                }
            }
        }
    }
}
