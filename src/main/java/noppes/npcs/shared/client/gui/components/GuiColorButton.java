package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.components.CustomGuiButton;

public class GuiColorButton extends CustomGuiButton {
	public int color;
	public GuiColorButton(GuiCustom parent, CustomGuiButtonWrapper component, int color) {
		super(parent, component);
		this.color = color;
	}
	
	@Override
	public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible)
        	return;
        graphics.fill(getX(), getY(), getX() + 50, getY() + 20, 0xFF000000 + color);
    }

}
