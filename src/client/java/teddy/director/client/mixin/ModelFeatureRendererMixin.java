package teddy.director.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teddy.director.client.access.LivingEntityStateAccess;
import teddy.director.client.entity.rendering.DummyEntityAnimationState;
import teddy.director.client.entity.rendering.ModelPartLookup;

import java.util.Map;

@Mixin(ModelFeatureRenderer.class)
public abstract class ModelFeatureRendererMixin<S> {
    @WrapOperation(
            method = "prepareModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V"
            )
    )
    private void director$applyTransforms(Model<?> model, S state, Operation<Void> original) {
        original.call(model, state);

        if (!(state instanceof LivingEntityStateAccess access))
            return;

        DummyEntityAnimationState animState = access.director$getAnimState();
        if (animState == null || animState.partTransforms == null)
            return;

        Map<String, ModelPart> partLookup = ModelPartLookup.get(model);
        animState.partTransforms.forEach((path, transform) -> {
            ModelPart part = partLookup.get(path);
            if (part == null) return;

            part.x += transform.dx();
            part.y += transform.dy();
            part.z += transform.dz();

            part.xRot += (float) Math.toRadians(transform.rx());
            part.yRot += (float) Math.toRadians(transform.ry());
            part.zRot += (float) Math.toRadians(transform.rz());
        });
    }
}
