package noppes.npcs.api.wrapper;

import net.minecraft.world.entity.npc.Villager;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IVillager;

public class VillagerWrapper<T extends Villager> extends EntityLivingWrapper<T> implements IVillager {

	public VillagerWrapper(T entity) {
		super(entity);
	}

	public String getProfession() {
		return entity.getVillagerData().getProfession().toString();
	}

	public String VillagerType(){
		return entity.getVillagerData().getType().toString();
	}

	@Override
	public int getType() {
		return EntitiesType.VILLAGER;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntitiesType.VILLAGER?true:super.typeOf(type);
	}
}
