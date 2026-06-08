package noppes.npcs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.controllers.CobblemonHelper;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.common.util.LogWriter;

import java.lang.reflect.Method;

public class ModelData extends ModelDataShared{

	public boolean simpleRender = false;

	public EntityCustomNpc npc;

	public ModelData(EntityCustomNpc npc) {
		this.npc = npc;
	}

	public LivingEntity getEntity(EntityNPCInterface npc){
		if(!hasEntity())
			return null;
		if(entity == null){
			try {
				entity = (LivingEntity) BuiltInRegistries.ENTITY_TYPE.get(getEntityName()).create(npc.level());
				CompoundTag comp = new CompoundTag();
				entity.addAdditionalSaveData(comp);
				if(PixelmonHelper.isPixelmon(entity) && !extra.contains("Name")){
					extra.putString("Name", "abra");
				}
				comp = comp.merge(extra);
				try {
					entity.readAdditionalSaveData(comp);
					if(PixelmonHelper.isPixelmon(entity)){
						PixelmonHelper.initEntity(entity, extra.getString("Name"));
					}
					if(CobblemonHelper.isPokemon(entity)){
						CobblemonHelper.setType(entity, new ResourceLocation(extra.getString("CobblemonModel")));
					}
				}
				catch (Exception e) {
					LogWriter.except(e);
				} 

				entity.setInvulnerable(true);
				entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(npc.getMaxHealth());
				for(EquipmentSlot slot : EquipmentSlot.values()){
					entity.setItemSlot(slot, npc.getItemBySlot(slot));
				}
			} catch (Exception e) {
				LogWriter.except(e);
			}
		}
		return entity;
	}
	
	public ModelData copy(){
		ModelData data = new ModelData(npc);
		data.load(this.save());
		return data;
	}

	@Override
	public CompoundTag save() {
		CompoundTag compound = super.save();
		compound.putBoolean("SimpleRender", simpleRender);
		return compound;
	}

	@Override
	public void load(CompoundTag compound){
		super.load(compound);
		simpleRender = compound.getBoolean("SimpleRender");
	}
	
	public void setExtra(LivingEntity entity, String key, String value){
		key = key.toLowerCase();

//		if(key.equals("breed") && entity.getEncodeId().equals("tgvstyle.Dog")){
//			try {
//				Method method = entity.getClass().getMethod("getBreedID");
//				Enum breed = (Enum) method.invoke(entity);
//				method = entity.getClass().getMethod("setBreedID", breed.getClass());
//				method.invoke(entity, breed.getClass().getEnumConstants()[Integer.parseInt(value)]);
//				CompoundTag comp = new CompoundTag();
//				entity.readAdditionalSaveData(comp);
//				extra.putString("EntityData21", comp.getString("EntityData21"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
    	if(key.equalsIgnoreCase("name") && PixelmonHelper.isPixelmon(entity)){
    		extra.putString("Name", value);
    	}
		if(key.equalsIgnoreCase("cobblemonmodel") && CobblemonHelper.isPokemon(entity)){
			extra.putString("CobblemonModel", value);
		}
		clearEntity();
	}

	@Override
	public LivingEntity getOwner() {
		return npc;
	}

	public static ModelData get(EntityCustomNpc npc){
		return npc.modelData;
	}
}
