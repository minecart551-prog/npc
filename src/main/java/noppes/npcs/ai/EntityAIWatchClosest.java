package noppes.npcs.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.EnumSet;

public class EntityAIWatchClosest extends Goal{
    private EntityNPCInterface npc;

    /** The closest entity which is being watched by this one. */
    protected Entity closestEntity;
    private float maxDistance;
    private int lookTime;
    private float change;
    private Class<? extends LivingEntity> watchedClass;
    protected final TargetingConditions predicate;

    public EntityAIWatchClosest(EntityNPCInterface par1EntityLiving, Class<? extends LivingEntity> limbSwingAmountClass, float par3){
        this.npc = par1EntityLiving;
        this.watchedClass = limbSwingAmountClass;
        this.maxDistance = par3;
        this.change = 0.002F;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        this.predicate = (TargetingConditions.forNonCombat()).range((double)par3);
    }

    @Override
    public boolean canUse(){
        if (this.npc.getRandom().nextFloat() >= this.change || npc.isInteracting()){
            return false;
        }
        
        if (this.npc.getTarget() != null){
            this.closestEntity = this.npc.getTarget();
        }

        if (this.watchedClass == Player.class){
            this.closestEntity = this.npc.level().getNearestPlayer(this.npc, (double)this.maxDistance);
        }
        else{
            this.closestEntity = this.npc.level().getNearestEntity(this.watchedClass, predicate, this.npc, this.npc.getX(), this.npc.getEyeY(), this.npc.getZ(), this.npc.getBoundingBox().inflate((double)this.maxDistance, 3.0D, (double)this.maxDistance));
            if (this.closestEntity != null){
            	return this.npc.canNpcSee(this.closestEntity);
            }
        }

        return this.closestEntity != null;
        
    }

    @Override
    public boolean canContinueToUse(){
    	if(npc.isInteracting() || npc.isAttacking() || !this.closestEntity.isAlive() || !npc.isAlive())
    		return false;
        return !this.npc.isInRange(this.closestEntity, maxDistance)? false : this.lookTime > 0;
    }

    @Override
    public void start(){
        this.lookTime = 60 + this.npc.getRandom().nextInt(60);
    }

    @Override
    public void stop(){
        this.closestEntity = null;
    }

    @Override
    public void tick(){
        this.npc.getLookControl().setLookAt(this.closestEntity.getX(), this.closestEntity.getY() + (double)this.closestEntity.getEyeHeight(), this.closestEntity.getZ(), 10.0F, (float)this.npc.getMaxHeadXRot());
        --this.lookTime;
    }
}
