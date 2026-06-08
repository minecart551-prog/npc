package noppes.npcs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class EntityNPCFlying extends EntityNPCInterface{

	public EntityNPCFlying(EntityType<? extends PathfinderMob> type, Level world) {
		super(type, world);
	}

	@Override
	public boolean canFly(){
		return ais.movementType == 1;
	}
	
    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
    	if(!canFly())
    		return super.causeFallDamage(distance, damageMultiplier,source);
    	return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        if (!canFly()) {
            super.checkFallDamage(y, onGroundIn, state, pos);
        }
    }

    @Override
    public void travel(Vec3 v){
    	if(!canFly()){
    		super.travel(v);
    		return;
    	}
    	Vec3 m = getDeltaMovement();
    	if (!this.isInWater() && ais.movementType == 2){
    	    m = new Vec3(0, -0.15, 0);
            this.move(MoverType.SELF, m);
        }else if (this.isInWater() && ais.movementType == 1){
            this.moveRelative(0.02F, v);
            this.move(MoverType.SELF, m);
            m = getDeltaMovement().scale(0.8);
        }
        else if (this.isInLava()){
            this.moveRelative(0.02F, v);
            this.move(MoverType.SELF, m);
            m = getDeltaMovement().scale(0.5);
        }
        else{
            BlockPos ground = new BlockPos((int) this.getX(), (int) (this.getY() - 1.0D), (int) this.getZ());
            float f = 0.91F;
            if (this.onGround()) {
                f = 0.6F * 0.91F;//this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
            }

            float f1 = 0.16277137F / (f * f * f);
            f = 0.91F;
            if (this.onGround()) {
                f = 0.6F * 0.91F;//this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
            }

            this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, v);
            this.move(MoverType.SELF, this.getDeltaMovement());
            m = this.getDeltaMovement().scale((double)f);
        }
        setDeltaMovement(m);


        this.calculateEntityAnimation(false);
    }

    @Override
    public boolean onClimbable(){
        return false;
    }
}
