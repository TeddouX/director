package teddy.director.client.data;

import com.google.gson.Gson;
import teddy.director.client.utils.Named;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SceneData extends Named {
    private static final String SAVE_FILE_NAME = "scene_data.json";

    public UUID uuid;
    public List<TimelineData> timelines = new ArrayList<>();
    public Map<UUID, DummyData> dummies = new HashMap<>();
    public CameraData camera = new CameraData();

    public SceneData() {
        super("");
    }

    public SceneData(String name) {
        super(name);
        this.uuid = UUID.randomUUID();
    }

    public void save(Path saveDir, Gson gson) throws IOException {
        Path folderPath = saveDir.resolve(uuid.toString());
        Path saveFilePath = folderPath.resolve(SAVE_FILE_NAME);

        Files.createDirectories(folderPath);

        SaveData saveData = new SaveData();
        saveData.name = name;
        saveData.uuid = uuid;
        saveData.dummies = dummies;
        saveData.camera = camera;

        for (TimelineData timelineData : timelines) {
            SaveData.TinyTimelineData tinyTimelineData = new SaveData.TinyTimelineData();
            tinyTimelineData.name = timelineData.name;
            tinyTimelineData.uuid = timelineData.uuid;

            saveData.timelines.add(tinyTimelineData);

            timelineData.save(folderPath, gson);
        }

        Files.writeString(saveFilePath, gson.toJson(saveData));
    }

    public static SceneData load(Path saveDir, UUID uuid, Gson gson) throws IOException {
        Path sceneFolderPath = saveDir.resolve(uuid.toString());
        Path saveFilePath = sceneFolderPath.resolve(SAVE_FILE_NAME);
        SceneData sceneData = new SceneData();

        try (Reader reader = Files.newBufferedReader(saveFilePath)) {
            SaveData saveData = gson.fromJson(reader, SaveData.class);
            sceneData.uuid = uuid;
            sceneData.name = saveData.name;
            sceneData.dummies = saveData.dummies;
            sceneData.camera = saveData.camera;

            for (SaveData.TinyTimelineData tinyTimelineData : saveData.timelines) {
                TimelineData timelineData = TimelineData.load(sceneFolderPath, tinyTimelineData.uuid, gson);
                sceneData.timelines.add(timelineData);
            }
        }

        return sceneData;
    }

    public static class SaveData {
        public static class TinyTimelineData {
            public String name;
            public UUID uuid;
        }

        public String name;
        public UUID uuid;
        public Map<UUID, DummyData> dummies = new HashMap<>();
        public List<TinyTimelineData> timelines = new ArrayList<>();
        public CameraData camera;
    }
}
