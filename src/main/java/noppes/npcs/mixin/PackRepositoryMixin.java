package noppes.npcs.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackRepository;
import noppes.npcs.CustomNpcs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @Inject(at = @At("TAIL"), method = "openAllSelected", cancellable = true)
    private void reload(CallbackInfoReturnable<List<PackResources>> ci) {
        List<PackResources> l = new ArrayList<>(ci.getReturnValue());
        l.add(new PathPackResources("cnpcs", CustomNpcs.Dir.toPath(), false));
        ci.setReturnValue(ImmutableList.copyOf(l));
    }
}
