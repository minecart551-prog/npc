package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.shared.common.PacketBasic;




public class PacketMarkData extends PacketBasic {
	private final int id;
    private final CompoundTag data;

    public PacketMarkData(int id, CompoundTag data) {
    	this.id = id;
    	this.data = data;
    }

    public static void encode(PacketMarkData msg, FriendlyByteBuf buf) {
    	buf.writeInt(msg.id);
        buf.writeNbt(msg.data);
    }

    public static PacketMarkData decode(FriendlyByteBuf buf) {
        return new PacketMarkData(buf.readInt(), buf.readNbt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(id);
        if(entity == null || !(entity instanceof LivingEntity))
            return;
        MarkData mark = MarkData.get((LivingEntity) entity);
        mark.setNBT(data);
	}
}