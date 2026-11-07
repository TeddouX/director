package teddy.director.client.data.track;

import com.google.gson.JsonObject;

public class DummyPositionTrack extends AbstractTrack {
    public static final String TYPE_ID = "dummy_position";

    @Override
    public String getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void save(JsonObject obj) {

    }

    @Override
    public void load(JsonObject obj) {

    }
}
