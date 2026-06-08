package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.shared.client.gui.listeners.IScrollData;
import noppes.npcs.shared.common.PacketBasic;

public class PacketGuiScrollSelected extends PacketBasic {
	private final String selected;

    public PacketGuiScrollSelected(String selected) {
    	this.selected = selected;
    }

    public static void encode(PacketGuiScrollSelected msg, FriendlyByteBuf buf) {
    	buf.writeUtf(msg.selected);
    }

    public static PacketGuiScrollSelected decode(FriendlyByteBuf buf) {
        return new PacketGuiScrollSelected(buf.readUtf(32767));
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Screen gui = Minecraft.getInstance().screen;
        if(gui == null || !(gui instanceof IScrollData))
            return;
        ((IScrollData)gui).setSelected(selected);
	}
}