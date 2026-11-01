package teddy.director.client.data.keyframe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.playback.DummyPlaybackController;
import teddy.director.client.playback.MoviePlaybackController;

import java.util.List;
import java.util.UUID;

public class ModelPartKeyframe extends AbstractKeyframe {
    public String key;
    public Vec3 position;
    public Vec3 rotation;

    public ModelPartKeyframe() {}

    public ModelPartKeyframe(int tick, String key, Vec3 position, Vec3 rotation) {
        super(tick);

        this.key = key;
        this.position = position;
        this.rotation = rotation;
    }

    public void updateDummies(int currTick, List<UUID> entities, @NonNull ModelPartKeyframe next) {
        int dTick = next.tick - tick;

        float t = dTick == 0f ? 1f : (currTick - tick) / (float) dTick;

        Vec3 lerpedPos = position.lerp(next.position, t);
        Vec3 lerpedRot = rotation.lerp(next.rotation, t);

        for (UUID entityUUID : entities) {
            DummyEntity entity = MoviePlaybackController.dummyPlaybackController.dummyEntities.get(entityUUID);
            if (entity == null)
                continue;

            entity.updateModelPart(key, lerpedPos, lerpedRot);
        }
    }

    @Override
    public String getTypeId() {
        return "model_part";
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
