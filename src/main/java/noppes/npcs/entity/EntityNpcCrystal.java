package noppes.npcs.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.ModelData;


public class EntityNpcCrystal extends EntityNPCInterface {

    public EntityNpcCrystal(EntityType<? extends EntityNPCInterface> type, Level world) {
        super(type, world);
		scaleX = 0.7f;
		scaleY = 0.7f;
		scaleZ = 0.7f;
		display.setSkinTexture("customnpcs:textures/entity/crystal/endercrystal.png");
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
			data.setEntity(BuiltInRegistries.ENTITY_TYPE.getKey(CustomEntities.entityNpcCrystal));


			level().addFreshEntity(npc);
    	}
        super.tick();
    }

}
