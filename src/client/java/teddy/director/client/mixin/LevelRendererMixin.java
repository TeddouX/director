package teddy.director.client.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teddy.director.client.DirectorState;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void director$skipReposition(CameraRenderState camera, CallbackInfo ci) {
        if (DirectorState.freezeCulling) ci.cancel();
    }
}
