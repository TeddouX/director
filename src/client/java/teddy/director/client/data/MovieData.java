package teddy.director.client.data;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FilenameUtils;
import teddy.director.Director;
import teddy.director.client.data.keyframe.AbstractKeyframe;
import teddy.director.client.data.keyframe.KeyframeRegistry;
import teddy.director.client.data.keyframe.ModelPartKeyframe;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MovieData {
    public static class KeyframeAdapter implements JsonSerializer<AbstractKeyframe>, JsonDeserializer<AbstractKeyframe> {
        @Override
        public JsonElement serialize(AbstractKeyframe src, Type t, JsonSerializationContext ctx) {
            return KeyframeRegistry.write(src);
        }

        @Override
        public AbstractKeyframe deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) {
            return KeyframeRegistry.read(json.getAsJsonObject());
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AbstractKeyframe.class, new KeyframeAdapter())
            .setPrettyPrinting()
            .create();

    private static final String SAVE_DIR_NAME = "director_movies";
    private static final String SAVE_FILE_EXT = "dir_movie";

    private static List<MovieData> MOVIES_CACHE = new ArrayList<>();

    public String name;
    public UUID uuid;
    public List<TimelineData> timelines = new ArrayList<>();
    public List<DummyData> dummies = new ArrayList<>();

    public MovieData(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID();
    }

    private static Path saveDir() {
        return FabricLoader.getInstance().getGameDir().resolve(SAVE_DIR_NAME);
    }

    private static Path getSaveFile(MovieData data) {
        return saveDir().resolve(data.uuid.toString() + "." + SAVE_FILE_EXT);
    }

    public static void save(MovieData data) throws IOException {
        Files.createDirectories(saveDir());

        Path saveFile = getSaveFile(data);
        Files.writeString(saveFile, GSON.toJson(data));

        Director.LOGGER.info("Saved movie \"{}\" to file:{}", data.name, saveFile.toAbsolutePath());
    }

    public static List<MovieData> listAllSavedMovies(boolean reloadCache) {
        if (!reloadCache && !MOVIES_CACHE.isEmpty())
            return MOVIES_CACHE;

        List<MovieData> savedData = new ArrayList<>();

        File[] files = saveDir().toFile().listFiles((_, name) ->
                Objects.equals(FilenameUtils.getExtension(name), SAVE_FILE_EXT)
        );

        if (files == null)
            return savedData;

        for (File file : files) {
            try (Reader reader = Files.newBufferedReader(file.toPath())){
                MovieData data = GSON.fromJson(reader, MovieData.class);
                savedData.add(data);
            }
            catch (IOException e) {
                Director.LOGGER.info("Failed to read file {}", file.toPath());
            }
        }

        MOVIES_CACHE = savedData;
        return MOVIES_CACHE;
    }
}
