package noppes.npcs.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import noppes.npcs.api.event.NpcEvent.DamagedEvent;
import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public class AbilityBlock extends AbstractAbility implements IAbilityDamaged{

	public AbilityBlock(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public boolean canRun(LivingEntity target){
		if(!super.canRun(target))
			return false;
		return true;
	}

	@Override
	public boolean isType(EnumAbilityType type){
		return type == EnumAbilityType.ATTACKED;
	}

	@Override
	public void handleEvent(DamagedEvent event) {
		ServerLevel level = (ServerLevel) npc.getCommandSenderWorld();
		level.broadcastEntityEvent(npc, (byte)29);
		event.setCanceled(true);
		endAbility();
	}
}
