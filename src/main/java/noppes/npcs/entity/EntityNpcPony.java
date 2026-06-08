package noppes.npcs.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.ModelData;

public class EntityNpcPony extends EntityNPCInterface
{
    public boolean isPegasus = false;
    public boolean isUnicorn = false;
    public boolean isFlying = false;
    
    public ResourceLocation checked = null;
    
    public EntityNpcPony(EntityType<? extends EntityNPCInterface> type, Level world) {
        super(type, world);
        display.setSkinTexture("customnpcs:textures/entity/ponies/minelpderpyhooves.png");
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

            data.setEntity(BuiltInRegistries.ENTITY_TYPE.getKey(CustomEntities.entityNpcPony));

            level().addFreshEntity(npc);
    	}
        super.tick();
    }
    
}
