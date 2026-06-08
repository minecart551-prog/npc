package noppes.npcs.packets.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiSliderWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.PacketServerBasic;

import java.util.UUID;


public class SPacketCustomGuiSliderUpdate extends PacketServerBasic {
    private final UUID id;
    private final float value;
    public SPacketCustomGuiSliderUpdate(UUID id, float value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiSliderUpdate msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
        buf.writeFloat(msg.value);
    }

    public static SPacketCustomGuiSliderUpdate decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiSliderUpdate(buf.readUUID(), buf.readFloat());
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            ICustomGuiComponent comp = container.activeGui.getComponentUuid(id);
            if(comp instanceof CustomGuiSliderWrapper slider){
                slider.setValue(this.value);
                slider.onChange(container.activeGui);
            }
        }
    }
}