package noppes.npcs.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHelperMixin {

    @Accessor(value="activeButton")
    int getActiveButton();

    @Accessor(value="mouseGrabbed")
    void setGrabbed(boolean bo);

    @Accessor(value="xpos")
    void setX(double x);

    @Accessor(value="ypos")
    void setY(double y);
}
