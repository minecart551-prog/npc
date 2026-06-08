package noppes.npcs.api.gui;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.subgui.AvailabilityGui;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.api.wrapper.gui.GuiComponentsScrollableWrapper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataStats;

public class LogicMenu extends MainMenuGui{
    public LogicMenu(EntityCustomNpc npc, IPlayer player) {
        super(3, npc, player);
        GuiComponentsScrollableWrapper panel = gui.getScrollingPanel().init(6, 26, gui.getWidth() - 12, gui.getHeight() - 32 );

        int y = 0;
        panel.addLabel(0, "stats.aggro", 0, y, 100, 8);
        panel.addTextField(1, 0, y + 9, 80, 20).setCharacterType(1).setText(npc.stats.aggroRange+"")
                .setOnChange((gui, textfield) -> npc.stats.setAggroRange(textfield.getInteger()));

        y += 36;
        panel.addLabel(2, "display.hitbox", 0, y, 100, 8);
        panel.addButtonList(3, 0, y + 9, 110, 20).setValues("stats.normal", "gui.none", "hair.solid")
                .setSelected(npc.display.getHitboxState())
                .setOnPress((gui2, bb) -> npc.display.setHitboxState((byte) ((IButtonList)bb).getSelected()));
    }
}
