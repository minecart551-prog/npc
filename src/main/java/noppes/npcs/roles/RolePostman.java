package noppes.npcs.roles;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.client.util.NoppesStringUtils;

import java.util.ArrayList;
import java.util.List;

public class RolePostman extends RoleInterface{

	public NpcMiscInventory inventory = new NpcMiscInventory(1);
	private List<Player> recentlyChecked = new ArrayList<Player>();
	private List<Player> toCheck;
	
	public RolePostman(EntityNPCInterface npc) {
		super(npc);
	}

	public boolean aiShouldExecute() {
		if(npc.tickCount % 20 != 0)
			return false;

		toCheck = npc.level().getEntitiesOfClass(Player.class, npc.getBoundingBox().inflate(10, 10, 10));
		toCheck.removeAll(recentlyChecked);

		List<Player> listMax = npc.level().getEntitiesOfClass(Player.class, npc.getBoundingBox().inflate(20, 20, 20));
		recentlyChecked.retainAll(listMax);
		recentlyChecked.addAll(toCheck);
		
		for(Player player : toCheck){
			if(PlayerData.get(player).mailData.hasMail()){
				npc.say(player, new Line("mailbox.gotmail"));
			}
		}
		return false;
	}
	
	@Override
	public boolean aiContinueExecute() {
		return false;
	}
	
	@Override
	public CompoundTag save(CompoundTag nbttagcompound) {
    	nbttagcompound.put("PostInv", inventory.getToNBT());
    	return nbttagcompound;
	}

	@Override
	public void load(CompoundTag nbttagcompound) {
		inventory.setFromNBT(nbttagcompound.getCompound("PostInv"));
	}


	@Override
	public void interact(Player player) {
		NoppesUtilServer.openContainerGui((ServerPlayer) player, EnumGuiType.PlayerMailman, (buf) -> {
			buf.writeBoolean(true);
			buf.writeBoolean(true);
		});
	}

	@Override
	public int getType() {
		return RoleType.MAILMAN;
	}

}
