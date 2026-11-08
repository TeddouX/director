package teddy.director.client.data;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import teddy.director.Director;
import teddy.director.client.utils.Named;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class MovieData extends Named {
    private static final Gson GSON = new GsonBuilder().create();

    private static final String MOVIE_DATA_SAVE_FILE_NAME = "movie_data.json";
    private static final String ROOT_DIR_NAME = "director";
    private static final String MOVIE_SAVE_DIR_NAME = "movies";

    private static List<SaveData> CACHED_SAVE_DATA = new ArrayList<>();

    public UUID uuid;
    public List<SceneData> scenes = new ArrayList<>();

    public MovieData() {
        super("");
    }

    public MovieData(String name) {
        super(name);

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

        clearDir(saveDir);

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
        SaveData saveData = loadSaveData(movieDataSaveFile);

        MovieData movieData = new MovieData();
        movieData.uuid = saveData.uuid;
        movieData.name = saveData.name;

        for (SaveData.TinySceneData tinySceneData : saveData.scenes) {
            SceneData sceneData = SceneData.load(saveDir, tinySceneData.uuid, GSON);
            movieData.scenes.add(sceneData);
        }

        return movieData;
    }

    public static List<SaveData> listAllSavedMovies(boolean refreshCache) {
        if (!refreshCache && !CACHED_SAVE_DATA.isEmpty())
            return CACHED_SAVE_DATA;

        List<SaveData> saveData = new ArrayList<>();

        File[] movieDirs = rootSaveDir().toFile().listFiles((file, _) -> file.isDirectory());
        if (movieDirs == null)
            return saveData;

        for (File movieDir : movieDirs) {
            Path movieSaveFile = movieDir.toPath().resolve(MOVIE_DATA_SAVE_FILE_NAME);

            try {
                saveData.add(loadSaveData(movieSaveFile));
            }
            catch (IOException _) {}
        }

        CACHED_SAVE_DATA = saveData;
        return CACHED_SAVE_DATA;
    }

    private static SaveData loadSaveData(Path movieDataSaveFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(movieDataSaveFile)) {
            return GSON.fromJson(reader, SaveData.class);
        }
    }

    private static void clearDir(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
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
