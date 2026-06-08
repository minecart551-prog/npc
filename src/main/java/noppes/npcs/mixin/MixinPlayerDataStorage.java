package noppes.npcs.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import noppes.npcs.controllers.data.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage {
    @Inject(method = "save", at = @At("TAIL"))
    public void save(Player player, CallbackInfo ci) {
        PlayerData.get(player).save(false);
    }
}
