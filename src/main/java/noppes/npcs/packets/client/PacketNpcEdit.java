package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;


public class PacketNpcEdit extends PacketBasic {
	private final int id;

    public PacketNpcEdit(int id) {
    	this.id = id;
    }

    public static void encode(PacketNpcEdit msg, FriendlyByteBuf buf) {
    	buf.writeInt(msg.id);
    }

    public static PacketNpcEdit decode(FriendlyByteBuf buf) {
        return new PacketNpcEdit(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(id);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            NoppesUtil.setLastNpc(null);
        else
            NoppesUtil.setLastNpc((EntityNPCInterface) entity);
	}
}