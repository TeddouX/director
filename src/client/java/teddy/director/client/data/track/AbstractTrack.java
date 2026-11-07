package teddy.director.client.data.track;

import com.google.gson.JsonObject;

import java.util.UUID;

public abstract class AbstractTrack {
    public UUID uuid;

    public AbstractTrack() {
        this.uuid = UUID.randomUUID();
    }

    public abstract String getTypeID();

    public abstract void save(JsonObject obj);
    public abstract void load(JsonObject obj);
}
