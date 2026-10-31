package teddy.director.client.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.data.DummyData;
import teddy.director.client.entity.rendering.DummyEntityAnimationState;

public class DummyEntity extends Entity {
    public EntityType<?> displayEntityType;
    public DummyData data;
    public DummyEntityAnimationState animationState;

    public DummyEntity(EntityType<DummyEntity> type, Level level) {
        super(type, level);
    }

    public EntityType<?> getDisplayEntity() {
        return displayEntityType;
    }

    public void setDisplayEntity(EntityType<?> entity) {
        displayEntityType = entity;
    }

    public void updateModelPart(String partPath, Vec3 newPos, Vec3 newRot) {
        animationState.partTransforms.put(partPath,
                new DummyEntityAnimationState.Transform(
                        (float) newPos.x, (float) newPos.y, (float) newPos.z,
                        (float) newRot.x, (float) newRot.y, (float) newRot.z
                )
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {

    }
}
