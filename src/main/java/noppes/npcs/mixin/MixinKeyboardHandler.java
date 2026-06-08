package noppes.npcs.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import noppes.npcs.client.ClientTickHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "keyPress", at=@At("TAIL"))
    public void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (windowPointer == minecraft.getWindow().getWindow()) {
            ClientTickHandler.onKey(key, scanCode, modifiers, action);
        }
    }
}
