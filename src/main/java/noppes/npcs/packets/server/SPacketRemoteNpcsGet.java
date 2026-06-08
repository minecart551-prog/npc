package noppes.npcs.packets.server;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiScrollSelected;

import java.text.DecimalFormat;
import java.util.HashMap;

public class SPacketRemoteNpcsGet extends PacketServerBasic {
    public SPacketRemoteNpcsGet() {

    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_GUI;
    }

    public static void encode(SPacketRemoteNpcsGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketRemoteNpcsGet decode(FriendlyByteBuf buf) {
        return new SPacketRemoteNpcsGet();
    }

    @Override
    protected void handle() {
        sendNearbyNpcs(player);
        Packets.send(player, new PacketGuiScrollSelected(CustomNpcs.FreezeNPCs?"Unfreeze Npcs":"Freeze Npcs"));
    }

    public static void sendNearbyNpcs(ServerPlayer player) {
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        for(Entity entity : ((ServerLevel)player.level()).getAllEntities()) {
            if(entity instanceof EntityNPCInterface) {
                EntityNPCInterface npc = (EntityNPCInterface) entity;
                if(npc.isRemoved())
                    continue;
                float distance = player.distanceTo(npc);
                DecimalFormat df = new DecimalFormat("#.#");
                String s = df.format(distance);
                if(distance < 10)
                    s = "0" + s;
                map.put(s + " : " + npc.display.getName(), npc.getId());
            }
        }

        NoppesUtilServer.sendScrollData(player, map);
    }
}