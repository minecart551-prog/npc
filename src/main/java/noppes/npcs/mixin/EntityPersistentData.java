package noppes.npcs.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.data.IEntityPersistentData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityPersistentData implements IEntityPersistentData {
    @Shadow private Level level;
    @Unique
    private CompoundTag CNPC_tag;

    @Unique
    public CompoundTag getPersistentData(){
        if(CNPC_tag==null){
            CNPC_tag = new CompoundTag();
        }
        return CNPC_tag;
    }

    @Inject(method = "saveWithoutId",at=@At("TAIL"))
    public void save(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir){
        if(CNPC_tag!=null) {
            MarkData.get((LivingEntity)(Object)this).save();
            compound.put("CNPC_persistantData", CNPC_tag);
        }
    }

    @Inject(method = "load",at=@At("TAIL"))
    public void read(CompoundTag compound, CallbackInfo ci){
        if(compound.contains("CNPC_persistantData")) {
            CNPC_tag = compound.getCompound("CNPC_persistantData");
            MarkData.get((LivingEntity)(Object)this);
        }
    }
}
