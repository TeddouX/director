package teddy.director.client.data;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SceneData {
    private static final String SAVE_FILE_NAME = "scene_data.json";

    public String name;
    public UUID uuid;
    public List<TimelineData> timelines = new ArrayList<>();
    public List<DummyData> dummies = new ArrayList<>();

    public SceneData() {}

    public SceneData(String name) {
        this.name = name;
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
        public List<DummyData> dummies;
        public List<TinyTimelineData> timelines;
    }
}
