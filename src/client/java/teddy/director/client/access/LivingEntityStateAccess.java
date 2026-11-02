package teddy.director.client.access;

import teddy.director.client.entity.rendering.DummyEntityAnimationState;

public interface LivingEntityStateAccess {
    DummyEntityAnimationState director$getAnimState();
    void director$setAnimState(DummyEntityAnimationState state);
}
