package noppes.npcs.api.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.shared.common.util.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class WrapperEntityData {
	// This used to hold a static Map<Integer, WrapperEntityData> keyed by entity id. Entity
	// ids are never reused, so it grew by one entry for every entity that was ever looked up
	// and was never pruned. It was also dead weight: the mapping function always produced the
	// shared "backup" instance, so the map never actually cached anything - the lookup below
	// always fell through to getData(entity). Dropping it keeps the exact same behaviour
	// without the unbounded growth.

	public IEntity base;
	public WrapperEntityData(IEntity base){
		this.base = base;
	}

	public static IEntity get(Entity entity){
		if(entity == null || entity.position() == Vec3.ZERO)
			return null;
		WrapperEntityData data = getData(entity);
		if(data == null)
			return null;
		return data.base;
	}

	private static final ResourceLocation key = new ResourceLocation("customnpcs", "entitydata");
	private static WrapperEntityData getData(Entity entity) {
		if(entity == null || entity.level() == null || entity.level().isClientSide)
			return null;
		
		if(entity instanceof ServerPlayer)
			return new WrapperEntityData(new PlayerWrapper((ServerPlayer) entity));
		else if(PixelmonHelper.isPixelmon(entity))
			return new WrapperEntityData(new PixelmonWrapper((AbstractHorse)entity));
		else if(entity instanceof Villager)
			return new WrapperEntityData(new VillagerWrapper((Villager)entity));
		else if(entity instanceof Animal)
			return new WrapperEntityData(new AnimalWrapper((Animal) entity));
		else if(entity instanceof Monster)
			return new WrapperEntityData(new MonsterWrapper((Monster) entity));
		else if(entity instanceof Mob)
			return new WrapperEntityData(new EntityLivingWrapper((Mob)entity));
		else if(entity instanceof LivingEntity)
			return new WrapperEntityData(new EntityLivingBaseWrapper((LivingEntity) entity));
		else if(entity instanceof ItemEntity)
			return new WrapperEntityData(new EntityItemWrapper((ItemEntity)entity));
		else if(entity instanceof EntityProjectile)
			return new WrapperEntityData(new ProjectileWrapper((EntityProjectile)entity));
		else if(entity instanceof ThrowableProjectile)
			return new WrapperEntityData(new ThrowableWrapper((ThrowableProjectile)entity));
		else if(entity instanceof AbstractArrow)
			return new WrapperEntityData(new ArrowWrapper((AbstractArrow)entity));
		return new WrapperEntityData(new EntityWrapper(entity));

	}
}
