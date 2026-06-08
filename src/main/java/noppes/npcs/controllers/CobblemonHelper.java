package noppes.npcs.controllers;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CobblemonHelper {
    public static boolean Enabled = FabricLoader.getInstance().isModLoaded("cobblemon");

    public static boolean isPokemon(Entity entity) {
        if (entity == null) return false;
        ResourceLocation typeResLoc = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return typeResLoc.equals(new ResourceLocation("cobblemon", "pokemon"));
    }

    public static ResourceLocation getType(Entity entity) {
        if (!isPokemon(entity)) return null;
        try {
            Object pokemon = (entity).getClass().getMethod("getPokemon").invoke(entity);
            Object species = pokemon.getClass().getMethod("getSpecies").invoke(pokemon);
            //new ResourceLocation("cobblemon", "stonjourner")
            return (ResourceLocation) species.getClass().getField("resourceIdentifier").get(species);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void setType(Entity entity, ResourceLocation resourceLocation) {
        if (!isPokemon(entity)) return;
        try {
            Object pokemon = (entity).getClass().getMethod("getPokemon").invoke(entity);
            Object species = pokemon.getClass().getMethod("getSpecies").invoke(pokemon);
            //new ResourceLocation("cobblemon", "stonjourner")
            species.getClass().getField("resourceIdentifier").set(species, resourceLocation);
        } catch (Exception ignored) {

        }
    }

    public static EntityModel getPokemonModel(Entity entity){
        ResourceLocation species = getType(entity);
        EntityModel model = null;
        try {
            Object instance = Class.forName("com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository").getField("INSTANCE").get(null);
            model = (EntityModel) instance.getClass().getMethod("getPoser", ResourceLocation.class, Set.class).invoke(instance, species, new HashSet<String>());
            model.getClass().getMethod("setProfileScale", float.class).invoke(model, 1.0f);
            model.getClass().getMethod("setPortraitScale", float.class).invoke(model, 1.0f);
        } catch (Exception ignored) {

        }
        return model;
    }

    public static List<String> getTypes() {
        HashSet<String> res = new HashSet<>();
        try {
            Object instance = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies").getField("INSTANCE").get(null);
            List implementedSpecies = (List) instance.getClass().getMethod("getImplemented").invoke(instance);
            for(Object obj : implementedSpecies){
                res.add(obj.getClass().getMethod("getResourceIdentifier").invoke(obj).toString());
            }
        } catch (Exception ignored) {

        }
        return new ArrayList<>(res);
    }
}
