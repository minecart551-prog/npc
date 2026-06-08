package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiAssetsSelectorWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.PacketServerBasic;

import java.util.UUID;


public class SPacketCustomGuiTextUpdate extends PacketServerBasic {
    private final UUID id;
    private final String text;
    public SPacketCustomGuiTextUpdate(UUID id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiTextUpdate msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
        buf.writeUtf(msg.text, 131068);
    }

    public static SPacketCustomGuiTextUpdate decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiTextUpdate(buf.readUUID(), buf.readUtf(131068));
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            ICustomGuiComponent comp = container.activeGui.getComponentUuid(id);
            if(comp instanceof CustomGuiTextFieldWrapper tf){
                tf.setText(this.text);
                tf.onChange(container.activeGui);
            }
            if(comp instanceof CustomGuiAssetsSelectorWrapper as){
                as.setSelected(this.text);
                as.onChange(container.activeGui);
            }
        }
    }
}