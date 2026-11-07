package teddy.director.client.data.track;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TrackRegistry {
    private static final Map<String, Supplier<? extends AbstractTrack>> TYPES = new HashMap<>();

    public static void register(String id, Supplier<? extends AbstractTrack> factory) {
        TYPES.put(id, factory);
    }

    public static JsonObject write(AbstractTrack track) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", track.getTypeID());
        track.save(obj);

        return obj;
    }

    public static AbstractTrack read(JsonObject obj) {
        String id = obj.get("type").getAsString();
        Supplier<? extends AbstractTrack> factory = getKeyframeFactory(id);
        AbstractTrack keyframe = factory.get();

        keyframe.load(obj);

        return keyframe;
    }

    public static Supplier<? extends AbstractTrack> getKeyframeFactory(String name) {
        Supplier<? extends AbstractTrack> factory = TYPES.get(name);
        if (factory == null)
            throw new JsonParseException("Unknown track type: " + name);

        return factory;
    }

    static {
        register(DummyModelPartTrack.TYPE_ID, DummyModelPartTrack::new);
        register(DummyPositionTrack.TYPE_ID, DummyPositionTrack::new);
    }
}
