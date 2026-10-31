package teddy.director.client.data.keyframe;

import com.google.gson.JsonObject;

public abstract class AbstractKeyframe {
    public int tick;

    protected AbstractKeyframe(int tick) {
        this.tick = tick;
    }

    protected AbstractKeyframe() {}

    public abstract String getTypeId();

    public void save(JsonObject json) {
        json.addProperty("tick", tick);
    }

    public void load(JsonObject json) {
        tick = json.get("tick").getAsInt();
    }
}
