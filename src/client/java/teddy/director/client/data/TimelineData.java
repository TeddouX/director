package teddy.director.client.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.TrackRegistry;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class TimelineData {
    private static final String SAVE_FILE_NAME = "timeline_data.json";

    public String name;
    public UUID uuid;
    public List<AbstractTrack> tracks = new ArrayList<>();

    public TimelineData() {}

    public TimelineData(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID();
    }

    public void save(Path sceneFolderPath, Gson gson) throws IOException {
        Path timelineFolderPath = sceneFolderPath.resolve(uuid.toString());
        Path saveFile = timelineFolderPath.resolve(SAVE_FILE_NAME);

        Files.createDirectories(timelineFolderPath);

        SaveData saveData = new SaveData();
        saveData.name = name;
        saveData.uuid = uuid;

        for (AbstractTrack track : tracks) {
            saveData.trackUUIDs.add(track.uuid);

            Path trackFile = timelineFolderPath.resolve(track.uuid.toString());

            JsonObject obj = TrackRegistry.write(track, gson);
            Files.writeString(trackFile, gson.toJson(obj));
        }

        Files.writeString(saveFile, gson.toJson(saveData));
    }

    public static TimelineData load(Path sceneFolderPath, UUID uuid, Gson gson) throws IOException {
        Path timelineFolderPath = sceneFolderPath.resolve(uuid.toString());
        Path saveFile = timelineFolderPath.resolve(SAVE_FILE_NAME);
        TimelineData timelineData = new TimelineData();

        try (Reader reader = Files.newBufferedReader(saveFile)) {
            SaveData saveData = gson.fromJson(reader, SaveData.class);
            timelineData.uuid = saveData.uuid;
            timelineData.name = saveData.name;

            for (UUID trackUUID : saveData.trackUUIDs) {
                Path trackFile = timelineFolderPath.resolve(trackUUID.toString());
                JsonObject obj = JsonParser.parseReader(Files.newBufferedReader(trackFile)).getAsJsonObject();
                AbstractTrack track = TrackRegistry.read(obj, gson);

                timelineData.tracks.add(track);
            }
        }

        return timelineData;
    }

    public static class SaveData {
        public String name;
        public UUID uuid;
        public List<UUID> trackUUIDs = new ArrayList<>();
    }
}
