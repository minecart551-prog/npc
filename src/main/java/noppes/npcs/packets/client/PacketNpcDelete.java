package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;


public class PacketNpcDelete extends PacketBasic {
	private final int id;

    public PacketNpcDelete(int id) {
        this.id = id;
    }

    public static void encode(PacketNpcDelete msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
    }

    public static PacketNpcDelete decode(FriendlyByteBuf buf) {
        return new PacketNpcDelete(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(id);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        ((EntityNPCInterface)entity).delete();
	}
}