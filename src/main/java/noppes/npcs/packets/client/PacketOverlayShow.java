package noppes.npcs.packets.client;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.api.wrapper.OverlayWrapper;
import noppes.npcs.client.controllers.OverlayController;
import noppes.npcs.shared.common.PacketBasic;

public class PacketOverlayShow extends PacketBasic
{
    private final CompoundTag compound;

    public PacketOverlayShow(final CompoundTag compound) {
        this.compound = compound;
    }

    public static void encode(final PacketOverlayShow msg, final FriendlyByteBuf buf) {
        buf.writeNbt(msg.compound);
    }

    public static PacketOverlayShow decode(final FriendlyByteBuf buf) {
        return new PacketOverlayShow(buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        final OverlayWrapper wrapper = new OverlayWrapper(0);
        wrapper.fromNbt(this.compound);
        OverlayController.getInstance().addOverlay(wrapper);
    }
}
