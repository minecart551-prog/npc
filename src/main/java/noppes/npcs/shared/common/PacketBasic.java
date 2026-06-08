package noppes.npcs.shared.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class PacketBasic {
    public Player player;

    public static void handle(final PacketBasic msg) {
        msg.handleClient();
    }

    @Environment(EnvType.CLIENT)
    private void handleClient(){
        player = Minecraft.getInstance().player;
        Minecraft.getInstance().execute(this::handle);
    }

    protected abstract void handle();
}
