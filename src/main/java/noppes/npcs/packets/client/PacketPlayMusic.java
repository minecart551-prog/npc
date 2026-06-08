package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.shared.common.PacketBasic;

public class PacketPlayMusic extends PacketBasic {
    private final String name;
    private final boolean streaming;
    private final boolean looping;

    public PacketPlayMusic(String name, boolean streaming, boolean looping) {
        this.name = name;
        this.streaming = streaming;
        this.looping = looping;
    }

    public static void encode(PacketPlayMusic msg, FriendlyByteBuf buf) {
    	buf.writeUtf(msg.name);
        buf.writeBoolean(msg.streaming);
        buf.writeBoolean(msg.looping);
    }

    public static PacketPlayMusic decode(FriendlyByteBuf buf) {
        return new PacketPlayMusic(buf.readUtf(32767), buf.readBoolean(), buf.readBoolean());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        if(streaming){
            MusicController.Instance.playStreaming(name, player, looping);
        }
        else{
            MusicController.Instance.playMusic(name, player, looping);
        }
	}
}