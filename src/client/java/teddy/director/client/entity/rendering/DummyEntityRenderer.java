package teddy.director.client.entity.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import teddy.director.client.entity.DummyEntity;

import java.util.Objects;

public class DummyEntityRenderer extends EntityRenderer<DummyEntity, DummyEntityRenderState> {
    private final EntityRenderDispatcher dispatcher;

    public DummyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public DummyEntityRenderState createRenderState() {
        return new DummyEntityRenderState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void extractRenderState(DummyEntity entity, DummyEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        EntityType<?> target = entity.getDisplayEntity();
        LivingEntityRenderState tempRenderState = new LivingEntityRenderState();

        tempRenderState.entityType = Objects.requireNonNullElse(target, EntityTypes.ARMOR_STAND);

        var renderer = (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) dispatcher.getRenderer(tempRenderState);

        state.delegateRenderer = renderer;
        state.delegateState = renderer.createRenderState();
        state.delegateEntityType = target;
        state.animState = entity.animationState;

        // renderer.extractRenderState(target, state.delegateState, partialTicks);
    }

    @Override
    public void submit(DummyEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.delegateState == null)
            return;
        
        state.delegateRenderer.submit(state.delegateState, poseStack, submitNodeCollector, camera);
    }
}
