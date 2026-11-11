package teddy.director.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import teddy.director.Director;
import teddy.director.client.screen.DirectorScreen;
import teddy.director.client.screen.Freecam;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(Vec3 position);
    @Shadow protected abstract void setRotation(final float yRot, final float xRot);

    @WrapOperation(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V"
            )
    )
    private void freecam$setPosAndRot(Camera self, float partialTicks, Operation<Void> original) {
        if (DirectorScreen.active) {
            setRotation(Freecam.yaw, Freecam.pitch);
            setPosition(Freecam.pos);
        } else {
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
    private boolean freecam$disableSmartCull(LocalPlayer instance, Operation<Boolean> original) {
        return DirectorScreen.active || original.call(instance);
    }
}
