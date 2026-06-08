package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.packets.IPacketServer;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiUpdate;

public class SPacketDialogCategorySave extends PacketServerBasic {
    private CompoundTag data;
    public SPacketDialogCategorySave(CompoundTag data) {
        this.data = data;
    }

    public SPacketDialogCategorySave(FriendlyByteBuf buf) {
        data = buf.readNbt();
    }

    public static SPacketDialogCategorySave decode(FriendlyByteBuf buf) {
        return new SPacketDialogCategorySave(buf);
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        return CustomNpcsPermissions.GLOBAL_DIALOG;
    }

    @Override
    protected void handle() {
        DialogCategory category = new DialogCategory();
        category.readNBT(data);
        DialogController.instance.saveCategory(category);
        Packets.send(player, new PacketGuiUpdate());
    }

    public static void encode(SPacketDialogCategorySave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }
}