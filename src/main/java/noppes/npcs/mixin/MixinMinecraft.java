package noppes.npcs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.CustomRenderers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "createSearchTrees", at=@At("HEAD"))
    private void lateInit(CallbackInfo ci){
        CustomRenderers.registerEntityRenderer();

        CustomNpcResourceListener listener = new CustomNpcResourceListener();
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(listener);
        listener.onResourceManagerReload(Minecraft.getInstance().getResourceManager());

        ((MinecraftAccessor)Minecraft.getInstance()).getItemColors().register((stack, tintIndex) -> 0x8B4513, CustomItems.mount, CustomItems.cloner, CustomItems.moving, CustomItems.scripter, CustomItems.wand, CustomItems.teleporter);

        ((MinecraftAccessor)Minecraft.getInstance()).getItemColors().register((stack, tintIndex) -> {
            if (stack.getItem() == CustomItems.scripted_item) {
                IItemStack item = NpcAPI.Instance().getIItemStack(stack);
                if(!item.isEmpty()){
                    return ((IItemScripted) item).getColor();
                }
            }
            return -1;
        }, CustomItems.scripted_item);
    }

    @Inject(method = "<init>", at=@At("TAIL"))
    private void veryLateInit(GameConfig gameConfig, CallbackInfo ci){
        ClientProxy.Font = new ClientProxy.FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
    }
}
