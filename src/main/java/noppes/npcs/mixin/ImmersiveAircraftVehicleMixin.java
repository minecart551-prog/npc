package noppes.npcs.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class ImmersiveAircraftVehicleMixin {

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void cnpc$blockNpcRiding(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
        if (passenger instanceof EntityNPCInterface npc) {
            if (!npc.advanced.canRideVehicles) {
                cir.setReturnValue(false);
            }
        }
    }
}
