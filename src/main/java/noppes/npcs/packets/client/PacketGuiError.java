package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.shared.client.gui.listeners.IGuiError;
import noppes.npcs.shared.common.PacketBasic;




public class PacketGuiError extends PacketBasic {
    private final int error;
	private final CompoundTag data;

    public PacketGuiError(int error, CompoundTag data) {
        this.error = error;
        this.data = data;
    }

    public static void encode(PacketGuiError msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.error);
        buf.writeNbt(msg.data);
    }

    public static PacketGuiError decode(FriendlyByteBuf buf) {
        return new PacketGuiError(buf.readInt(), buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Screen gui = Minecraft.getInstance().screen;
        if(gui == null || !(gui instanceof IGuiError))
            return;

        ((IGuiError)gui).setError(error, data);
	}
}