package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;


public class PacketNpcVisibleFalse extends PacketBasic {
	private final int id;

    public PacketNpcVisibleFalse(int id) {
    	this.id = id;
    }

    public static void encode(PacketNpcVisibleFalse msg, FriendlyByteBuf buf) {
    	buf.writeInt(msg.id);
    }

    public static PacketNpcVisibleFalse decode(FriendlyByteBuf buf) {
        return new PacketNpcVisibleFalse(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        ClientLevel w = Minecraft.getInstance().level;
        Entity entity = w.getEntity(id);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        w.removeEntity(id, Entity.RemovalReason.DISCARDED);
	}
}