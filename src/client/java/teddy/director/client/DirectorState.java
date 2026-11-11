package teddy.director.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import teddy.director.Director;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.TrackRegistry;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.screen.Freecam;
import teddy.director.client.utils.Named;

import java.util.*;

public class DirectorState {
    public static MovieData CURRENT_MOVIE = null;
    public static SceneData CURRENT_SCENE = null;
    public static TimelineData CURRENT_TIMELINE = null;

    public static boolean IS_ACTIVE = true;

    public static void reset() {
        CURRENT_TIMELINE = null;
        CURRENT_SCENE = null;
        CURRENT_MOVIE = null;

        ScenePlaybackController.refreshScene();
    }

    // --- HELPERS ---

    private static <T extends Named> void delete(List<T> list, int idx, String kind) {
        Director.LOGGER.info("Deleted {} \"{}\"", kind, list.remove(idx).name);
    }

    private static <T extends Named> void rename(List<T> list, int idx, String newName, String kind) {
        T item = list.get(idx);
        Director.LOGGER.info("Renamed {} \"{}\" to \"{}\"", kind, item.name, newName);
        item.name = newName;
    }

    // --- MOVIE ---
    public static void createAndSetCurrentMovie(String name) {
        reset();

        MovieData movieData = new MovieData(name);
        CURRENT_MOVIE = movieData;

        Director.LOGGER.info("Created movie \"{}\"", movieData.name);
    }

    // --- SCENES ---

    public static String getSceneName(int idx) {
        return CURRENT_MOVIE == null ? "ERROR: No movie selected" : CURRENT_MOVIE.scenes.get(idx).name;
    }

    public static void deleteScene(int idx) {
        if (CURRENT_MOVIE != null) delete(CURRENT_MOVIE.scenes, idx, "scene");
    }

    public static void renameScene(int idx, String newName) {
        if (CURRENT_MOVIE != null) rename(CURRENT_MOVIE.scenes, idx, newName, "scene");
    }

    public static void setCurrentScene(int idx) {
        CURRENT_SCENE = CURRENT_MOVIE.scenes.get(idx);
        ScenePlaybackController.refreshScene();
    }

    public static int createAndAddScene(String name) {
        SceneData sceneData = new SceneData(name);

        DirectorState.CURRENT_MOVIE.scenes.add(sceneData);

        ScenePlaybackController.refreshScene();

        Director.LOGGER.info("Created scene \"{}\"", name);

        return DirectorState.CURRENT_MOVIE.scenes.size() - 1;
    }

    public static void createAndSummonDummy(String name, EntityType<?> displayEntityType) {
        if (CURRENT_SCENE == null) return;

        DummyData dummyData = new DummyData(name);
        dummyData.displayEntityID = BuiltInRegistries.ENTITY_TYPE.getKey(displayEntityType).toString();
        dummyData.pos = Freecam.pos;

        CURRENT_SCENE.dummies.put(dummyData.uuid, dummyData);
        ScenePlaybackController.summonDummy(dummyData);
    }

    public static Map<UUID, DummyData> getAllDummies() {
        if (CURRENT_SCENE == null) return new HashMap<>();
        return CURRENT_SCENE.dummies;
    }

    @Nullable
    public static DummyData getDummy(UUID uuid) {
        if (CURRENT_SCENE == null) return null;
        return CURRENT_SCENE.dummies.get(uuid);
    }

    // --- TIMELINES ---

    public static String getTimelineName(int idx) {
        return CURRENT_SCENE == null ? "ERROR: No scene selected" : CURRENT_SCENE.timelines.get(idx).name;
    }

    public static void deleteTimeline(int idx) {
        if (CURRENT_SCENE != null) delete(CURRENT_SCENE.timelines, idx, "timeline");
    }

    public static void renameTimeline(int idx, String newName) {
        if (CURRENT_SCENE != null) rename(CURRENT_SCENE.timelines, idx, newName, "timeline");
    }

    public static void setCurrentTimeline(int idx) {
        CURRENT_TIMELINE = CURRENT_SCENE.timelines.get(idx);
    }

    public static int createAndAddTimeline(String name) {
        if (CURRENT_SCENE == null) return -1;

        TimelineData timeline = new TimelineData(name);
        CURRENT_SCENE.timelines.add(timeline);

        Director.LOGGER.info("Created timeline {}", name);

        return CURRENT_SCENE.timelines.size() - 1;
    }

    // --- TRACKS ---

    public static AbstractTrack getTrack(int idx) {
        return CURRENT_TIMELINE.tracks.get(idx);
    }

    public static String getTrackName(int idx) {
        return CURRENT_TIMELINE == null ? "ERROR: No timeline loaded" : CURRENT_TIMELINE.tracks.get(idx).name;
    }

    public static void deleteTrack(int idx) {
        if (CURRENT_TIMELINE != null) delete(CURRENT_TIMELINE.tracks, idx, "track");
    }

    public static void renameTrack(int idx, String newName) {
        if (CURRENT_TIMELINE != null) rename(CURRENT_TIMELINE.tracks, idx, newName, "track");
    }

    public static void createAndAddTrack(String name, String type) {
        if (CURRENT_TIMELINE == null) return;

        AbstractTrack track = TrackRegistry.getTrackFactory(type).get();
        track.name = name;
        track.uuid = UUID.randomUUID();

        CURRENT_TIMELINE.tracks.add(track);

        Director.LOGGER.info("Created track \"{}\"", name);

    }
}
