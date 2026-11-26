package teddy.director.client.data.track;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import imgui.type.ImBoolean;
import teddy.director.client.utils.Named;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public abstract class AbstractTrack<T extends AbstractKeyframe> extends Named {
    public ImBoolean windowOpen = new ImBoolean(false);
    public UUID uuid;

    private TreeMap<Long, T> keyframes = new TreeMap<>();

    public AbstractTrack() {
        super("");
    }

    public abstract String getTypeID();

    public void save(JsonObject obj, Gson gson) {
        obj.addProperty("uuid", uuid.toString());
        obj.addProperty("name", name);
        obj.add("keyframes", gson.toJsonTree(keyframes));
    }

    public void load(JsonObject obj, Gson gson) {
        this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        this.name = obj.get("name").getAsString();

        Type kfClass = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Type keyframesTy = TypeToken
                .getParameterized(TreeMap.class, Long.class, kfClass)
                .getType();

        this.keyframes = gson.fromJson(obj.get("keyframes"), keyframesTy);
        for (T kf : keyframes.values())
            kf.setParentTrack(this);
    }

    public abstract void update(long ms);

    public abstract T createKeyframe(long ms);

    public void addKeyframe(long ms, T kf) {
        getKeyframes().put(ms, kf);
        kf.setParentTrack(this);
    }

    public TreeMap<Long, T> getKeyframes() {
        return keyframes;
    }

    public void moveKeyframe(long fromMs, long toMs) {
        if (fromMs == toMs) return;

        T kf = getKeyframes().remove(fromMs);
        if (kf == null) return;

        T toKf = getKeyframes().get(toMs);
        if (toKf != null)
            toMs = toMs > fromMs
                    ? toMs + 1  // Moving from left to right
                    : toMs - 1; // Moving from right to left

        kf.ms = toMs;
        getKeyframes().put(toMs, kf);
    }

    protected KeyframePair getKeyframesAround(long ms) {
        Map.Entry<Long, T> lower  = getKeyframes().floorEntry(ms);
        Map.Entry<Long, T> higher = getKeyframes().ceilingEntry(ms);

        return new KeyframePair(
                lower == null ? null : lower.getValue(),
                higher == null ? null : higher.getValue()
        );
    }

    public void showEditWindow() {
        windowOpen.set(true);
    }

    public boolean isEditWindowOpen() {
        return windowOpen.get();
    }

    public abstract void drawEditWindow();
}

