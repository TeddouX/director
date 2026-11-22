package teddy.director.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teddy.director.client.DirectorState;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void director$skipReposition(CameraRenderState camera, CallbackInfo ci) {
        if (DirectorState.freezeCulling) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void director$captureRenderCameraFogColor(
            GraphicsResourceAllocator resourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderOutline,
            CameraRenderState cameraState,
            Matrix4fc modelViewMatrix,
            GpuBufferSlice terrainFog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            CallbackInfo ci
    ) {
        if (DirectorState.renderingUsingRenderCam)
            DirectorState.renderCamera.fogColor = fogColor;
    }
}
