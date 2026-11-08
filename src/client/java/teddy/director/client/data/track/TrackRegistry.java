package teddy.director.client.data.track;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.*;
import java.util.function.Supplier;

public class TrackRegistry {
    private static final Map<String, Supplier<? extends AbstractTrack>> TYPES = new HashMap<>();
    private static List<String> ALL_IDS = new ArrayList<>();

    public static void register(String id, Supplier<? extends AbstractTrack> factory) {
        TYPES.put(id, factory);
    }

    public static JsonObject write(AbstractTrack track, Gson gson) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", track.getTypeID());
        track.save(obj, gson);

        return obj;
    }

    public static AbstractTrack read(JsonObject obj, Gson gson) {
        String id = obj.get("type").getAsString();
        Supplier<? extends AbstractTrack> factory = getTrackFactory(id);
        AbstractTrack keyframe = factory.get();

        keyframe.load(obj, gson);

        return keyframe;
    }

    public static Supplier<? extends AbstractTrack> getTrackFactory(String name) {
        Supplier<? extends AbstractTrack> factory = TYPES.get(name);
        if (factory == null)
            throw new JsonParseException("Unknown track type: " + name);

        return factory;
    }

    public static List<String> getAllTrackTypeIDs() {
        return ALL_IDS;
    }

    private static void registrationFinished() {
        ALL_IDS = new ArrayList<>(TYPES.keySet());
    }

    static {
        register(DummyModelPartTrack.TYPE_ID, DummyModelPartTrack::new);
        register(DummyPositionTrack.TYPE_ID, DummyPositionTrack::new);

        registrationFinished();
    }
}
