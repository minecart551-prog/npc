package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;

public class PacketNpcRotationUpdate extends PacketBasic {
    private final int id;
    private final int orientation;

    public PacketNpcRotationUpdate(int id, int orientation) {
        this.id = id;
        this.orientation = orientation;
    }

    public static void encode(PacketNpcRotationUpdate msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
        buf.writeInt(msg.orientation);
    }

    public static PacketNpcRotationUpdate decode(FriendlyByteBuf buf) {
        return new PacketNpcRotationUpdate(buf.readInt(), buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(id);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        ((EntityNPCInterface)entity).ais.orientation = orientation;
    }
}