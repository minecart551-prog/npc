package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.shared.common.PacketBasic;


public class PacketConfigFont extends PacketBasic {
    private final String font;
    private final int size;

    public PacketConfigFont(String font, int size) {
        this.font = font;
        this.size = size;
    }

    public static void encode(PacketConfigFont msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.font);
        buf.writeInt(msg.size);
    }

    public static PacketConfigFont decode(FriendlyByteBuf buf) {
        return new PacketConfigFont(buf.readUtf(32767), buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Runnable run = () -> {
            if (!font.isEmpty()) {
                CustomNpcs.FontType = font;
                CustomNpcs.FontSize = size;
                ClientProxy.Font.clear();
                ClientProxy.Font = new ClientProxy.FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
                CustomNpcs.Config.updateConfig();
                player.sendSystemMessage(Component.translatable("Font set to %s", ClientProxy.Font.getName()));
            } else
                player.sendSystemMessage(Component.translatable("Current font is "+ ClientProxy.Font.getName()));
        };
        Minecraft.getInstance().submit(run);
	}
}