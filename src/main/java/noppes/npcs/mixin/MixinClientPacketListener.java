package noppes.npcs.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixinintf.IMixinClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow private ClientLevel level;

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    public void handleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        IMixinClientboundAddEntityPacket accessor = (IMixinClientboundAddEntityPacket) packet;
        if(level.getEntity(packet.getId()) instanceof EntityNPCInterface){
            if(accessor.getEntity() instanceof EntityNPCInterface){
                ((EntityNPCInterface)level.getEntity(packet.getId())).readSpawnData(((EntityNPCInterface) accessor.getEntity()).writeSpawnData());
            }else {
                ((EntityNPCInterface) level.getEntity(packet.getId())).readSpawnData(accessor.getBuf());
            }
        }
    }
}
