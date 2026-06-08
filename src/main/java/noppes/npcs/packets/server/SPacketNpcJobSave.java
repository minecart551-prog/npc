package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.packets.PacketServerBasic;

import java.util.Set;



public class SPacketNpcJobSave extends PacketServerBasic {
    private CompoundTag data;

    public SPacketNpcJobSave(CompoundTag data) {
        this.data = data;
    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_ADVANCED;
    }

    public static void encode(SPacketNpcJobSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SPacketNpcJobSave decode(FriendlyByteBuf buf) {
        return new SPacketNpcJobSave(buf.readNbt());
    }

    @Override
    protected void handle() {
        CompoundTag original = npc.job.save(new CompoundTag());
        Set<String> names = data.getAllKeys();
        for(String name : names)
            original.put(name, data.get(name));
        npc.job.load(original);
        npc.updateClient = true;
    }
}