package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class SPacketBankGet extends PacketServerBasic {

    private int bank;

    public SPacketBankGet(int bank) {
        this.bank = bank;
    }


    public static void encode(SPacketBankGet msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.bank);
    }

    public static SPacketBankGet decode(FriendlyByteBuf buf) {
        return new SPacketBankGet(buf.readInt());
    }

    @Override
    protected void handle() {
        sendBank(player,BankController.getInstance().getBank(this.bank));
    }

    public static void sendBank(ServerPlayer player, Bank bank) {
        CompoundTag compound = new CompoundTag();
        bank.addAdditionalSaveData(compound);
        Packets.send(player, new PacketGuiData(compound));

        if(player.containerMenu instanceof ContainerManageBanks){
            ((ContainerManageBanks)player.containerMenu).setBank(bank);
        }

        player.containerMenu.sendAllDataToRemote();
    }
}