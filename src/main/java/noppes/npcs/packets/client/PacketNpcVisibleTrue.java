package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import noppes.npcs.shared.common.PacketBasic;


public class PacketNpcVisibleTrue extends PacketBasic {
	private final ClientboundAddEntityPacket pkt;
    private final int id;

    public PacketNpcVisibleTrue(Entity entity) {
        id = entity.getId();
        pkt = new ClientboundAddEntityPacket(entity);
    }
    public PacketNpcVisibleTrue(int id, ClientboundAddEntityPacket pkt) {
        this.id = id;
        this.pkt = pkt;
    }

    public static void encode(PacketNpcVisibleTrue msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
        msg.pkt.write(buf);
    }

    public static PacketNpcVisibleTrue decode(FriendlyByteBuf buf) {
        return new PacketNpcVisibleTrue(buf.readInt(), new ClientboundAddEntityPacket(buf));
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        ClientLevel w = Minecraft.getInstance().level;
        Entity entity = w.getEntity(id);
        if(entity == null){
            Minecraft.getInstance().getConnection().handleAddEntity(pkt);
        }

	}
}