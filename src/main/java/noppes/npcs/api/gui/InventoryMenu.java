package noppes.npcs.api.gui;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.wrapper.gui.GuiComponentsScrollableWrapper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.shared.client.gui.components.GuiSliderNop;

public class InventoryMenu extends MainMenuGui{
    public InventoryMenu(EntityCustomNpc npc, IPlayer player) {
        super(2, npc, player);

        IEntityDisplay display = (IEntityDisplay)gui.addEntityDisplay(40, 22, 27, npc.wrappedNPC).setScale(1).setSize(60, 72);

        gui.addItemSlot(4 ,28, npc.inventory.getArmor(0)).setOnUpdate((gui, slot) -> {npc.inventory.setArmor(0, slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(5);
        gui.addItemSlot(4 ,46, npc.inventory.getArmor(1)).setOnUpdate((gui, slot) -> {npc.inventory.setArmor(1, slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(6);
        gui.addItemSlot(4 ,64, npc.inventory.getArmor(2)).setOnUpdate((gui, slot) -> {npc.inventory.setArmor(2, slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(7);
        gui.addItemSlot(4 ,82, npc.inventory.getArmor(3)).setOnUpdate((gui, slot) -> {npc.inventory.setArmor(3, slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(8);
        gui.addItemSlot(84 ,28, npc.inventory.getRightHand()).setOnUpdate((gui, slot) -> {npc.inventory.setRightHand(slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(2);
        gui.addItemSlot(84 ,46, npc.inventory.getProjectile()).setOnUpdate((gui, slot) -> {npc.inventory.setProjectile(slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(3);
        gui.addItemSlot(84 ,64, npc.inventory.getLeftHand()).setOnUpdate((gui, slot) -> {npc.inventory.setLeftHand(slot.getStack()); display.setEntity(npc.wrappedNPC); gui.update(display);}).setGuiType(4);
        gui.showPlayerInventory(4, 140);
//        gui.addSlider(41, 32, 122, 100, 20, "").setMax(360).setDecimals(0).setValue(0).setOnChange((gui, slider) -> {
//            display.setRotation((int)slider.getValue());
//            gui.update(display);
//        });

        GuiComponentsScrollableWrapper panel = gui.getScrollingPanel().init(gui.getWidth() / 2, 26, gui.getWidth() / 2 - 6, gui.getHeight() - 32 );
        for(int i = 0; i < 20; i++){
            float chance = 100;
            if(npc.inventory.dropchance.containsKey(i)){
                chance = npc.inventory.dropchance.get(i);
            }
            if(chance <= 1 || chance > 100) {
                chance = 100;
            }
            final int id = i;
            panel.addItemSlot(0 ,i * 22 + 1, npc.inventory.getDropItem(i)).
                    setOnUpdate((gui, slot) -> npc.inventory.setDropItem(id, slot.getStack(), ((ISlider)panel.getComponent(id + 20)).getValue()));
            panel.addSlider(20 + i, 20, i * 22, 150, 18, "%s %%").setDecimals(2).setValue(chance).
                    setOnChange((gui, slider) -> npc.inventory.setDropItem(id, npc.inventory.getDropItem(id), slider.getValue()));
        }
    }
}
