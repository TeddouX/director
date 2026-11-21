package teddy.director.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import teddy.director.client.DirectorState;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.screen.DirectorScreen;
import teddy.director.client.screen.Freecam;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique private boolean director$shouldOverrideRenderTarget = false;

    @Shadow @Final private RenderTarget mainRenderTarget;
    @Shadow @Final private GlobalSettingsUniform globalSettingsUniform;
    @Shadow @Final private GameRenderState gameRenderState;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Lightmap lightmap;
    @Shadow @Final private FogRenderer fogRenderer;
    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Shadow public abstract void renderLevel(DeltaTracker deltaTracker);
    @Shadow public abstract void extract(DeltaTracker deltaTracker, boolean advanceGameTime);
    @Shadow public abstract void update(DeltaTracker deltaTracker);
    @Shadow public abstract void resize(int width, int height);


    @Shadow
    public abstract RenderTarget mainRenderTarget();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void director$render(final DeltaTracker deltaTracker, final boolean advanceGameTime, CallbackInfo ci) {
        if (!DirectorScreen.active)
            return;

        RenderCamera renderCam = DirectorState.renderCamera;
        if (renderCam != null && renderCam.active) {
            renderCam.updateMcCam(minecraft);

            director$shouldOverrideRenderTarget = true;
            DirectorState.freezeCulling = true;

            try {
                director$renderLevelShort(deltaTracker);
            } finally {
                director$shouldOverrideRenderTarget = false;
                DirectorState.freezeCulling = true;
            }
        }

        Freecam.update(minecraft);
        director$renderLevelShort(deltaTracker);

        long deltaMs = Math.round(deltaTracker.getRealtimeDeltaTicks() * 50f);
        ScenePlaybackController.frame(deltaMs);

        ci.cancel();
    }

    @Inject(method = "mainRenderTarget", at = @At("HEAD"), cancellable = true)
    private void director$overrideRenderTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (!director$shouldOverrideRenderTarget) {
            cir.setReturnValue(this.mainRenderTarget);
            return;
        }

        RenderCamera renderCam = DirectorState.renderCamera;
        if (renderCam != null)
            cir.setReturnValue(renderCam.getRenderTarget());
    }

    @Unique
    private void director$renderLevelShort(final DeltaTracker deltaTracker) {
        update(deltaTracker);
        extract(deltaTracker, true);

        WindowRenderState windowRenderState = gameRenderState.windowRenderState;
        if (windowRenderState.width != mainRenderTarget.width || windowRenderState.height != mainRenderTarget.height) {
            resize(windowRenderState.width, windowRenderState.height);
        }

        RenderSystem.getDevice()
                .createCommandEncoder()
                .clearColorAndDepthTextures(
                        mainRenderTarget().getColorTexture(),
                        gameRenderState.guiRenderState.clearColorOverride,
                        mainRenderTarget().getDepthTexture(),
                        0.0
                );

        globalSettingsUniform
                .update(
                        windowRenderState.width,
                        windowRenderState.height,
                        gameRenderState.optionsRenderState.glintStrength,
                        minecraft.level == null ? 0L : minecraft.level.getGameTime(),
                        deltaTracker,
                        gameRenderState.optionsRenderState.menuBackgroundBlurriness,
                        gameRenderState.levelRenderState.cameraRenderState.pos,
                        gameRenderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS
                );

        if (minecraft.level != null) {
            lightmap.render(gameRenderState.lightmapRenderState);
            renderLevel(deltaTracker);
        }

        fogRenderer.endFrame();
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(mainRenderTarget().getDepthTexture(), 0.0);
        renderBuffers.endFrame();
        resourcePool.endFrame();
        minecraft.levelRenderer.cloudRenderer().endFrame();
    }
}
