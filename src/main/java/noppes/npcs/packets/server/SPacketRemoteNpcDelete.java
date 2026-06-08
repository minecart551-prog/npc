package noppes.npcs.packets.server;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketNpcDelete;

public class SPacketRemoteNpcDelete extends PacketServerBasic {
    private int entityId;

    public SPacketRemoteNpcDelete(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_DELETE;
    }

    public static void encode(SPacketRemoteNpcDelete msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static SPacketRemoteNpcDelete decode(FriendlyByteBuf buf) {
        return new SPacketRemoteNpcDelete(buf.readInt());
    }

    @Override
    protected void handle() {
        Entity entity = player.level().getEntity(entityId);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        npc = (EntityNPCInterface) entity;
        npc.delete();
        Packets.sendNearby(npc, new PacketNpcDelete(npc.getId()));
        SPacketRemoteNpcsGet.sendNearbyNpcs(player);
    }
}