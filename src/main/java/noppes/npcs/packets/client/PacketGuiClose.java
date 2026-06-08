package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.shared.client.gui.listeners.IGuiClose;
import noppes.npcs.shared.common.PacketBasic;




public class PacketGuiClose extends PacketBasic {
	private final CompoundTag data;

    public PacketGuiClose(CompoundTag data) {
        this.data = data;
    }

    public PacketGuiClose() {
        this(new CompoundTag());
    }

    public static void encode(PacketGuiClose msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static PacketGuiClose decode(FriendlyByteBuf buf) {
        return new PacketGuiClose(buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Screen gui = Minecraft.getInstance().screen;
        if(gui == null)
            return;

        if(gui instanceof IGuiClose){
            ((IGuiClose)gui).setClose(data);
        }

        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(null);
        mc.mouseHandler.grabMouse();
	}
}