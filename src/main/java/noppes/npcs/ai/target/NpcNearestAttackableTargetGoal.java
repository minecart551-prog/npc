package noppes.npcs.ai.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.scores.Team;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.function.Predicate;

public class NpcNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private int unseenTicks1;

    public NpcNearestAttackableTargetGoal(EntityNPCInterface npc, Class<T> c, int range, boolean b, boolean b2, Predicate<LivingEntity> selector) {
        super(npc, c, range, b, b2, selector);
        if(npc.ais.attackInvisible){
            this.targetConditions.ignoreInvisibilityTesting();
        }
        if(!npc.ais.directLOS)
            this.targetConditions.ignoreLineOfSight();
    }

    public void start() {
        unseenTicks1 = 0;
        this.mob.setTarget(this.target);
        super.start();
    }

    public void stop() {
        this.mob.setTarget(null);
        this.targetMob = null;
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            livingentity = this.targetMob;
        }

        if (livingentity == null) {
            return false;
        } else if (!this.mob.canAttack(livingentity)) {
            return false;
        } else {
            Team team = this.mob.getTeam();
            Team team1 = livingentity.getTeam();
            if (team != null && team1 == team) {
                return false;
            } else {
                double d0 = this.getFollowDistance();
                if (this.mob.distanceToSqr(livingentity) > d0 * d0) {
                    return false;
                } else {
                    if (this.mustSee) {
                        if (this.mob.getSensing().hasLineOfSight(livingentity)) {
                            this.unseenTicks1 = 0;
                        } else if (++this.unseenTicks1 > reducedTickDelay(this.unseenMemoryTicks)) {
                            return false;
                        }
                    }

                    this.mob.setTarget(livingentity);
                    return true;
                }
            }
        }
    }
}
