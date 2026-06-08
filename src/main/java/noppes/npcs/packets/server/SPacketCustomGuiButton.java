package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiAssetsSelectorWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.PacketServerBasic;

import java.util.UUID;

public class SPacketCustomGuiButton extends PacketServerBasic {
    private final UUID buttonId;
    public SPacketCustomGuiButton(UUID id) {
        this.buttonId = id;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiButton msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.buttonId);
    }

    public static SPacketCustomGuiButton decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiButton(buf.readUUID());
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            ICustomGuiComponent comp = container.activeGui.getComponentUuid(buttonId);
            if(comp instanceof CustomGuiButtonWrapper button){
                PlayerWrapper p = (PlayerWrapper)NpcAPI.Instance().getIEntity(player);
                button.onPress(container.activeGui);
                EventHooks.onCustomGuiButton(p, container.activeGui, button);
            }
            if(comp instanceof CustomGuiAssetsSelectorWrapper assets){
                PlayerWrapper p = (PlayerWrapper)NpcAPI.Instance().getIEntity(player);
                assets.onPress(container.activeGui);
            }
        }
    }
}