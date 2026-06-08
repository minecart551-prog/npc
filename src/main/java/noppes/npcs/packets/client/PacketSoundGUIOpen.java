package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.shared.common.PacketBasic;

public class PacketSoundGUIOpen extends PacketBasic {

    public PacketSoundGUIOpen() {
    }

    public static void encode(PacketSoundGUIOpen msg, FriendlyByteBuf buf) {
    }

    public static PacketSoundGUIOpen decode(FriendlyByteBuf buf) {
        return new PacketSoundGUIOpen();
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        try{
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new GuiSoundSelection(""));
        }
        catch(Exception ignored){
        }
    }
}