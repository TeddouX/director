package teddy.director.client.access;

import teddy.director.client.entity.rendering.DummyEntityAnimationState;

public interface DirectorLivingEntityStateAccess {
    DummyEntityAnimationState director$getAnimState();
    void director$setAnimState(DummyEntityAnimationState state);
}
