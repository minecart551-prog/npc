package noppes.npcs.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import noppes.npcs.client.gui.player.GuiMailmanWrite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {

    @Shadow @Nullable protected Slot hoveredSlot;

    @Shadow protected abstract boolean checkHotbarKeyPressed(int keyCode, int scanCode);

    @Shadow protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

    protected MixinAbstractContainerScreen(Component title) {
        super(title);
    }

    @Inject(method = "keyPressed", at=@At("HEAD"),cancellable = true)
    public void keyPressed(int p_97765_, int p_97766_, int p_97767_, CallbackInfoReturnable<Boolean> cir) {
        if(((Object)this) instanceof GuiMailmanWrite){
            if (super.keyPressed(p_97765_, p_97766_, p_97767_)) {
                cir.setReturnValue(true);
            } else if (minecraft.options.keyInventory.matches(p_97765_,p_97766_)) {
                this.onClose();
                cir.setReturnValue(true);
            } else {
                boolean handled = checkHotbarKeyPressed(p_97765_, p_97766_);// Forge MC-146650: Needs to return true when the key is handled
                if (hoveredSlot != null && hoveredSlot.hasItem()) {
                    if (this.minecraft.options.keyPickItem.matches(p_97765_,p_97766_)) {
                        slotClicked(hoveredSlot, hoveredSlot.index, 0, ClickType.CLONE);
                        handled = true;
                    } else if (this.minecraft.options.keyDrop.matches(p_97765_,p_97766_)) {
                        slotClicked(hoveredSlot, hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
                        handled = true;
                    }
                } else if (this.minecraft.options.keyDrop.matches(p_97765_,p_97766_)) {
                    handled = true; // Forge MC-146650: Emulate MC bug, so we don't drop from hotbar when pressing drop without hovering over a item.
                }

                cir.setReturnValue(handled);
            }
        }
    }
}
