package noppes.npcs.api.wrapper;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntitiesType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.MarkData;

public class EntityLivingBaseWrapper<T extends LivingEntity> extends EntityWrapper<T> implements IEntityLiving {
	
	public EntityLivingBaseWrapper(T entity) {
		super(entity);
	}

	@Override
	public float getHealth(){
		return entity.getHealth();
	}

	@Override
	public void setHealth(float health){
		entity.setHealth(health);
	}

	@Override
	public float getMaxHealth(){
		return entity.getMaxHealth();
	}
	
	@Override
	public void setMaxHealth(float health){
		if(health < 0)
			return;
		entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
	}

	@Override
	public boolean isAttacking(){
		return entity.getLastHurtByMob() != null;
	}

	@Override
	public void setAttackTarget(IEntityLiving living){
		if(living == null)
			entity.setLastHurtByMob(null);
		else
			entity.setLastHurtByMob((LivingEntity) living.getMCEntity());
	}

	@Override
	public IEntityLiving getAttackTarget(){
		return (IEntityLiving)NpcAPI.Instance().getIEntity(entity.getLastHurtByMob());
	}

	@Override
	public IEntityLiving getLastAttacked(){
		return (IEntityLiving)NpcAPI.Instance().getIEntity(entity.getLastHurtMob());
	}

	@Override
	public int getLastAttackedTime(){
		return entity.getLastHurtMobTimestamp();
	}

	@Override
	public boolean canSeeEntity(IEntity entity){
		return this.entity.hasLineOfSight(entity.getMCEntity());
	}

	@Override
	public void swingMainhand(){
		entity.swing(InteractionHand.MAIN_HAND);
	}

	@Override
	public void swingOffhand(){
		entity.swing(InteractionHand.OFF_HAND);
	}

	@Override
	public void addPotionEffect(int effect, int duration, int strength, boolean hideParticles){
		MobEffect p = MobEffect.byId(effect);
        if (p == null)
        	return;
        
		if(strength < 0)
			strength = 0;
		else if(strength > 255)
			strength = 255;

		if(duration < 0)
			duration = 0;
		else if(duration > 1000000)
			duration = 1000000;
		
		if(!p.isInstantenous())
			duration *= 20;
		
		if(duration == 0)
			entity.removeEffect(p);
		else
			entity.addEffect(new MobEffectInstance(p, duration, strength, false, hideParticles));
	}

	@Override
	public void clearPotionEffects(){
		entity.removeAllEffects();
	}

	@Override
	public int getPotionEffect(int effect){
		MobEffectInstance pf = entity.getEffect(MobEffect.byId(effect));
		if(pf == null)
			return -1;
		return pf.getAmplifier();
	}
	
	@Override
	public IItemStack getMainhandItem(){
		return NpcAPI.Instance().getIItemStack(entity.getMainHandItem());
	}

	@Override
	public void setMainhandItem(IItemStack item){
		entity.setItemInHand(InteractionHand.MAIN_HAND, item == null?ItemStack.EMPTY:item.getMCItemStack());
	}
	
	@Override
	public IItemStack getOffhandItem(){
		return NpcAPI.Instance().getIItemStack(entity.getOffhandItem());
	}

	@Override
	public void setOffhandItem(IItemStack item){
		entity.setItemInHand(InteractionHand.OFF_HAND, item == null?ItemStack.EMPTY:item.getMCItemStack());
	}

	@Override
	public IItemStack getArmor(int slot){
		if(slot < 0 || slot > 3)
			throw new CustomNPCsException("Wrong slot id:" + slot);
		return NpcAPI.Instance().getIItemStack(entity.getItemBySlot(getSlot(slot)));
	}

	@Override
	public void setArmor(int slot, IItemStack item){
		if(slot < 0 || slot > 3)
			throw new CustomNPCsException("Wrong slot id:" + slot);
		entity.setItemSlot(getSlot(slot), item == null?ItemStack.EMPTY:item.getMCItemStack());
	}

	private EquipmentSlot getSlot(int slot) {
		if(slot == 3)
			return EquipmentSlot.HEAD;
		if(slot == 2)
			return EquipmentSlot.CHEST;
		if(slot == 1)
			return EquipmentSlot.LEGS;
		if(slot == 0)
			return EquipmentSlot.FEET;
		return null;
	}

	@Override
	public float getRotation(){
		return entity.yBodyRot;
	}

	@Override
	public void setRotation(float rotation){
		entity.yBodyRot = rotation;
	}

	@Override
	public int getType() {
		return EntitiesType.LIVING;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntitiesType.LIVING?true:super.typeOf(type);
	}

	@Override
	public boolean isChild() {
		return entity.isBaby();
	}

	@Override
	public IMark addMark(int type) {
		MarkData data = MarkData.get(entity);
		return data.addMark(type);
	}

	@Override
	public void removeMark(IMark mark) {
		MarkData data = MarkData.get(entity);
		data.marks.remove(mark);
		data.syncClients();
	}

	@Override
	public IMark[] getMarks() {
		MarkData data = MarkData.get(entity);
		return data.marks.toArray(new IMark[data.marks.size()]);
	}

	@Override
	public float getMoveForward() {
		return entity.zza;
	}

	@Override
	public void setMoveForward(float move) {
		entity.zza = move;
	}

	@Override
	public float getMoveStrafing() {
		return entity.xxa;
	}

	@Override
	public void setMoveStrafing(float move) {
		entity.xxa = move;
	}

	@Override
	public float getMoveVertical() {
		return entity.yya;
	}

	@Override
	public void setMoveVertical(float move) {
		entity.yya = move;
	}
}
