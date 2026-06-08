package noppes.npcs.api.wrapper;

import net.minecraft.world.entity.projectile.AbstractArrow;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IArrow;

public class ArrowWrapper<T extends AbstractArrow> extends EntityWrapper<T> implements IArrow{

	public ArrowWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return EntitiesType.ANIMAL;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntitiesType.ANIMAL?true:super.typeOf(type);
	}
}
