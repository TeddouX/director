package teddy.director.client.playback;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import teddy.director.client.data.TimelineData;
import teddy.director.client.data.keyframe.ModelPartKeyframe;
import teddy.director.client.entity.DummyEntity;

import java.util.*;

public class DummyPlaybackController {
    public Map<UUID, DummyEntity> dummyEntities = new HashMap<>();
    private final ClientLevel level;

    public DummyPlaybackController(ClientLevel level) {
        this.level = level;
    }

    public void addDummyEntity(DummyEntity entity) {
        dummyEntities.put(entity.data.uuid, entity);
    }

    public void removeDummyEntity(UUID uuid) {
        dummyEntities.remove(uuid);
    }

    public void removeAllEntities() {
        for (DummyEntity entity : dummyEntities.values()) {
            level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    public void update(int tick, TimelineData timeline) {
        if (timeline.controlledDummies.isEmpty())
            return;

        for (TimelineData.Track track : timeline.tracks) {
            if (Objects.equals(track.type, "model_part")) {
                int idx = track.getKeyFrameIndex(tick);
                int nextIdx = idx + 1;

                ModelPartKeyframe keyframe = (ModelPartKeyframe) track.keyframes.get(idx);

                ModelPartKeyframe nextKeyframe =
                        nextIdx >= track.keyframes.size()
                                ? (ModelPartKeyframe) track.keyframes.getLast()
                                : (ModelPartKeyframe) track.keyframes.get(nextIdx);

                keyframe.updateDummies(tick, timeline.controlledDummies, nextKeyframe);
            }
            else if (false) {
                // do some other thing
            }
        }
    }
}
