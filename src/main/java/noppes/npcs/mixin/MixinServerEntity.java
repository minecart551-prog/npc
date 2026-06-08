package noppes.npcs.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketMarkData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class MixinServerEntity {
    @Shadow @Final private Entity entity;

    @Inject(method = "addPairing", at=@At("TAIL"))
    public void addPairing(ServerPlayer player, CallbackInfo ci) {
        if(this.entity instanceof EntityNPCInterface){
            EntityNPCInterface npc = (EntityNPCInterface)this.entity;
            npc.tracking.add(player.getId());
            VisibilityController.checkIsVisible(npc, player);
        }
        if(!(this.entity instanceof LivingEntity) || this.entity.level().isClientSide)
            return;
        MarkData data = MarkData.get((LivingEntity) this.entity);
        if(data.marks.isEmpty())
            return;
        Packets.send(player, new PacketMarkData(this.entity.getId(), data.getNBT()));
    }

    @Inject(method = "removePairing", at=@At("TAIL"))
    public void removePairing(ServerPlayer player, CallbackInfo ci) {
        if(this.entity instanceof EntityNPCInterface){
            EntityNPCInterface npc = (EntityNPCInterface)this.entity;
            npc.tracking.remove(player.getId());
        }
    }
}
