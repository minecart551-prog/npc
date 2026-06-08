package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;




public class PacketNpcRole extends PacketBasic {
	private final int id;
	private final CompoundTag data;

    public PacketNpcRole(int id, CompoundTag data) {
        this.id = id;
        this.data = data;
    }

    public static void encode(PacketNpcRole msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
        buf.writeNbt(msg.data);
    }

    public static PacketNpcRole decode(FriendlyByteBuf buf) {
        return new PacketNpcRole(buf.readInt(), buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(id);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        ((EntityNPCInterface)entity).advanced.setRole(data.getInt("Role"));
        ((EntityNPCInterface)entity).role.load(data);
        NoppesUtil.setLastNpc((EntityNPCInterface) entity);
	}
}