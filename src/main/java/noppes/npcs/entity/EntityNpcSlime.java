package noppes.npcs.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.ModelData;

public class EntityNpcSlime extends EntityNPCInterface{
	public EntityNpcSlime(EntityType<? extends EntityNPCInterface> type, Level world) {
        super(type, world);
		scaleX = 2f;
		scaleY = 2f;
		scaleZ = 2f;
		display.setSkinTexture("customnpcs:textures/entity/slime/slime.png");
		this.baseSize = new EntityDimensions(0.8f, 0.8f, false);
	}    
	
	@Override
	public EntityDimensions getDimensions(Pose pos) {
		return new EntityDimensions(0.8f, 0.8f, false);
	}
	
	@Override
    public void tick() {
    	discard();
    	setNoAi(true);

    	if(!level().isClientSide){
	    	CompoundTag compound = new CompoundTag();
	    	
	    	addAdditionalSaveData(compound);
	    	EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, level());
	    	npc.readAdditionalSaveData(compound);
	    	ModelData data = npc.modelData;
			data.setEntity(BuiltInRegistries.ENTITY_TYPE.getKey(CustomEntities.entityNpcSlime));

			level().addFreshEntity(npc);
    	}
        super.tick();
    }
}
