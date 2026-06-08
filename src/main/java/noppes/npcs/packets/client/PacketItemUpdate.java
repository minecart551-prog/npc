package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.shared.common.PacketBasic;



public class PacketItemUpdate extends PacketBasic {
	private final int id;
	private CompoundTag data;

    public PacketItemUpdate(int id, CompoundTag data) {
    	this.id = id;
    	this.data = data;
    }

    public static void encode(PacketItemUpdate msg, FriendlyByteBuf buf) {
    	buf.writeInt(msg.id);
    	buf.writeNbt(msg.data);
    }

    public static PacketItemUpdate decode(FriendlyByteBuf buf) {
        return new PacketItemUpdate(buf.readInt(), buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        ItemStack stack = player.getInventory().getItem(id);
        if(!stack.isEmpty()) {
            ((ItemStackWrapper) NpcAPI.Instance().getIItemStack(stack)).setMCNbt(data);
        }
	}
}