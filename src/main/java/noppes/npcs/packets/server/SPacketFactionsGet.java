package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.CustomItems;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.packets.PacketServerBasic;

import java.util.HashMap;
import java.util.Map;

public class SPacketFactionsGet extends PacketServerBasic {

    public SPacketFactionsGet() {

    }

    public boolean toolAllowed(ItemStack item){
        return true;
    }


    public static void encode(SPacketFactionsGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketFactionsGet decode(FriendlyByteBuf buf) {
        return new SPacketFactionsGet();
    }

    @Override
    protected void handle() {
        sendFactionDataAll(player);
    }

    public static void sendFactionDataAll(ServerPlayer player) {
        Map<String,Integer> map = new HashMap<String,Integer>();
        for(Faction faction : FactionController.instance.factions.values()){
            map.put(faction.name, faction.id);
        }
        NoppesUtilServer.sendScrollData(player, map);
    }

}