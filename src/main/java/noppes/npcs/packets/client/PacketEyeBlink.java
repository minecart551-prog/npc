package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import noppes.npcs.ModelData;
import noppes.npcs.ModelEyeData;
import noppes.npcs.client.parts.MpmPartData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.PacketBasic;

public class PacketEyeBlink extends PacketBasic {
	private final int id;

    public PacketEyeBlink(int id) {
    	this.id = id;
    }

    public static void encode(PacketEyeBlink msg, FriendlyByteBuf buf) {
    	buf.writeInt(msg.id);
    }

    public static PacketEyeBlink decode(FriendlyByteBuf buf) {
        return new PacketEyeBlink(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(id);
        if(entity == null || !(entity instanceof EntityNPCInterface))
            return;
        ModelData data = ((EntityCustomNpc)entity).modelData;
        for(MpmPartData pd : data.mpmParts){
            if(pd instanceof ModelEyeData){
                ((ModelEyeData)pd).blinkStart = System.currentTimeMillis();
            }
        }
	}
}