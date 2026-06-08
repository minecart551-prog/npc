package noppes.npcs.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.EditBox;
import noppes.npcs.shared.client.gui.components.GuiTextFieldNop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EditBox.class)
public class MixinEditBox {

    @Redirect(method = "insertText", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;filterText(Ljava/lang/String;)Ljava/lang/String;"))
    public String filterTextProxy(String p_136191_) {
        if(((Object)this) instanceof GuiTextFieldNop) {
            return p_136191_;
        }
        return SharedConstants.filterText(p_136191_);
    }
}
