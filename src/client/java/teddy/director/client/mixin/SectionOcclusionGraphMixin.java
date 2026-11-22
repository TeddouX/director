package teddy.director.client.mixin;

import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teddy.director.client.DirectorState;

@Mixin(SectionOcclusionGraph.class)
public abstract class SectionOcclusionGraphMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void director$skipUpdate(CallbackInfo ci) {
        if (DirectorState.freezeCulling) ci.cancel();
    }
}
