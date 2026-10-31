package teddy.director.client.playback;

import teddy.director.client.data.TimelineData;
import teddy.director.client.data.keyframe.ModelPartKeyframe;
import teddy.director.client.entity.DummyEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DummyPlaybackController {
    public static Map<UUID, DummyEntity> dummyEntities = new HashMap<>();

    public static void addDummyEntity(DummyEntity entity) {
        dummyEntities.put(entity.data.uuid, entity);
    }

    public static void removeDummyEntity(UUID uuid) {
        dummyEntities.remove(uuid);
    }

    public static void update(int tick, TimelineData timeline) {
        if (timeline.controlledDummies.isEmpty())
            return;

        for (TimelineData.Track track : timeline.tracks) {
            if (!Objects.equals(track.type, "model_part"))
                break;

            int idx = track.getKeyFrameIndex(tick);
            int nextIdx = idx + 1;

            ModelPartKeyframe keyframe = (ModelPartKeyframe) track.keyframes.get(idx);

            ModelPartKeyframe nextKeyframe =
                    nextIdx >= track.keyframes.size()
                            ? (ModelPartKeyframe) track.keyframes.getLast()
                            : (ModelPartKeyframe) track.keyframes.get(nextIdx);

            keyframe.updateDummies(tick, timeline.controlledDummies, nextKeyframe);
        }
    }
}
