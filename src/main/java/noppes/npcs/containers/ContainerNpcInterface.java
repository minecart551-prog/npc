package noppes.npcs.containers;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.wrapper.ContainerWrapper;

public class ContainerNpcInterface extends AbstractContainerMenu {
	private int posX, posZ;
	public Player player;
	public IContainer scriptContainer;

	public ContainerNpcInterface(MenuType type, int containerId, Inventory playerInventory){
		super(type, containerId);
		this.player = playerInventory.player;
    	posX = Mth.floor(player.getX());
    	posZ = Mth.floor(player.getZ());
    	player.setDeltaMovement(Vec3.ZERO);
	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
        return !player.isRemoved() && posX == Mth.floor(player.getX()) && posZ == Mth.floor(player.getZ());
	}

	public static IContainer getOrCreateIContainer(ContainerNpcInterface container) {
		if(container.scriptContainer != null)
			return container.scriptContainer;
		return container.scriptContainer = new ContainerWrapper(container);
	}

}
