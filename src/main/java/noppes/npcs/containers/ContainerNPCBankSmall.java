package noppes.npcs.containers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.CustomContainer;

public class ContainerNPCBankSmall extends ContainerNPCBankInterface
{

    public ContainerNPCBankSmall(int containerId, Inventory playerInventory, int slot, int bankid) {
        super(CustomContainer.container_banksmall, containerId, playerInventory,slot,bankid);
    }

    public boolean isAvailable(){
    	return true;
    }
    public int getRowNumber() {
		return 3;
	}
}
