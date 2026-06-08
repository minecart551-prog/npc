package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.packets.PacketServerBasic;

import java.util.HashMap;
import java.util.Map;

public class SPacketBanksGet extends PacketServerBasic {
    public SPacketBanksGet() {

    }

    public static void encode(SPacketBanksGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketBanksGet decode(FriendlyByteBuf buf) {
        return new SPacketBanksGet();
    }

    @Override
    protected void handle() {
        sendBankDataAll(player);
    }

    public static void sendBankDataAll(ServerPlayer player) {
        Map<String,Integer> map = new HashMap<String,Integer>();
        for(Bank bank : BankController.getInstance().banks.values()){
            map.put(bank.name, bank.id);
        }
        NoppesUtilServer.sendScrollData(player, map);
    }
}