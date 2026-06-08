package noppes.npcs;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class CommonProxy {
	
	public boolean newVersionAvailable = false;
	public int revision = 4;

	public void load() {

	}

	public void postload() {
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
		// TODO Auto-generated method stub
	}

	public void openGui(Player player, EnumGuiType gui) {
		// TODO Auto-generated method stub
	}


	public void openGui(Player player, Object guiscreen) {
		// TODO Auto-generated method stub
		
	}

	public void spawnParticle(LivingEntity player, String string, Object... ob) {
		// TODO Auto-generated method stub
		
	}

	public boolean hasClient() {
		return false;
	}

	public Player getPlayer() {
		return null;
	}

	public void spawnParticle(ParticleOptions type, double x, double y, double z,
							  double motionX, double motionY, double motionZ, float scale) {
	}

	public PlayerData getPlayerData(Player player) {
		return null;
		
	}
}
