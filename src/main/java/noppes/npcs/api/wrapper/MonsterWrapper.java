package noppes.npcs.api.wrapper;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IMonster;

public class MonsterWrapper<T extends Monster> extends EntityLivingWrapper<T> implements IMonster{
	public MonsterWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return EntitiesType.MONSTER;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntitiesType.MONSTER?true:super.typeOf(type);
	}
}
