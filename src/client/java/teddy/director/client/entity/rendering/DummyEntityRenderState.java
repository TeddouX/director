package teddy.director.client.entity.rendering;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class DummyEntityRenderState extends EntityRenderState {
    public LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> delegateRenderer;
    public LivingEntityRenderState delegateState;
    public EntityType<?> delegateEntityType;
    public DummyEntityAnimationState animState;
}
