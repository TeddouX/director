package teddy.director.client.data.track;

import com.google.gson.JsonObject;

public class DummyModelPartTrack extends AbstractTrack {
    public static final String TYPE_ID = "dummy_model_part";

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
