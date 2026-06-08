package noppes.npcs.packets.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiAssetsSelectorWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.PacketServerBasic;

import java.util.UUID;

public class SPacketCustomGuiParts extends PacketServerBasic {
    private final CompoundTag data;
    public SPacketCustomGuiParts(CompoundTag data) {
        this.data = data;
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketCustomGuiParts msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SPacketCustomGuiParts decode(FriendlyByteBuf buf) {
        return new SPacketCustomGuiParts(buf.readNbt());
    }

    @Override
    protected void handle() {
        if(player.containerMenu instanceof ContainerCustomGui container) {
            container.customGui.npc.modelData.load(data);
            container.customGui.npc.updateClient = true;
        }
    }
}