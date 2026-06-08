package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketPlayerCloseContainer extends PacketServerBasic {

    public SPacketPlayerCloseContainer() {
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketPlayerCloseContainer msg, FriendlyByteBuf buf) {
    }

    public static SPacketPlayerCloseContainer decode(FriendlyByteBuf buf) {
        return new SPacketPlayerCloseContainer();
    }

    @Override
    protected void handle() {
        player.closeContainer();
    }
}