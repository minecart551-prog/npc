package noppes.npcs.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.items.ItemTeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntityLivingMixin {

    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);

    @Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
    private void addAdditionalSaveData(CompoundTag compound, CallbackInfo callbackInfo) {
        LivingEntity e = (LivingEntity)(Object)this;
        if(!e.level().isClientSide()) {
            MarkData.get(e).save();
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    public void swing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        ItemStack stack = getItemInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemTeleporter && ItemTeleporter.onEntitySwing(stack, (LivingEntity)(Object)this)) ci.cancel();
    }
}