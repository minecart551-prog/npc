package noppes.npcs.shared.client.gui.listeners;


import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;

public interface ICustomScrollListener {

	void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll);
	
	void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll);

}
