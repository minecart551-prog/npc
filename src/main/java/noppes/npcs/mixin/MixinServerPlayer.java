package noppes.npcs.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.CustomItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {
    public MixinServerPlayer(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "findDimensionEntryPoint", at=@At("HEAD"), cancellable = true)
    protected void findDimensionEntryPoint(ServerLevel destination, CallbackInfoReturnable<PortalInfo> cir) {
        if(getMainHandItem().is(CustomItems.teleporter)){
            cir.setReturnValue(new PortalInfo(position(), Vec3.ZERO, getYRot(), getXRot()));
        }
    }
}
