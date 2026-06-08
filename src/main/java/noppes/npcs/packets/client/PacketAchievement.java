package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.client.gui.GuiAchievement;
import noppes.npcs.shared.common.PacketBasic;

public class PacketAchievement extends PacketBasic {
    private final Component title;
    private final Component message;
    private final int type;

    public PacketAchievement(Component title, Component message, int type) {
        this.title = title;
    	this.message = message;
        this.type = type;
    }

    public static void encode(PacketAchievement msg, FriendlyByteBuf buf) {
        buf.writeComponent(msg.title);
        buf.writeComponent(msg.message);
        buf.writeInt(msg.type);
    }

    public static PacketAchievement decode(FriendlyByteBuf buf) {
        return new PacketAchievement(buf.readComponent(), buf.readComponent(), buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Minecraft.getInstance().getToasts().addToast(new GuiAchievement(title, message, type));
	}
}