package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMailData;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class SPacketPlayerMailGet extends PacketServerBasic {

    public SPacketPlayerMailGet() {

    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketPlayerMailGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketPlayerMailGet decode(FriendlyByteBuf buf) {
        return new SPacketPlayerMailGet();
    }

    @Override
    protected void handle() {
        PlayerMailData data = PlayerData.get(player).mailData;
        Packets.send(player, new PacketGuiData(data.saveNBTData(new CompoundTag())));
    }
}