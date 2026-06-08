package noppes.npcs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModelShaper.class)
public class ItemModelMesherMixin {

    @Inject(at = @At("HEAD"), method = "getItemModel*", cancellable = true)
    public void getModel(ItemStack item, CallbackInfoReturnable<BakedModel> cir){
        if(item.getItem() == CustomItems.scripted_item){
            ItemScriptedWrapper si = (ItemScriptedWrapper) WrapperNpcAPI.Instance().getIItemStack(item);
            if(si == null)
                return;
            Item i = null;
            if(si.texture != null){
                i = BuiltInRegistries.ITEM.get(si.texture);
            }
            if(i == null){
                i = CustomItems.scripted_item;
            }
            BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(i);
            if(model != null){
                cir.setReturnValue(model);
                cir.cancel();
            }
        }
    }
}
