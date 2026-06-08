package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.packets.PacketServerBasic;

import java.util.HashMap;

public class SPacketTransportCategoriesGet extends PacketServerBasic {
    public SPacketTransportCategoriesGet() {

    }

    public static void encode(SPacketTransportCategoriesGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketTransportCategoriesGet decode(FriendlyByteBuf buf) {
        return new SPacketTransportCategoriesGet();
    }

    @Override
    protected void handle() {
        sendTransportCategoryData(player);
    }

    public static void sendTransportCategoryData(ServerPlayer player) {
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        for(TransportCategory category : TransportController.getInstance().categories.values()){
            map.put(category.title, category.id);
        }
        NoppesUtilServer.sendScrollData(player, map);
    }
}