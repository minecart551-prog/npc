package noppes.npcs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public class Resistances {

	public float knockback = 1f;
	public float arrow = 1f;
	public float melee = 1f;
	public float explosion = 1f;
	
	public CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		compound.putFloat("Knockback", knockback);
		compound.putFloat("Arrow", arrow);
		compound.putFloat("Melee", melee);
		compound.putFloat("Explosion", explosion);
		return compound;
	}

	public void readToNBT(CompoundTag compound) {
		knockback = compound.getFloat("Knockback");
		arrow = compound.getFloat("Arrow");
		melee = compound.getFloat("Melee");
		explosion = compound.getFloat("Explosion");
	}

	public float applyResistance(DamageSource source, float damage) {
		if(source.getMsgId().equals("arrow") || source.getMsgId().equals("thrown") || source.is(DamageTypeTags.IS_PROJECTILE)){
			damage *= (2 - arrow);
		}
		else if(source.getMsgId().equals("player") || source.getMsgId().equals("mob") || source.getMsgId().equals("npc")){
			damage *= (2 - melee);
		}
		else if(source.getMsgId().equals("explosion") || source.getMsgId().equals("explosion.player")){
			damage *= (2 - explosion);
		}
		
		return damage;
	}

}
