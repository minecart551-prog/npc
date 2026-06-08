package noppes.npcs.shared.client.gui.listeners;

import net.minecraft.client.gui.screens.Screen;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiWrapper;



public interface IGuiInterface {
	void buttonEvent(GuiButtonNop button);

	void save();

	boolean hasSubGui();

	Screen getSubGui();

	int getWidth();

	int getHeight();

	Screen getParent();

	void elementClicked();

	void subGuiClosed(Screen subgui);

	GuiWrapper getWrapper();

	void initGui();
}
