package teddy.director.client.data.track;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.data.track.keyframe.DummyModelPartKeyframe;
import teddy.director.client.data.track.keyframe.DummyPositionKeyframe;

import java.util.TreeMap;

public class DummyPositionTrack extends AbstractTrack {
    public static final String TYPE_ID = "dummy_position";

    private TreeMap<Integer, DummyPositionKeyframe> keyframes = new TreeMap<>();

    @Override
    public String getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void save(JsonObject obj, Gson gson) {

    }

    @Override
    public void load(JsonObject obj, Gson gson) {

    }

    @Override
    public AbstractKeyframe createKeyframe(int tick) {
        DummyPositionKeyframe kf = new DummyPositionKeyframe(tick);
        keyframes.put(tick, kf);
        return kf;
    }

    @Override
    public TreeMap<Integer, ? extends AbstractKeyframe> getKeyframes() {
        return keyframes;
    }
}
