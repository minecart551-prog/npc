package noppes.npcs.packets.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.PacketServerBasic;

import java.util.UUID;


public class SPacketCustomGuiSubGuiClosed extends PacketServerBasic {
    public SPacketCustomGuiSubGuiClosed() {
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiSubGuiClosed msg, FriendlyByteBuf buf) {
    }

    public static SPacketCustomGuiSubGuiClosed decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiSubGuiClosed();
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            if(container.customGui.hasSubGui()){
                container.activeGui.close();
            }
        }
    }
}