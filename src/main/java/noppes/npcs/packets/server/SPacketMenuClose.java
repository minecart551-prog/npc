package noppes.npcs.packets.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketMenuClose extends PacketServerBasic
{
    public SPacketMenuClose() {

    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_GUI;
    }

    public static void encode(SPacketMenuClose msg, FriendlyByteBuf buf) {

    }

    public static SPacketMenuClose decode(FriendlyByteBuf buf) {
        return new SPacketMenuClose();
    }

    @Override
    protected void handle() {
        npc.reset();
        if(npc.linkedData != null)
            LinkedNpcController.Instance.saveNpcData(npc);
        NoppesUtilServer.setEditingNpc(player, null);
    }
}