package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;
import noppes.npcs.shared.common.PacketBasic;


public class PacketGuiUpdate extends PacketBasic {

    public PacketGuiUpdate() {

    }

    public static void encode(PacketGuiUpdate msg, FriendlyByteBuf buf) {

    }

    public static PacketGuiUpdate decode(FriendlyByteBuf buf) {
        return new PacketGuiUpdate();
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Screen gui = Minecraft.getInstance().screen;
        if(gui == null)
            return;
        if(gui instanceof IGuiInterface igui) {
            igui.initGui();
        }
	}
}