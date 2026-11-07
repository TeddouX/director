package teddy.director.client.data;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovieData {
    private static final Gson GSON = new GsonBuilder().create();

    private static final String MOVIE_DATA_SAVE_FILE_NAME = "movie_data.json";
    private static final String ROOT_DIR_NAME = "director";
    private static final String MOVIE_SAVE_DIR_NAME = "movies";

    public String name;
    public UUID uuid;
    public List<SceneData> scenes;

    public MovieData() {}

    public MovieData(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID();
    }

    private static Path rootSaveDir() {
        return FabricLoader.getInstance()
                .getGameDir()
                .resolve(ROOT_DIR_NAME)
                .resolve(MOVIE_SAVE_DIR_NAME);
    }

    public void save() throws IOException {
        Path saveDir = rootSaveDir().resolve(uuid.toString());
        Files.createDirectories(saveDir);

        Path movieDataSaveFile = saveDir.resolve(MOVIE_DATA_SAVE_FILE_NAME);

        SaveData saveData = new SaveData();
        saveData.name = name;
        saveData.uuid = uuid;

        for (SceneData sceneData : scenes) {
            SaveData.TinySceneData tinySceneData = new SaveData.TinySceneData();
            tinySceneData.name = sceneData.name;
            tinySceneData.uuid = sceneData.uuid;

            saveData.scenes.add(tinySceneData);

            sceneData.save(saveDir, GSON);
        }

        Files.writeString(movieDataSaveFile, GSON.toJson(saveData));
    }

    public static MovieData load(UUID movieUUID) throws IOException {
        Path saveDir = rootSaveDir().resolve(movieUUID.toString());
        Path movieDataSaveFile = saveDir.resolve(MOVIE_DATA_SAVE_FILE_NAME);
        MovieData movieData = new MovieData();

        try (Reader reader = Files.newBufferedReader(movieDataSaveFile)) {
            SaveData saveData = GSON.fromJson(reader, SaveData.class);
            movieData.uuid = saveData.uuid;
            movieData.name = saveData.name;

            for (SaveData.TinySceneData tinySceneData : saveData.scenes) {
                SceneData sceneData = SceneData.load(saveDir, tinySceneData.uuid, GSON);
                movieData.scenes.add(sceneData);
            }
        }

        return movieData;
    }

    public static class SaveData {
        public static class TinySceneData {
            public String name;
            public UUID uuid;
        }

        public String name;
        public UUID uuid;
        public List<TinySceneData> scenes = new ArrayList<>();
    }
}
