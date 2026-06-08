package noppes.npcs;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public class NpcDamageSource {

	public static final ResourceKey<DamageType> NPC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(CustomNpcs.MODID, "npc"));
}
