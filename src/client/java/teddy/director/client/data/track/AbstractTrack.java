package teddy.director.client.data.track;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.data.track.keyframe.KeyframePair;
import teddy.director.client.utils.Named;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public abstract class AbstractTrack extends Named {
    public UUID uuid;

    public AbstractTrack() {
        super("");
    }

    public abstract String getTypeID();

    public void save(JsonObject obj, Gson gson) {
        obj.addProperty("uuid", uuid.toString());
        obj.addProperty("name", name);
    }

    public void load(JsonObject obj, Gson gson) {
        this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        this.name = obj.get("name").getAsString();
    }

    public abstract AbstractKeyframe createKeyframe(int tick);

    public abstract TreeMap<Integer, ? extends AbstractKeyframe> getKeyframes();

    public KeyframePair getKeyframesAround(int tick) {
        Map.Entry<Integer, ? extends AbstractKeyframe> lower  = getKeyframes().floorEntry(tick);
        Map.Entry<Integer, ? extends AbstractKeyframe> higher = getKeyframes().ceilingEntry(tick);

        return new KeyframePair(
                lower == null ? null : lower.getValue(),
                higher == null ? null : higher.getValue()
        );
    }
}

