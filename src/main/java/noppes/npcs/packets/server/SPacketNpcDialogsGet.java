package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class SPacketNpcDialogsGet extends PacketServerBasic {
    public SPacketNpcDialogsGet() {

    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_GUI;
    }

    public static void encode(SPacketNpcDialogsGet msg, FriendlyByteBuf buf) {

    }

    public static SPacketNpcDialogsGet decode(FriendlyByteBuf buf) {
        return new SPacketNpcDialogsGet();
    }

    @Override
    protected void handle() {
        for(int pos : npc.dialogs.keySet()){
            DialogOption option = npc.dialogs.get(pos);
            if(option == null || !option.hasDialog())
                continue;

            CompoundTag compound = option.writeNBT();
            compound.putInt("Position", pos);
            Packets.send((ServerPlayer)player, new PacketGuiData(compound));

        }
    }
}