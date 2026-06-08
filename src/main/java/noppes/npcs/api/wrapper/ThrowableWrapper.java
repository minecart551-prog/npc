package noppes.npcs.api.wrapper;

import net.minecraft.world.entity.projectile.ThrowableProjectile;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IThrowable;

public class ThrowableWrapper<T extends ThrowableProjectile> extends EntityWrapper<T> implements IThrowable{

	public ThrowableWrapper(T entity) {
		super(entity);
	}

	@Override
	public int getType() {
		return EntitiesType.THROWABLE;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntitiesType.THROWABLE?true:super.typeOf(type);
	}
}
