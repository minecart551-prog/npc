package noppes.npcs.shared;

import noppes.npcs.CustomNpcs;

import java.io.File;

public class SharedReferences {

    public static String modid(){
        return CustomNpcs.MODID;
    }

    public static File dir(){
        return CustomNpcs.Dir;
    }

    public static boolean AllowFullyInvisibleSkins(){
        return true;
    }

    public static boolean VerboseDebug() { return CustomNpcs.VerboseDebug; }
}
