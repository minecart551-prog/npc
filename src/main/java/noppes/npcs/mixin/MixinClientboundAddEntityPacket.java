package noppes.npcs.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixinintf.IMixinClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundAddEntityPacket.class)
public class MixinClientboundAddEntityPacket implements IMixinClientboundAddEntityPacket {
    @Unique
    private FriendlyByteBuf buf;
    @Unique
    private Entity entity;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;I)V", at = @At("TAIL"))
    public void initFromEnt1(Entity entity, int data, CallbackInfo ci){
        this.entity = entity;
    }

    @Inject(method = "Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;<init>(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    public void initFromEnt2(Entity entity, int data, BlockPos pos, CallbackInfo ci){
        this.entity = entity;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    public void initFromBuf(FriendlyByteBuf buffer, CallbackInfo ci){
        final int count = buffer.readVarInt();
        if (count > 0)
        {
            final FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());
            spawnDataBuffer.writeBytes(buffer, count);
            buf = spawnDataBuffer;
            return;
        }
        buf = new FriendlyByteBuf(Unpooled.buffer());
    }

    @Inject(method = "write", at = @At("TAIL"))
    public void write(FriendlyByteBuf buffer, CallbackInfo ci){
        if (entity instanceof EntityNPCInterface)
        {
            final FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());

            ((EntityNPCInterface)entity).writeSpawnData(spawnDataBuffer);

            buffer.writeVarInt(spawnDataBuffer.readableBytes());
            buffer.writeBytes(spawnDataBuffer);

            spawnDataBuffer.release();
        } else
        {
            buffer.writeVarInt(0);
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public FriendlyByteBuf getBuf() {
        return buf;
    }
}
