package noppes.npcs.api.gui;

import net.minecraft.world.entity.player.Player;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.api.wrapper.gui.GuiComponentsScrollableWrapper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.DataStats;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiParts;

public class ModelMenu extends MainMenuGui{
    public ModelMenu(EntityCustomNpc npc, IPlayer player) {
        super(1, npc, player, false);

        gui.getScrollingPanel().init(180, 26, 230, gui.getHeight() - 32 );
    }

    public static void open(Player player, EntityCustomNpc npc){
        IPlayer p = (IPlayer) NpcAPI.Instance().getIEntity(player);
        CustomGuiWrapper menu = new ModelMenu(npc, p).gui;
        p.showCustomGui(menu);
        Packets.send(p.getMCEntity(), new PacketGuiParts(npc.getId(), menu.toNBT()));
    }
}
