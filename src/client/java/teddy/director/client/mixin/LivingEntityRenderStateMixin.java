package teddy.director.client.mixin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import teddy.director.client.access.LivingEntityStateAccess;
import teddy.director.client.entity.rendering.DummyEntityAnimationState;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements LivingEntityStateAccess {
    @Unique private DummyEntityAnimationState director$animState;

    @Override
    public DummyEntityAnimationState director$getAnimState() {
        return director$animState;
    }

    @Override
    public void director$setAnimState(DummyEntityAnimationState state) {
        this.director$animState = state;
    }
}
