package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;

import noppes.npcs.controllers.PhysicsHelper;
import noppes.npcs.shared.common.PacketBasic;

import java.lang.reflect.Constructor;

public class PacketUpdatePhysics extends PacketBasic {

    private final ClientboundAddEntityPacket pkt;
    private final int id;

    public PacketUpdatePhysics(Entity entity) {
        id = entity.getId();
        pkt = new ClientboundAddEntityPacket(entity);
    }
    public PacketUpdatePhysics(int id, ClientboundAddEntityPacket pkt) {
        this.id = id;
        this.pkt = pkt;
    }

    public static void encode(PacketUpdatePhysics msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
        msg.pkt.write(buf);
    }

    public static PacketUpdatePhysics decode(FriendlyByteBuf buf) {
        return new PacketUpdatePhysics(buf.readInt(), new ClientboundAddEntityPacket(buf));
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        ClientLevel w = Minecraft.getInstance().level;
        Entity entity = w.getEntity(id);
        if(entity == null){
            Minecraft.getInstance().getConnection().handleAddEntity(pkt);
        }
        if(PhysicsHelper.Enabled){
            PhysicsHelper.resetEntityPhysics(w, id);
        }
    }
}