package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;
import noppes.npcs.shared.common.PacketBasic;

import java.util.UUID;


public class PacketGuiComponentUpdate extends PacketBasic {
    private UUID id;
    private CompoundTag data;

    public PacketGuiComponentUpdate(UUID id, CompoundTag data) {
        this.id = id;
        this.data = data;
    }

    public static void encode(PacketGuiComponentUpdate msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
        buf.writeNbt(msg.data);
    }

    public static PacketGuiComponentUpdate decode(FriendlyByteBuf buf) {
        return new PacketGuiComponentUpdate(buf.readUUID(), buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Screen gui = Minecraft.getInstance().screen;
        if(gui == null)
            return;
        if(gui instanceof GuiCustom cgui) {
            CustomGuiComponentWrapper component = (CustomGuiComponentWrapper)cgui.guiWrapper.getComponentUuid(id);
            component.fromNBT(data);
            IGuiComponent guic = cgui.getComponent(id);
            guic.init();
        }
	}
}