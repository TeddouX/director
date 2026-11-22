package teddy.director.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.access.CameraAccess;
import teddy.director.client.screen.DirectorScreen;

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
