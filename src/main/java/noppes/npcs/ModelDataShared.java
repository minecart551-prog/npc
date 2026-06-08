package noppes.npcs;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.client.parts.MpmPart;
import noppes.npcs.client.parts.MpmPartData;
import noppes.npcs.constants.BodyPart;
import noppes.npcs.constants.EnumParts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class ModelDataShared{
	public ModelPartConfig arm1 = new ModelPartConfig();
	public ModelPartConfig arm2 = new ModelPartConfig();
	public ModelPartConfig body = new ModelPartConfig();
	public ModelPartConfig leg1 = new ModelPartConfig();
	public ModelPartConfig leg2 = new ModelPartConfig();
	public ModelPartConfig head = new ModelPartConfig();

	protected ResourceLocation entityName = null;
	protected LivingEntity entity;

	public CompoundTag extra = new CompoundTag();

	public ListTag oldPartData = new ListTag();

	public List<MpmPartData> mpmParts = new ArrayList<MpmPartData>();
	public List<BodyPart> hiddenParts = new ArrayList<>();

	public int wingMode = 0; //0:do nothing, 1:hide elytra

	public String url = "";
	public String displayName = "";


	public long lastEdited = System.currentTimeMillis();
	public int inLove = 0;
	public int animationTime = -1;
	public int modelType = 0; //0:Default, 1:Steve, 2:Alex

	public int moveAnimation = AnimationType.IDLE;
	public int prevMoveAnimation = AnimationType.IDLE;
	public boolean startMoveAnimation = false;
	public int animation = AnimationType.NONE;
	public int prevAnimation = AnimationType.NONE;
	public boolean startAnimation = false;
	public int animationStart = 0;
	public float sleepRotation;

	public CompoundTag save(){
		CompoundTag compound = new CompoundTag();

		if(entityName != null)
			compound.putString("EntityName", entityName.toString());

		compound.put("ArmsConfig", arm1.writeToNBT());
		compound.put("Arms2Config", arm2.writeToNBT());
		compound.put("BodyConfig", body.writeToNBT());
		compound.put("LegsConfig", leg1.writeToNBT());
		compound.put("Legs2Config", leg2.writeToNBT());
		compound.put("HeadConfig", head.writeToNBT());

		compound.put("ExtraData", extra);
		compound.putInt("WingMode", wingMode);

		compound.putString("CustomSkinUrl", url);
		compound.putString("DisplayName", displayName);

		compound.putInt("Animation", animation);
		compound.putInt("MoveAnimation", moveAnimation);
		compound.putInt("ModelType", modelType);
		compound.putLong("LastEdited", lastEdited);

		compound.put("Parts", oldPartData);

		ListTag list = new ListTag();
		for(MpmPartData e : mpmParts){
			list.add(e.getNbt());
		}
		compound.put("NewParts", list);

		return compound;
	}

	public void load(CompoundTag compound){
		String rl = compound.getString("EntityName");
		setEntity(rl.isEmpty()? null : new ResourceLocation(rl));

		arm1.readFromNBT(compound.getCompound("ArmsConfig"));
		arm2.readFromNBT(compound.getCompound("Arms2Config"));
		body.readFromNBT(compound.getCompound("BodyConfig"));
		leg1.readFromNBT(compound.getCompound("LegsConfig"));
		leg2.readFromNBT(compound.getCompound("Legs2Config"));
		head.readFromNBT(compound.getCompound("HeadConfig"));

		extra = compound.getCompound("ExtraData");
		wingMode = compound.getInt("WingMode");

		url = compound.getString("CustomSkinUrl");
		displayName = compound.getString("DisplayName");

		animation = compound.getInt("Animation");
		moveAnimation = compound.getInt("MoveAnimation");
		modelType = compound.getInt("ModelType");
		lastEdited = compound.getLong("LastEdited");

		List<MpmPartData> mpmParts = new ArrayList<MpmPartData>();
		ListTag list = compound.getList("NewParts", 10);
		for (int i = 0; i < list.size(); i++) {
			MpmPartData part = new MpmPartData();
			part.setNbt(list.getCompound(i));
			if(part.partId.equals(ModelEyeData.RESOURCE) || part.partId.equals(ModelEyeData.RESOURCE_RIGHT) || part.partId.equals(ModelEyeData.RESOURCE_LEFT)){
				part = new ModelEyeData();
				part.setNbt(list.getCompound(i));
			}
			mpmParts.add(part);
		}
		this.mpmParts = mpmParts;

		oldPartData = compound.getList("Parts", 10);
		if(this.mpmParts.isEmpty()){
			for (int i = 0; i < list.size(); i++) {
				this.mpmParts.add(EnumParts.convertOldPart(list.getCompound(i)));
			}
		}
		refreshParts();
		updateTransate();
	}

	public void setMoveAnimation(int ani) {
		startMoveAnimation = moveAnimation != ani;
		moveAnimation = ani;
	}

	public int getMoveAnimtion(LivingEntity player){
		if(player.isPassenger()){
			return AnimationType.SIT;
		}
		if(player.isSleeping()){
			return AnimationType.SLEEP;
		}

		if(moveAnimation == AnimationType.IDLE && player.isCrouching()){
			return AnimationType.CROUCH;
		}
		return moveAnimation;
	}

	public boolean isMovementAnimation(int ani){
		return ani == AnimationType.SLEEP || ani == AnimationType.CRAWL || ani == AnimationType.CROUCH || ani == AnimationType.SIT || ani == AnimationType.DEATH
				|| ani == AnimationType.WALK || ani == AnimationType.IDLE || ani == AnimationType.FLY_IDLE || ani == AnimationType.FLY;
	}

	public void setAnimation(int ani) {
		if(isMovementAnimation(ani)){
			setMoveAnimation(ani);
			return;
		}
		animationTime = -1;
		animation = ani;
		lastEdited = System.currentTimeMillis();
		startAnimation = animation != ani;

		if(animation == AnimationType.WAVE)
			animationTime = 80;

		if(animation == AnimationType.YES || animation == AnimationType.NO)
			animationTime = 60;

		if(getOwner() == null || ani == AnimationType.NONE)
			animationStart = -1;
		else
			animationStart = getOwner().tickCount;
	}

	public void  updateTransate(){
		for(EnumParts part : EnumParts.values()){
			ModelPartConfig config = getPartConfig(part);
			if(config == null)
				continue;
			if(part == EnumParts.HEAD){
				config.setTranslate(0, getBodyY(), 0);
			}
			else if(part == EnumParts.ARM_LEFT){
				ModelPartConfig body = getPartConfig(EnumParts.BODY);
				float x = (1 - body.scaleX) * 0.25f + (1 - config.scaleX) * 0.0625f;
				float y = getBodyY() + (1 - config.scaleY) * -0.125f;
				config.setTranslate(-x, y, 0);
				if(!config.notShared){
					ModelPartConfig arm = getPartConfig(EnumParts.ARM_RIGHT);
					arm.copyValues(config);
				}
			}
			else if(part == EnumParts.ARM_RIGHT){
				ModelPartConfig body = getPartConfig(EnumParts.BODY);
				float x = (1 - body.scaleX) * 0.25f + (1 - config.scaleX) * 0.0625f;
				float y = getBodyY() + (1 - config.scaleY) * -0.125f;
				config.setTranslate(x, y, 0);
			}
			else if(part == EnumParts.LEG_LEFT){
				config.setTranslate(-(1 - config.scaleX) * 0.118f , getLegsY(),  -(1 - config.scaleZ) * 0.00625f);
				if(!config.notShared){
					ModelPartConfig leg = getPartConfig(EnumParts.LEG_RIGHT);
					leg.copyValues(config);
				}
			}
			else if(part == EnumParts.LEG_RIGHT){
				config.setTranslate((1 - config.scaleX) * 0.118f, getLegsY(),  -(1 - config.scaleZ) * 0.00625f);
			}
			else if(part == EnumParts.BODY){
				config.setTranslate(0, getBodyY(), 0);
			}
		}
	}

	public void setEntity(ResourceLocation resourceLocation){
		this.entityName = resourceLocation;
		clearEntity();
		extra = new CompoundTag();
	}

	public ResourceLocation getEntityName(){
		return entityName;
	}

	public boolean hasEntity(){
		return entityName != null;
	}

	public float offsetY() {
		if(entity == null)
			return -getBodyY();
		return entity.getBbHeight() - 1.8f;
	}

	public void clearEntity() {
		entity = null;
	}

	public ModelPartConfig getPartConfig(EnumParts type){
		if(type == EnumParts.BODY)
			return body;
		if(type == EnumParts.ARM_LEFT)
			return arm1;
		if(type == EnumParts.ARM_RIGHT)
			return arm2;
		if(type == EnumParts.LEG_LEFT)
			return leg1;
		if(type == EnumParts.LEG_RIGHT)
			return leg2;

		return head;
	}

	public abstract LivingEntity getOwner();

	public float getBodyY(){
		if(entity != null){
			return entity.getBbHeight();
		}
//		if(legParts.type == 3)
//			return (0.9f - body.scaleY) * 0.75f + getLegsY();
//		if(legParts.type == 3)
//			return (0.5f - body.scaleY) * 0.75f + getLegsY();
		return (1 - body.scaleY) * 0.75f + getLegsY();
	}

	public float getLegsY() {
		ModelPartConfig legs = leg1;
		if(leg1.notShared && leg2.scaleY > leg1.scaleY)
			legs = leg2;
//		if(legParts.type == 3)
//			return (0.87f - legs.scaleY) * 1f;
		return (1 - legs.scaleY) * 0.75f;
	}


	public void refreshParts(){
		this.hiddenParts = mpmParts.stream().flatMap(part -> {
			MpmPart p = part.getPart();
			if(p != null){
				return p.hiddenParts.stream();
			}
			return Stream.empty();
		}).distinct().collect(Collectors.toList());
	}
}
