package noppes.npcs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class EntityAIMoveIndoors extends Goal
{
    private PathfinderMob theCreature;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private Level level;

    public EntityAIMoveIndoors(PathfinderMob par1Mob)
    {
        this.theCreature = par1Mob;
        this.level = par1Mob.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse(){
    	if(this.theCreature.level().isDay() && !this.theCreature.level().isRaining() || theCreature.level().dimensionType().hasSkyLight())
    		return false;
    	BlockPos pos =  new BlockPos((int) theCreature.getX(), (int) this.theCreature.getBoundingBox().minY, (int) theCreature.getZ());

		if (!this.level.canSeeSky(pos) && this.level.getLightEmission(pos) > 8){
            return false;
        }

        Vec3 var1 = this.findPossibleShelter();
        if (var1 == null){
            return false;
        }
        this.shelterX = var1.x;
        this.shelterY = var1.y;
        this.shelterZ = var1.z;
        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !this.theCreature.getNavigation().isDone();
    }

    @Override
    public void start()
    {
        this.theCreature.getNavigation().moveTo(this.shelterX, this.shelterY, this.shelterZ, 1.0D);
    }

    private Vec3 findPossibleShelter(){
        RandomSource random = this.theCreature.getRandom();
        BlockPos blockpos = new BlockPos((int) this.theCreature.getX(), (int) this.theCreature.getBoundingBox().minY, (int) this.theCreature.getZ());

        for (int i = 0; i < 10; ++i){
            BlockPos blockpos1 = blockpos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);

            if (!this.level.canSeeSky(blockpos1) && this.theCreature.getWalkTargetValue(blockpos1) < 0.0F)
            {
                return new Vec3((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
            }
        }

        return null;
    }
}
