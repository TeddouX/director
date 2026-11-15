package teddy.director.client.data.track;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import imgui.type.ImBoolean;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.data.track.keyframe.KeyframePair;
import teddy.director.client.utils.Named;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public abstract class AbstractTrack extends Named {
    public transient ImBoolean windowOpen = new ImBoolean(false);
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

    public abstract void update(long ms);

    public abstract AbstractKeyframe createKeyframe(long ms);

    public abstract TreeMap<Long, ? extends AbstractKeyframe> getKeyframes();

    public abstract void moveKeyframe(long fromMs, long toMs);

    protected KeyframePair getKeyframesAround(long ms) {
        Map.Entry<Long, ? extends AbstractKeyframe> lower  = getKeyframes().floorEntry(ms);
        Map.Entry<Long, ? extends AbstractKeyframe> higher = getKeyframes().ceilingEntry(ms);

        return new KeyframePair(
                lower == null ? null : lower.getValue(),
                higher == null ? null : higher.getValue()
        );
    }

    public void showEditWindow() {
        windowOpen.set(true);
    }

    public abstract void drawEditWindow();
}

