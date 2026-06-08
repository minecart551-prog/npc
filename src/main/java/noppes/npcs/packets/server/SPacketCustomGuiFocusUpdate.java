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


public class SPacketCustomGuiFocusUpdate extends PacketServerBasic {
    private final UUID id;
    private final boolean focus;
    public SPacketCustomGuiFocusUpdate(UUID id, boolean focus) {
        this.id = id;
        this.focus = focus;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiFocusUpdate msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
        buf.writeBoolean(msg.focus);
    }

    public static SPacketCustomGuiFocusUpdate decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiFocusUpdate(buf.readUUID(), buf.readBoolean());
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            ICustomGuiComponent comp = container.activeGui.getComponentUuid(id);
            if(comp instanceof CustomGuiTextFieldWrapper tf){
                PlayerWrapper p = (PlayerWrapper) NpcAPI.Instance().getIEntity(player);
                tf.setFocused(this.focus);
                if(!this.focus){
                    tf.onFocusLost(container.activeGui);
                }
            }
        }
    }
}