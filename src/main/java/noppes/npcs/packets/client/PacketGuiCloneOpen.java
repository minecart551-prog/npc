package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;
import noppes.npcs.shared.common.PacketBasic;



public class PacketGuiCloneOpen extends PacketBasic {
    private final CompoundTag data;

    public PacketGuiCloneOpen(CompoundTag data) {
    	this.data = data;
    }

    public static void encode(PacketGuiCloneOpen msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static PacketGuiCloneOpen decode(FriendlyByteBuf buf) {
        return new PacketGuiCloneOpen(buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(data));
	}
}