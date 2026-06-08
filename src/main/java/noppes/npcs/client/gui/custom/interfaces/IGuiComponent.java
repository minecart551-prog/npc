package noppes.npcs.client.gui.custom.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import noppes.npcs.api.gui.IComponentsWrapper;
import noppes.npcs.api.gui.ICustomGuiComponent;

public interface IGuiComponent {

    int getID();
    void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);
    default void onRenderPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks){};
    void init();
    ICustomGuiComponent component();
}
