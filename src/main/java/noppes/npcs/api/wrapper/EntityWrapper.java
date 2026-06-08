package noppes.npcs.api.wrapper;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.*;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.data.IEntityPersistentData;
import noppes.npcs.mixin.EntityIMixin;
import noppes.npcs.mixin.EntityPersistentData;

import java.util.*;

public class EntityWrapper<T extends Entity> implements IEntity{
	protected T entity;
	private Map<String, Object> tempData = new HashMap<String, Object>();
	private IWorld levelWrapper;
	

	private final IData tempdata = new IData() {

		@Override
		public void put(String key, Object value) {
			tempData.put(key, value);
		}

		@Override
		public Object get(String key) {
			return tempData.get(key);
		}

		@Override
		public void remove(String key) {
			tempData.remove(key);
		}

		@Override
		public boolean has(String key) {
			return tempData.containsKey(key);
		}

		@Override
		public void clear() {
			tempData.clear();
		}

		@Override
		public String[] getKeys() {
			return tempData.keySet().toArray(new String[tempData.size()]);
		}
		
	};
	

	private final IData storeddata = new IData() {

		@Override
		public void put(String key, Object value) {
			CompoundTag compound = getStoredCompound();
			if(value instanceof Number){
				compound.putDouble(key, ((Number) value).doubleValue());
			}
			else if(value instanceof String)
				compound.putString(key, (String)value);
			saveStoredCompound(compound);
		}

		@Override
		public Object get(String key) {
			CompoundTag compound = getStoredCompound();
			if(!compound.contains(key))
				return null;
			Tag base = compound.get(key);
			if(base instanceof NumericTag)
				return ((NumericTag)base).getAsDouble();
			return base.getAsString();
		}

		@Override
		public void remove(String key) {
			CompoundTag compound = getStoredCompound();
			compound.remove(key);
			saveStoredCompound(compound);
		}

		@Override
		public boolean has(String key) {
			return getStoredCompound().contains(key);
		}

		@Override
		public void clear() {
			((IEntityPersistentData)entity).getPersistentData().remove("CNPCStoredData");
		}
		
		private CompoundTag getStoredCompound(){
			CompoundTag compound = ((IEntityPersistentData)entity).getPersistentData().getCompound("CNPCStoredData");
			if(compound == null)
				((IEntityPersistentData)entity).getPersistentData().put("CNPCStoredData", compound = new CompoundTag());
			return compound;
		}
		
		private void saveStoredCompound(CompoundTag compound){
			((IEntityPersistentData)entity).getPersistentData().put("CNPCStoredData", compound);
		}

		@Override
		public String[] getKeys() {
			CompoundTag compound = getStoredCompound();
			return compound.getAllKeys().toArray(new String[compound.getAllKeys().size()]);
		}
	};
	
	public EntityWrapper(T entity){
		this.entity = entity;
		this.levelWrapper = NpcAPI.Instance().getIWorld((ServerLevel)entity.level());
	}
	@Override
	public double getX() {
		return entity.getX();
	}

	@Override
	public void setX(double x) {
		entity.setPos(x, entity.getY(), entity.getZ());
	}

	@Override
	public double getY() {
		return entity.getY();
	}

	@Override
	public void setY(double y) {
		entity.setPos(entity.getX(), y, entity.getZ());
	}

	@Override
	public double getZ() {
		return entity.getZ();
	}

	@Override
	public void setZ(double z) {
		entity.setPos(entity.getX(), entity.getY(), z);
	}

	@Override
	public int getBlockX() {
		return Mth.floor(entity.getX());
	}

	@Override
	public int getBlockY() {
		return Mth.floor(entity.getY());
	}

	@Override
	public int getBlockZ() {
		return Mth.floor(entity.getZ());
	}

	@Override
	public String getEntityName() {
        String s = entity.getType().getDescriptionId();
        return Language.getInstance().getOrDefault(s);
	}
	
	@Override
	public String getName() {
		return entity.getName().getString();
	}

	@Override
	public void setName(String name) {
		entity.setCustomName(Component.literal(name));
	}

	@Override
	public boolean hasCustomName() {
		return entity.hasCustomName();
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		entity.setPos(x, y, z);
	}
	
	@Override
	public IWorld getWorld() {
		if(entity.level() != levelWrapper.getMCLevel())
			this.levelWrapper = NpcAPI.Instance().getIWorld((ServerLevel) entity.level());
		return levelWrapper;
	}

	@Override
	public boolean isAlive(){
		return entity.isAlive();
	}

	@Override
	public IData getTempdata(){
		return tempdata;
	}

	@Override
	public IData getStoreddata(){
		return storeddata;
	}

	@Override
	public long getAge(){
		return entity.tickCount;
	}

	@Override
	public void damage(float amount){
		if(getType()==1 && (((IPlayer)this).getGamemode()==1 || ((IPlayer)this).getGamemode()==3)) return;
		entity.hurt(entity.damageSources().genericKill(), amount);
	}

