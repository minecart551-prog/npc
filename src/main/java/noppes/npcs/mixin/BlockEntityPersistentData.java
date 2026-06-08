package noppes.npcs.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import noppes.npcs.entity.data.IEntityPersistentData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class BlockEntityPersistentData implements IEntityPersistentData {
    @Unique
    private CompoundTag CNPC_tag;

    @Unique
    public CompoundTag getPersistentData(){
        if(CNPC_tag==null){
            CNPC_tag = new CompoundTag();
        }
        return CNPC_tag;
    }

    @Inject(method = "saveAdditional",at=@At("TAIL"))
    public void save(CompoundTag tag, CallbackInfo ci){
        if(CNPC_tag!=null) {
            tag.put("CNPC_persistantData", CNPC_tag);
        }
    }

    @Inject(method = "load",at=@At("TAIL"))
    public void read(CompoundTag compound, CallbackInfo ci){
        if(compound.contains("CNPC_persistantData")) {
            CNPC_tag = compound.getCompound("CNPC_persistantData");
        }
    }
}
