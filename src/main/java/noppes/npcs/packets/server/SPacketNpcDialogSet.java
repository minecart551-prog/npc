package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class SPacketNpcDialogSet extends PacketServerBasic {
    private int slot;
    private int dialog;

    public SPacketNpcDialogSet(int slot, int dialog) {
        this.slot = slot;
        this.dialog = dialog;
    }

    @Override
    public boolean requiresNpc(){
        return true;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.NPC_ADVANCED;
    }

    public static void encode(SPacketNpcDialogSet msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeInt(msg.dialog);
    }

    public static SPacketNpcDialogSet decode(FriendlyByteBuf buf) {
        return new SPacketNpcDialogSet(buf.readInt(), buf.readInt());
    }

    @Override
    protected void handle() {
        if(!npc.dialogs.containsKey(slot))
            npc.dialogs.put(slot, new DialogOption());

        DialogOption option = npc.dialogs.get(slot);
        option.dialogId = dialog;
        option.optionType = OptionType.DIALOG_OPTION;
        if(option.hasDialog())
            option.title = option.getDialog().title;
        if(option.hasDialog()){
            CompoundTag compound = option.writeNBT();
            compound.putInt("Position", slot);
            Packets.send(player, new PacketGuiData(compound));
        }
    }
}