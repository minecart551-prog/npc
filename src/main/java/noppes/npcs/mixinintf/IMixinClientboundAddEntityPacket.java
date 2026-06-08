package noppes.npcs.mixinintf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public interface IMixinClientboundAddEntityPacket {
    public Entity getEntity();

    public FriendlyByteBuf getBuf();
}
