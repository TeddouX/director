package teddy.director.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import teddy.director.client.access.CameraAccess;
import teddy.director.client.entity.rendering.DummyEntityAnimationState;
import teddy.director.client.screen.DirectorScreen;
import teddy.director.client.screen.Freecam;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraAccess {
    @Unique private boolean director$overrideTransform = false;
    @Unique private Vec3 director$pos = Vec3.ZERO;
    @Unique private float director$yaw = 0f, director$pitch = 0f;

    @Shadow protected abstract void setPosition(Vec3 position);
    @Shadow protected abstract void setRotation(final float yRot, final float xRot);

    @WrapOperation(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V"
            )
    )
    private void director$setPosAndRot(Camera self, float partialTicks, Operation<Void> original) {
        if (director$overrideTransform) {
            setRotation(director$yaw, director$pitch);
            setPosition(director$pos);

            director$overrideTransform = false;
        }
        else {
            original.call(self, partialTicks);
        }
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"
            )
    )
    private boolean director$disableSmartCull(LocalPlayer instance, Operation<Boolean> original) {
        return DirectorScreen.active || original.call(instance);
    }

    @Override
    public void director$setRotation(float yaw, float pitch) {
        director$yaw = yaw;
        director$pitch = pitch;
    }

    @Override
    public void director$setPosition(Vec3 pos) {
        director$pos = pos;
    }

    @Override
    public void director$overrideTransform() {
        director$overrideTransform = true;
    }
}
