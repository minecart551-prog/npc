package noppes.npcs;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.entity.*;

import java.util.ArrayList;
import java.util.List;

public class CustomEntities {
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNpcPony;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNpcCrystal;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNpcSlime;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNpcDragon;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNPCGolem;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityCustomNpc;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNPC64x32;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNpcAlex;
    public static net.minecraft.world.entity.EntityType<? extends EntityNPCInterface> entityNpcClassicPlayer;
    public static net.minecraft.world.entity.EntityType<?> entityChairMount;
    public static net.minecraft.world.entity.EntityType<? extends ThrowableProjectile> entityProjectile;

    private static List<EntityType<?>> types = new ArrayList<>();

    public static void registerEntities() {
            types.clear();
            entityNpcPony = registerNpc(EntityNpcPony.class, "npcpony", EntityNpcPony::new);
            entityNpcCrystal = registerNpc(EntityNpcCrystal.class, "npccrystal", EntityNpcCrystal::new);
            entityNpcSlime = registerNpc(EntityNpcSlime.class, "npcslime", EntityNpcSlime::new);
            entityNpcDragon = registerNpc(EntityNpcDragon.class, "npcdragon", EntityNpcDragon::new);
            entityNPCGolem = registerNpc(EntityNPCGolem.class, "npcgolem", EntityNPCGolem::new);
            entityCustomNpc = registerNpc(EntityCustomNpc.class, "customnpc", EntityCustomNpc::new);
            entityNPC64x32 = registerNpc(EntityNPC64x32.class, "customnpc64x32", EntityNPC64x32::new);
            entityNpcAlex = registerNpc(EntityNpcAlex.class, "customnpcalex", EntityNpcAlex::new);
            entityNpcClassicPlayer = registerNpc(EntityNpcClassicPlayer.class, "customnpcclassic", EntityNpcClassicPlayer::new);

            entityChairMount = registerNewentity(EntityChairMount.class, "customnpcchairmount", EntityChairMount::new, 64, 10, false, 0.001f, 0.001f);
            entityProjectile = registerNewentity(EntityProjectile.class, "customnpcprojectile", EntityProjectile::new, 64, 20, true, 0.5f, 0.5f);
    }

    public static void attribute(){
        for(EntityType type : types){
            FabricDefaultAttributeRegistry.register(type, EntityNPCInterface.createMobAttributes().build());
        }
    }

    private static <T extends Entity> EntityType<T> registerNpc(Class<? extends Entity> c, String name, EntityType.EntityFactory<T> factoryIn) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factoryIn, MobCategory.CREATURE);
        builder.updateInterval(3);
        builder.clientTrackingRange(10);
        builder.sized(1, 1);
        ResourceLocation registryName = new ResourceLocation(CustomNpcs.MODID, name);
        EntityType<T> type = builder.build(registryName.toString());
        types.add(type);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, registryName, type);

        if(CustomNpcs.FixUpdateFromPre_1_12) {
            registryName = new ResourceLocation("customnpcs." + name);
            Registry.register(BuiltInRegistries.ENTITY_TYPE, registryName, builder.build(registryName.toString()));
            //ForgeRegistries.ENTITIES.register(new EntityType(cl, name).setRegistryName(registryName));
        }
        return type;
    }

    private static <T extends Entity> EntityType<T> registerNewentity(Class<? extends Entity> c, String name, EntityType.EntityFactory<T> factoryIn, int range, int update, boolean velocity, float width, float height) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factoryIn, MobCategory.MISC);
        builder.updateInterval(update);
        builder.sized(width, height);
        builder.clientTrackingRange(4);
        ResourceLocation registryName = new ResourceLocation(CustomNpcs.MODID, name);
        EntityType<T> type = builder.build(registryName.toString());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, registryName, type);
        return type;
    }
}
