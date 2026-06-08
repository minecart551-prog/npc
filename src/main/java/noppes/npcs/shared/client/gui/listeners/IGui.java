package noppes.npcs.shared.client.gui.listeners;

import net.minecraft.client.gui.GuiGraphics;

public interface IGui {

	public int getID();
	
	public void render(GuiGraphics graphics, int xMouse, int yMouse);

	public void tick();
	
	public boolean isActive();
}
