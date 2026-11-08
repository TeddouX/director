package teddy.director.client.data.track;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.data.track.keyframe.DummyModelPartKeyframe;

import java.lang.reflect.Type;
import java.util.TreeMap;

public class DummyModelPartTrack extends AbstractTrack {
    public static final String TYPE_ID = "dummy_model_part";

    private TreeMap<Integer, DummyModelPartKeyframe> keyframes = new TreeMap<>();

    @Override
    public String getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void save(JsonObject obj, Gson gson) {
        super.save(obj, gson);

        obj.add("keyframes", gson.toJsonTree(keyframes));
    }

    @Override
    public void load(JsonObject obj, Gson gson) {
        super.load(obj, gson);

        Type type = new TypeToken<TreeMap<Integer, DummyModelPartKeyframe>>(){}.getType();
        this.keyframes = gson.fromJson(obj.get("keyframes"), type);
    }

    @Override
    public AbstractKeyframe createKeyframe(int tick) {
        DummyModelPartKeyframe kf = new DummyModelPartKeyframe(tick);
        keyframes.put(tick, kf);
        return kf;
    }

    @Override
    public TreeMap<Integer, ? extends AbstractKeyframe> getKeyframes() {
        return keyframes;
    }
}
