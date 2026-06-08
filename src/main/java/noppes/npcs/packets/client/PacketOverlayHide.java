package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.controllers.OverlayController;
import noppes.npcs.shared.common.PacketBasic;

public class PacketOverlayHide extends PacketBasic
{
    private final int id;

    public PacketOverlayHide(final int id) {
        this.id = id;
    }

    public static void encode(final PacketOverlayHide msg, final FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
    }

    public static PacketOverlayHide decode(final FriendlyByteBuf buf) {
        return new PacketOverlayHide(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        OverlayController.getInstance().removeOverlay(this.id);
    }
}