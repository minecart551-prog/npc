package noppes.npcs.packets.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonListWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.PacketServerBasic;

import java.util.UUID;

public class SPacketCustomGuiButtonList extends PacketServerBasic {
    private final UUID buttonId;
    private final boolean isRightClick;

    public SPacketCustomGuiButtonList(UUID id, boolean isRightClick) {
        this.buttonId = id;
        this.isRightClick = isRightClick;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiButtonList msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.buttonId);
        buf.writeBoolean(msg.isRightClick);
    }

    public static SPacketCustomGuiButtonList decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiButtonList(buf.readUUID(), buf.readBoolean());
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            ICustomGuiComponent comp = container.activeGui.getComponentUuid(buttonId);
            if(comp instanceof CustomGuiButtonListWrapper button){
                PlayerWrapper p = (PlayerWrapper)NpcAPI.Instance().getIEntity(player);
                button.setSelected(button.getSelected() + (isRightClick ? 1 : -1));
                button.onPress(container.activeGui);
                EventHooks.onCustomGuiButton(p, container.activeGui, button);
            }
        }
    }
}