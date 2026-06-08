package noppes.npcs.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class EntityAIPanic extends Goal
{
    private PathfinderMob entityCreature;
    private float speed;
    private double randPosX;
    private double randPosY;
    private double randPosZ;

    public EntityAIPanic(PathfinderMob par1Mob, float limbSwingAmount)
    {
        this.entityCreature = par1Mob;
        this.speed = limbSwingAmount;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse(){
        if (this.entityCreature.getTarget() == null && !this.entityCreature.isOnFire()){
            return false;
        }
        else
        {
            Vec3 var1 = DefaultRandomPos.getPos(this.entityCreature, 5, 4);

            if (var1 == null){
                return false;
            }
            else{
				this.randPosX = var1.x;
                this.randPosY = var1.y;
                this.randPosZ = var1.z;
                return true;
            }
        }
    }

    @Override
    public void start(){
        this.entityCreature.getNavigation().moveTo(this.randPosX, this.randPosY, this.randPosZ, this.speed);
    }

    @Override
    public boolean canContinueToUse(){
    	if(this.entityCreature.getTarget() == null)
    		return false;
        return !this.entityCreature.getNavigation().isDone();
    }
}