	@Override
    public void damage(float damage, IEntity source) {
        if(source.getMCEntity() instanceof Player){
            entity.hurt(entity.damageSources().playerAttack((Player)source.getMCEntity()), damage);
        }else {
            Holder<DamageType> damageTypeHolder = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NpcDamageSource.NPC);
            entity.hurt(new DamageSource(damageTypeHolder, source.getMCEntity()), damage);
        }
    }

	@Override
	public void despawn(){
		entity.discard();
	}

	@Override
	public void spawn() {
		if(levelWrapper.getMCLevel().getEntity(entity.getUUID()) != null)
			throw new CustomNPCsException("Entity is already spawned");
		((EntityIMixin)entity).removal(null);
		levelWrapper.getMCLevel().addFreshEntity(entity);
	}

	@Override
	public void kill(){
		entity.kill();
	}

	@Override
	public boolean inWater(){
		return entity.isInWater();
	}

	@Override
	public boolean inLava(){
		return entity.isInLava();
	}

	@Override
	public boolean inFire(){
		return entity.level().getBlockStates(entity.getBoundingBox()).anyMatch((state) -> state.is(BlockTags.FIRE));
	}
	
	@Override
	public boolean isBurning(){
		return entity.isOnFire();
	}

	@Override
	public void setBurning(int ticks){
		entity.setRemainingFireTicks(ticks);
	}

	@Override
	public void extinguish(){
		entity.clearFire();
	}

	@Override
	public String getTypeName(){
		return entity.getEncodeId();
	}

	@Override
	public IEntityItem dropItem(IItemStack item){
		return (IEntityItem) NpcAPI.Instance().getIEntity(entity.spawnAtLocation(item.getMCItemStack(), 0));
	}

	@Override
	public IEntity[] getRiders(){
		List<Entity> list = entity.getPassengers();
		IEntity[] riders = new IEntity[list.size()];
		for(int i = 0; i < list.size(); i++){
			riders[i] = NpcAPI.Instance().getIEntity(list.get(i));
		}
		return riders;
	}

	@Override
	public IRayTrace rayTraceBlock(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
        Vec3 vec3d = entity.getEyePosition(1);
        Vec3 vec3d1 = entity.getViewVector(1);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
		HitResult result = entity.level().clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.OUTLINE, stopOnLiquid ? ClipContext.Fluid.ANY: ClipContext.Fluid.NONE, entity));
		if(result.getType() == HitResult.Type.MISS)
        	return null;
		BlockHitResult br = (BlockHitResult) result;
        return new RayTraceWrapper(NpcAPI.Instance().getIBlock(entity.level(), br.getBlockPos()), br.getDirection().get3DDataValue());
	}

	@Override
	public IEntity[] rayTraceEntities(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
        Vec3 vec3d = entity.getEyePosition(1);
        Vec3 vec3d1 = entity.getViewVector(1);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
        //HitResult result = entity.level.clip(vec3d, vec3d2, stopOnLiquid ? RayTraceFluidMode.ALWAYS: ClipContext.Fluid.NONE, ignoreBlockWithoutBoundingBox, false);
		HitResult result = entity.level().clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, stopOnLiquid ? ClipContext.Fluid.ANY: ClipContext.Fluid.NONE, entity));
		if(result.getType() != HitResult.Type.MISS) {
			vec3d2 = result.getLocation();
		}
        return this.findEntityOnPath(distance, vec3d, vec3d2);
	}
	
	private IEntity[] findEntityOnPath(double distance, Vec3 vec3d, Vec3 vec3d1) {

        List<Entity> list = entity.level().getEntities(entity, entity.getBoundingBox().inflate(distance));

        List<IEntity> result = new ArrayList<IEntity>();
        for (Entity entity1 : list){
            if (entity1 != this.entity){
                AABB axisalignedbb = entity1.getBoundingBox().inflate(entity1.getPickRadius());

				Optional<Vec3> optional = axisalignedbb.clip(vec3d, vec3d1);
				if (optional.isPresent()) {
                    result.add(NpcAPI.Instance().getIEntity(entity1));
                }
                
            }
        }
        result.sort((o1, o2) -> {
            double d1 = EntityWrapper.this.entity.distanceToSqr(o1.getMCEntity());
            double d2 = EntityWrapper.this.entity.distanceToSqr(o2.getMCEntity());
            if (d1 == d2)
                return 0;
            return d1 > d2 ? 1 : -1;
        });
        return result.toArray(new IEntity[result.size()]);
	}

	@Override
	public IEntity[] getAllRiders(){

		List<Entity> list = ImmutableList.copyOf(entity.getIndirectPassengers());
		IEntity[] riders = new IEntity[list.size()];
		for(int i = 0; i < list.size(); i++){
			riders[i] = NpcAPI.Instance().getIEntity(list.get(i));
		}
		return riders;
	}

	@Override
	public void addRider(IEntity entity){
		if(entity != null){
			entity.getMCEntity().startRiding(this.entity, true);
		}
	}

	@Override
	public void clearRiders(){
		entity.ejectPassengers();
	}
	
	@Override
	public IEntity getMount(){
		return NpcAPI.Instance().getIEntity(entity.getVehicle());
	}
	
	@Override
	public void setMount(IEntity entity){
		if(entity == null)
			this.entity.stopRiding();
		else {
			this.entity.startRiding(entity.getMCEntity(), true);
		}
	}

	@Override
	public void setRotation(float rotation){
		entity.setYRot(rotation);
	}

	@Override
	public float getRotation(){
		return entity.getYRot();
	}

	@Override
	public void setPitch(float rotation){
		entity.setXRot(rotation);
	}

	@Override
	public float getPitch(){
		return entity.getXRot();
	}

	@Override
	public void knockback(int power, float direction){
		float v = direction * (float)Math.PI / 180.0F;
        entity.push(-Mth.sin(v) * (float)power, 0.1D + power * 0.04f, Mth.cos(v) * (float)power);
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6, 1, 0.6));
        entity.hurtMarked = true;
	}

	@Override
	public boolean isSneaking(){
		return entity.isCrouching();
	}

	@Override
	public boolean isSprinting(){
		return entity.isSprinting();
	}

	@Override
	public T getMCEntity(){
		return entity;
	}

	@Override
	public int getType() {
		return EntitiesType.UNKNOWN;
	}

	@Override
	public boolean typeOf(int type){
		return type == getType();
	}
	
	@Override
	public String getUUID() {
		return entity.getUUID().toString();
	}
	
	@Override
	public String generateNewUUID() {
		UUID id = UUID.randomUUID();
		entity.setUUID(id);
		return id.toString();
	}
	
	@Override
	public INbt getNbt() {
		return NpcAPI.Instance().getINbt(((IEntityPersistentData)entity).getPersistentData());
	}
	
	@Override
	public void storeAsClone(int tab, String name) {
		CompoundTag compound = new CompoundTag();
		if(!entity.saveAsPassenger(compound))
			throw new CustomNPCsException("Cannot store dead entities");
		ServerCloneController.Instance.addClone(compound, name, tab);
	}

	@Override
	public INbt getEntityNbt(){
		CompoundTag compound = new CompoundTag();
		entity.saveWithoutId(compound);
		ResourceLocation resourcelocation = net.minecraft.world.entity.EntityType.getKey(entity.getType());
		if(getType() == EntitiesType.PLAYER) {
			resourcelocation = new ResourceLocation("player");
		}
		if(resourcelocation != null) {
            compound.putString("id", resourcelocation.toString());
		}
		return NpcAPI.Instance().getINbt(compound);
	}

	@Override
	public void setEntityNbt(INbt nbt){
		entity.load(nbt.getMCNBT());
	}

	@Override
	public void playAnimation(int type) {
		levelWrapper.getMCLevel().getChunkSource().broadcastAndSend(entity, new ClientboundAnimatePacket(entity, type));
	}

	@Override
	public float getHeight() {
		return entity.getBbHeight();
	}

	@Override
	public float getEyeHeight() {
		return entity.getEyeHeight();
	}

	@Override
	public float getWidth() {
		return entity.getBbWidth();
	}
	@Override
	public IPos getPos() {
		return new BlockPosWrapper(entity.blockPosition());
	}
	@Override
	public void setPos(IPos pos) {
		entity.setPos(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
	}
	@Override
	public String[] getTags() {
		return entity.getTags().toArray(new String[entity.getTags().size()]);
	}
	@Override
	public void addTag(String tag) {
		entity.addTag(tag);
	}
	@Override
	public boolean hasTag(String tag) {
		return entity.getTags().contains(tag);
	}
	@Override
	public void removeTag(String tag) {
		entity.removeTag(tag);
	}
	@Override
	public double getMotionX() {
		return entity.getDeltaMovement().x;
	}
	@Override
	public double getMotionY() {
		return entity.getDeltaMovement().y;
	}
	@Override
	public double getMotionZ() {
		return entity.getDeltaMovement().z;
	}
	@Override
	public void setMotionX(double motion) {
		Vec3 mo = entity.getDeltaMovement();
		if(mo.x == motion)
			return;
		entity.setDeltaMovement(motion, mo.y, mo.z);
		entity.hurtMarked = true;
	}
	@Override
	public void setMotionY(double motion) {
		Vec3 mo = entity.getDeltaMovement();
		if(mo.y == motion)
			return;
		entity.setDeltaMovement(mo.x, motion, mo.z);
		entity.hurtMarked = true;
	}
	@Override
	public void setMotionZ(double motion) {
		Vec3 mo = entity.getDeltaMovement();
		if(mo.z == motion)
			return;
		entity.setDeltaMovement(mo.x, mo.y, motion);
		entity.hurtMarked = true;
	}
}
