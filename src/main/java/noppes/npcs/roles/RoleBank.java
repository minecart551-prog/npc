package noppes.npcs.roles;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.entity.EntityNPCInterface;



public class RoleBank extends RoleInterface{
	
	public int bankId = -1;
	
	public RoleBank(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public CompoundTag save(CompoundTag nbttagcompound) {
		nbttagcompound.putInt("RoleBankID", bankId);
		return nbttagcompound;
	}

	@Override
	public void load(CompoundTag nbttagcompound) {
		bankId = nbttagcompound.getInt("RoleBankID");
	}

	@Override
	public void interact(Player player) {
		NoppesUtilServer.setEditingNpc(player, npc);
		BankData data = PlayerDataController.instance.getBankData(player,bankId).getBankOrDefault(bankId);
		data.openBankGui((ServerPlayer)player,npc,bankId,0);
		npc.say(player, npc.advanced.getInteractLine());
	}

	public Bank getBank() {
		Bank bank = BankController.getInstance().banks.get(bankId);
		if(bank != null)
			return bank;
		return BankController.getInstance().banks.values().iterator().next();
	}

	@Override
	public int getType() {
		return RoleType.BANK;
	}
}
