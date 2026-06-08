package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.packets.PacketServerBasic;

import java.util.HashMap;

public class SPacketTransportGet extends PacketServerBasic {
    private int id;
    public SPacketTransportGet(int id) {
        this.id = id;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketTransportGet msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
    }

    public static SPacketTransportGet decode(FriendlyByteBuf buf) {
        return new SPacketTransportGet(buf.readInt());
    }

    @Override
    protected void handle() {
        sendTransportData(player, id);
    }

    public static void sendTransportData(ServerPlayer player, int categoryid) {
        TransportCategory category = TransportController.getInstance().categories.get(categoryid);
        if(category == null)
            return;
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        for(TransportLocation transport : category.locations.values()){
            map.put(transport.name, transport.id);
        }
        NoppesUtilServer.sendScrollData(player, map);
    }
}