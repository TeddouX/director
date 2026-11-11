package teddy.director.client.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teddy.director.client.access.LivingEntityStateAccess;
import teddy.director.client.entity.rendering.DummyEntityAnimationState;
import teddy.director.client.entity.rendering.ModelPartLookup;

import java.util.Map;

@Mixin(Model.class)
public abstract class EntityModelMixin<S> {
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void director$applyPartTransforms(S state, CallbackInfo ci) {
        if (!(state instanceof LivingEntityStateAccess access))
            return;

        DummyEntityAnimationState animState = access.director$getAnimState();
        if (animState == null)
            return;

        var partTransforms = animState.partTransforms;
        if (partTransforms == null)
            return;

        Map<String, ModelPart> partLookup = ModelPartLookup.get((Model<?>) (Object) this);
        partTransforms.forEach((path, transform) -> {
            ModelPart modelPart = partLookup.get(path);
            if (modelPart == null)
                return;

            modelPart.x += transform.dx();
            modelPart.y += transform.dy();
            modelPart.z += transform.dz();

            modelPart.xRot += transform.rx();
            modelPart.yRot += transform.ry();
            modelPart.zRot += transform.rz();
        });
    }
}
