package teddy.director.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teddy.director.client.screen.DirectorScreen;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void director$hidePlayerArm(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            float inverseArmHeight,
            float attackValue,
            HumanoidArm arm,
            CallbackInfo ci
    ) {
        if (DirectorScreen.active)
            ci.cancel();
    }
}
