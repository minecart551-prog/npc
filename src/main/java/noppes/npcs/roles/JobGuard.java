package noppes.npcs.roles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.NBTTags;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

public class JobGuard extends JobInterface{
	
	public List<String> targets = new ArrayList<String>();
	
	public JobGuard(EntityNPCInterface npc) {
		super(npc);
	}
	
	public boolean isEntityApplicable(Entity entity) {
    	if(entity instanceof Player || entity instanceof EntityNPCInterface) return false;
    	if(targets.contains(entity.getType().getDescriptionId())) return true;
		return false;
	}

	@Override
	public CompoundTag save(CompoundTag nbttagcompound) {
		nbttagcompound.put("GuardTargets", NBTTags.nbtStringList(targets));
		return nbttagcompound;
	}

	@Override
	public void load(CompoundTag nbttagcompound) {
		
		targets = NBTTags.getStringList(nbttagcompound.getList("GuardTargets", 10));
	}

	@Override
	public int getType() {
		return JobType.GUARD;
	}
}
