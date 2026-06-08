package noppes.npcs.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.ModelData;
import noppes.npcs.api.constants.AnimationType;

public class EntityNPCGolem extends EntityNPCInterface
{

    public EntityNPCGolem(EntityType<? extends EntityNPCInterface> type, Level world) {
        super(type, world);
		display.setSkinTexture("customnpcs:textures/entity/golem/irongolem.png");
		this.baseSize = new EntityDimensions(1.4f, 2.5f, false);
    }

    @Override
	public EntityDimensions getDimensions(Pose pos) {
		currentAnimation = entityData.get(Animation);
		if(currentAnimation == AnimationType.SLEEP){
			return new EntityDimensions(0.5f, 0.5f, false);
		}
		else if (currentAnimation == AnimationType.SIT){
			return new EntityDimensions(1.4f, 2f, false);
		}
		else{
			return new EntityDimensions(1.4f, 2.5f, false);
		}
	}
    
    @Override
    public void tick(){
    	discard();
    	setNoAi(true);

    	if(!level().isClientSide){
	    	CompoundTag compound = new CompoundTag();
	    	
	    	addAdditionalSaveData(compound);
	    	EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, level());
	    	npc.readAdditionalSaveData(compound);
	    	ModelData data = npc.modelData;
			data.setEntity(BuiltInRegistries.ENTITY_TYPE.getKey(CustomEntities.entityNPCGolem));

			level().addFreshEntity(npc);
    	}
        super.tick();
    }
}