package noppes.npcs.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import noppes.npcs.ModelData;
import noppes.npcs.ModelEyeData;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.parts.MpmPartData;
import noppes.npcs.constants.EnumParts;

public class EntityCustomNpc extends EntityNPCFlying{
	public ModelData modelData = new ModelData(this);

	public EntityCustomNpc(EntityType<? extends PathfinderMob> type, Level world) {
		super(type, world);
	}
	//s 29031 r 29038
    @Override
	public void readAdditionalSaveData(CompoundTag compound) {
		if(compound.contains("NpcModelData"))
			modelData.load(compound.getCompound("NpcModelData"));
		super.readAdditionalSaveData(compound);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.put("NpcModelData", modelData.save());
	}

	@Override
    public boolean saveAsPassenger(CompoundTag compound){
    	boolean bo = super.saveAsPassenger(compound);
    	if(bo){
    		String s = getEncodeId();
    		if(s.equals("minecraft:customnpcs.customnpc")){
    			compound.putString("id", "customnpcs:customnpc");
    		}
    	}
    	return bo;
    }

	@Override
    public void tick(){
    	super.tick();
    	if(isClientSide()){
//	        ModelPartData particles = modelData.getPartData(EnumParts.PARTICLES);
//	    	if(particles != null && !isKilled()){
//	    		CustomNpcs.proxy.spawnParticle(this, "ModelData", modelData, particles);
//	    	}
	    	LivingEntity entity = modelData.getEntity(this);
	    	if(entity != null){
	    		try{
	    			entity.tick();
	    		}
	    		catch(Exception e){
	    		}
				EntityUtil.Copy(this, entity);
	    	}
    	}
		for(MpmPartData pd : modelData.mpmParts){
			if(pd instanceof ModelEyeData){
				((ModelEyeData)pd).update(this);
			}
		}
    }

	@Override
    public boolean startRiding(Entity par1Entity, boolean force){
    	boolean b = super.startRiding(par1Entity, force);
    	refreshDimensions();
    	return b;
    }

	@Override
	public void refreshDimensions() {
		Entity entity = modelData.getEntity(this);
		if(entity != null){
			entity.refreshDimensions();
		}
		super.refreshDimensions();
	}
	
	@Override
	public EntityDimensions getDimensions(Pose pos) {
		Entity entity = modelData.getEntity(this);
		if(entity == null){
			float height = 1.9f - modelData.getBodyY() + (modelData.getPartConfig(EnumParts.HEAD).scaleY - 1) / 2;
			if(baseSize.height != height){
				baseSize = new EntityDimensions(baseSize.width, height, false);
			}
			return super.getDimensions(pos);
		}
		else{
			EntityDimensions size = entity.getDimensions(pos);
			if(entity instanceof EntityNPCInterface){
				return size.scale(display.getSize() * 0.2f);
			}

			float width = (size.width / 5f) * display.getSize();
			float height = (size.height / 5f) * display.getSize();

			if(width < 0.1f)
				width = 0.1f;
			if(height < 0.1f)
				height = 0.1f;
			if(display.getHitboxState() == 1 || isKilled() && stats.hideKilledBody) {
				width = 0.00001f;
			}

//			if(width / 2 > level().getMaxEntityRadius()) {
//				level().increaseMaxEntityRadius(width / 2);
//			}
			//this.setPos(posX, posY, posZ);
			return new EntityDimensions(width, height, false);
		}
	}

	@Override
	public double getPassengersRidingOffset() {
		Entity entity = modelData.getEntity(this);
		if(entity!=null){
			return entity.getPassengersRidingOffset()/5*display.getSize();
		}
		return super.getPassengersRidingOffset();
	}
}
	