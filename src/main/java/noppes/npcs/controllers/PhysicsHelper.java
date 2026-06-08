package noppes.npcs.controllers;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Set;

public class PhysicsHelper {
    public static boolean Enabled = FabricLoader.getInstance().isModLoaded("physicsmod");

    public static void resetEntityPhysics(Level level, int id){
        try{
            Class physModClass = Class.forName("net.diebuddies.physics.PhysicsMod");
            Object modInstance = physModClass.getMethod("getInstance", Level.class).invoke(null, level);
            Set<Integer> blockified = (Set<Integer>) physModClass.getField("alreadyBlockified").get(modInstance);
            blockified.remove(id);
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
    }
}
