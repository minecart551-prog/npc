package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;
import noppes.npcs.roles.JobSpawner;

public class SPacketNpcJobGet extends PacketServerBasic {

    public SPacketNpcJobGet() {

    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_GUI;
    }

    public static void encode(SPacketNpcJobGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketNpcJobGet decode(FriendlyByteBuf buf) {
        return new SPacketNpcJobGet();
    }

    @Override
    protected void handle() {
        if(npc.job.getType() == JobType.NONE)
            return;
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("JobData", true);
        npc.job.save(compound);
        Packets.send(player, new PacketGuiData(compound));
    }
}