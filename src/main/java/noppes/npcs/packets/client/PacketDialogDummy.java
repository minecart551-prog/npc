package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.shared.common.PacketBasic;




public class PacketDialogDummy extends PacketBasic {
	private final String name;
    private final CompoundTag data;

    public PacketDialogDummy(String name, CompoundTag data) {
    	this.name = name;
    	this.data = data;
    }

    public static void encode(PacketDialogDummy msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name);
        buf.writeNbt(msg.data);
    }

    public static PacketDialogDummy decode(FriendlyByteBuf buf) {
        return new PacketDialogDummy(buf.readUtf(32767), buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        EntityDialogNpc npc = new EntityDialogNpc(player.level());
        npc.display.setName(I18n.get(name));
        EntityUtil.Copy(player, npc);
        Dialog dialog = new Dialog(null);
        dialog.readNBT(data);
        PacketDialog.openDialog(dialog, npc, player);
	}
}