package noppes.npcs.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.items.ItemScripted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager<T extends EntityAccess> {
    @Inject(method = "addEntity", at=@At("HEAD"), cancellable = true)
    private void addEntity(T ent, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (ent instanceof Entity entity){
            if(entity.level().isClientSide || !(entity instanceof ItemEntity entityItem))
                return;

            ItemStack stack = entityItem.getItem();
            if(!stack.isEmpty() && stack.getItem() == CustomItems.scripted_item) {
                if(EventHooks.onScriptItemSpawn(ItemScripted.GetWrapper(stack), entityItem)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
