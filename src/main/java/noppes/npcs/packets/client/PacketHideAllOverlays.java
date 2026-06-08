package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.controllers.OverlayController;
import noppes.npcs.shared.common.PacketBasic;

public class PacketHideAllOverlays extends PacketBasic
{
    private final boolean id;

    public PacketHideAllOverlays(final boolean id) {
        this.id = id;
    }

    public static void encode(final PacketHideAllOverlays msg, final FriendlyByteBuf buf) {
        buf.writeBoolean(msg.id);
    }

    public static PacketHideAllOverlays decode(final FriendlyByteBuf buf) {
        return new PacketHideAllOverlays(buf.readBoolean());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        OverlayController.getInstance().clear();
    }
}

