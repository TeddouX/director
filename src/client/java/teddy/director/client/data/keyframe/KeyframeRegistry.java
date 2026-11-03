package teddy.director.client.data.keyframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class KeyframeRegistry {
    private static final Map<String, Supplier<? extends AbstractKeyframe>> TYPES = new HashMap<>();

    public static void register(String id, Supplier<? extends AbstractKeyframe> factory) {
        TYPES.put(id, factory);
    }

    public static JsonObject write(AbstractKeyframe keyframe) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", keyframe.getTypeId());
        keyframe.save(obj);

        return obj;
    }

    public static AbstractKeyframe read(JsonObject obj) {
        String id = obj.get("type").getAsString();
        Supplier<? extends AbstractKeyframe> factory = getKeyframeFactory(id);
        AbstractKeyframe keyframe = factory.get();

        keyframe.load(obj);

        return keyframe;
    }

    public static Supplier<? extends AbstractKeyframe> getKeyframeFactory(String name) {
        Supplier<? extends AbstractKeyframe> factory = TYPES.get(name);
        if (factory == null)
            throw new JsonParseException("Unknown keyframe type: " + name);

        return factory;
    }

    static {
        register(ModelPartKeyframe.TYPE_ID, ModelPartKeyframe::new);
    }
}
