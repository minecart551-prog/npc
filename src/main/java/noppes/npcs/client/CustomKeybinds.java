package noppes.npcs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import noppes.npcs.CustomNpcs;

@Environment(EnvType.CLIENT)
public class CustomKeybinds {

    @Environment(EnvType.CLIENT)
    public static void registerKeys() {
        ClientProxy.QuestLog = new KeyMapping("Quest Log", 76, "key.categories.gameplay");

        if(CustomNpcs.SceneButtonsEnabled){
            ClientProxy.Scene1 = new KeyMapping("Scene1 start/pause", 321, "key.categories.gameplay");
            ClientProxy.Scene2 = new KeyMapping("Scene2 start/pause", 322, "key.categories.gameplay");
            ClientProxy.Scene3 = new KeyMapping("Scene3 start/pause", 323, "key.categories.gameplay");
            ClientProxy.SceneReset = new KeyMapping("Scene reset", 320, "key.categories.gameplay");

            KeyBindingHelper.registerKeyBinding(ClientProxy.Scene1);
            KeyBindingHelper.registerKeyBinding(ClientProxy.Scene2);
            KeyBindingHelper.registerKeyBinding(ClientProxy.Scene3);
            KeyBindingHelper.registerKeyBinding(ClientProxy.SceneReset);
        }

        KeyBindingHelper.registerKeyBinding(ClientProxy.QuestLog);
    }
}
