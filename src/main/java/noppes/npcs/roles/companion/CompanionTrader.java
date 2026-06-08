package noppes.npcs.roles.companion;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumGuiType;



public class CompanionTrader extends CompanionJobInterface{

	@Override
	public CompoundTag getNBT() {
		CompoundTag compound = new CompoundTag();
		return compound;
	}

	@Override
	public void setNBT(CompoundTag compound) {
		
	}
	
	public void interact(Player player){
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionTrader, npc);
	}

	@Override
	public EnumCompanionJobs getType() {
		return EnumCompanionJobs.SHOP;
	}
}
