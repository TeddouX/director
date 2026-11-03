package teddy.director.client.data.keyframe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import teddy.director.client.data.TimelineData;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.playback.MoviePlaybackController;

import java.util.List;
import java.util.UUID;

public class ModelPartKeyframe extends AbstractKeyframe {
    public static final String TYPE_ID = "model_part";

    public String key;
    public Vec3 position;
    public Vec3 rotation;

    public ModelPartKeyframe() {}

    public ModelPartKeyframe(int tick, TimelineData parentTimeline) {
        super(tick, parentTimeline);
    }

    @Override
    public void update(int currTick, @NonNull AbstractKeyframe nextAbstract) {
        ModelPartKeyframe next = (ModelPartKeyframe) nextAbstract;
        int dTick = next.tick - tick;

        float t = dTick == 0f ? 1f : (currTick - tick) / (float) dTick;

        Vec3 lerpedPos = position.lerp(next.position, t);
        Vec3 lerpedRot = rotation.lerp(next.rotation, t);

        for (UUID entityUUID : parentTimeline.controlledDummies) {
            DummyEntity entity = MoviePlaybackController.dummyEntities.get(entityUUID);
            if (entity == null)
                continue;

            entity.updateModelPart(key, lerpedPos, lerpedRot);
        }
    }

    public void updateDummies(int currTick, List<UUID> entities, @NonNull ModelPartKeyframe next) {

    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public void save(JsonObject json) {
        super.save(json);

        json.addProperty("model_key", key);
        json.add("position", Vec3.CODEC.encodeStart(JsonOps.INSTANCE, position).getOrThrow());
        json.add("rotation", Vec3.CODEC.encodeStart(JsonOps.INSTANCE, rotation).getOrThrow());
    }

    @Override
    public void load(JsonObject json) {
        super.load(json);

        key = json.get("model_key").getAsString();
        position = Vec3.CODEC.parse(JsonOps.INSTANCE, json.get("position")).getOrThrow();
        rotation = Vec3.CODEC.parse(JsonOps.INSTANCE, json.get("rotation")).getOrThrow();
    }
}
