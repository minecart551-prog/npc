package noppes.npcs.ai.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.EnumSet;

public class EntityAIOwnerHurtByTarget extends TargetGoal {
    EntityNPCInterface npc;
    LivingEntity theOwnerAttacker;
    private int timer;

    public EntityAIOwnerHurtByTarget(EntityNPCInterface npc){
        super(npc, false);
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse(){
        if (!this.npc.isFollower() || !npc.role.defendOwner()){
            return false;
        }
        else{
            LivingEntity entitylivingbase = this.npc.getOwner();

            if (entitylivingbase == null){
                return false;
            }
            else{
                this.theOwnerAttacker = entitylivingbase.getLastHurtByMob();
                int i = entitylivingbase.getLastHurtByMobTimestamp();
                return i != this.timer && this.canAttack(this.theOwnerAttacker, TargetingConditions.DEFAULT);
            }
        }
    }

    @Override
    public void start(){
        this.npc.setTarget(this.theOwnerAttacker);
        LivingEntity entitylivingbase = this.npc.getOwner();

        if (entitylivingbase != null){
            this.timer = entitylivingbase.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}