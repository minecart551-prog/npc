package noppes.npcs.api.wrapper;

import net.minecraft.world.entity.animal.Animal;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IAnimal;


public class AnimalWrapper<T extends Animal> extends EntityLivingWrapper<T> implements IAnimal{

	public AnimalWrapper(T entity) {
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
