package noppes.npcs.packets.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.api.gui.ModelMenu;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketOpenParts extends PacketServerBasic {
    public SPacketOpenParts() {
    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_GUI;
    }

    public static void encode(SPacketOpenParts msg, FriendlyByteBuf buf) {
    }

    public static SPacketOpenParts decode(FriendlyByteBuf buf) {
        return new SPacketOpenParts();
    }

    @Override
    protected void handle() {
        ModelMenu.open(player, (EntityCustomNpc) npc);
    }
}
