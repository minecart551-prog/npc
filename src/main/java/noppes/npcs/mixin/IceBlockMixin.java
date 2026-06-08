package noppes.npcs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.CustomNpcs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(IceBlock.class)
public class IceBlockMixin {

    @Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
    private void setupAnimPre(BlockState p_221355_, ServerLevel p_221356_, BlockPos p_221357_, RandomSource p_221358_, CallbackInfo ci) {
        if(!CustomNpcs.IceMeltsEnabled) {
            ci.cancel();
        }
    }
}
