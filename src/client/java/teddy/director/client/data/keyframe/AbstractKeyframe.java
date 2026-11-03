package teddy.director.client.data.keyframe;

import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import teddy.director.client.data.TimelineData;

public abstract class AbstractKeyframe {
    public int tick;
    public TimelineData parentTimeline;

    protected AbstractKeyframe(int tick, TimelineData parentTimeline) {
        this.tick = tick;
        this.parentTimeline = parentTimeline;
    }

    protected AbstractKeyframe() {}

    public abstract void update(int currTick, @NonNull AbstractKeyframe next);

    public abstract String getTypeId();

    public void save(JsonObject json) {
        json.addProperty("tick", tick);
    }

    public void load(JsonObject json) {
        tick = json.get("tick").getAsInt();
    }
}
