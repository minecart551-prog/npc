package noppes.npcs.api.wrapper;

import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;

public class DamageSourceWrapper implements IDamageSource {
	private DamageSource source;

	public DamageSourceWrapper(DamageSource source){
		this.source = source;
	}
	
	@Override
	public String getType() {
		return source.getMsgId();
	}

	@Override
	public boolean isUnblockable() {
		return source.is(DamageTypeTags.BYPASSES_ARMOR);
	}

	@Override
	public boolean isProjectile() {
		return source.is(DamageTypeTags.IS_PROJECTILE);
	}

	@Override
	public DamageSource getMCDamageSource() {
		return source;
	}

	@Override
	public IEntity getTrueSource() {
		return NpcAPI.Instance().getIEntity(source.getEntity());
	}

	@Override
	public IEntity getImmediateSource() {
		return NpcAPI.Instance().getIEntity(source.getDirectEntity());
	}

}
