package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.CustomItems;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class SPacketNpcRoleGet extends PacketServerBasic {

    public SPacketNpcRoleGet() {

    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    public static void encode(SPacketNpcRoleGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketNpcRoleGet decode(FriendlyByteBuf buf) {
        return new SPacketNpcRoleGet();
    }

    @Override
    protected void handle() {
        if(npc.role.getType() == RoleType.NONE)
            return;
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("RoleData", true);
        Packets.send(player, new PacketGuiData(npc.role.save(compound)));
    }
}