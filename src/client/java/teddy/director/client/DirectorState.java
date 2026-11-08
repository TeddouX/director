package teddy.director.client;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import teddy.director.Director;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.TrackRegistry;
import teddy.director.client.utils.Named;

import java.util.List;
import java.util.UUID;

public class DirectorState {
    public static final EntityType<?> DEFAULT_DUMMY_ENTITY_TYPE = EntityTypes.ARMOR_STAND;

    public static MovieData CURRENT_MOVIE = null;
    public static SceneData CURRENT_SCENE = null;
    public static TimelineData CURRENT_TIMELINE = null;

    public static boolean IS_ACTIVE = true;

    public static void reset() {
        CURRENT_TIMELINE = null;
        CURRENT_SCENE = null;
        CURRENT_MOVIE = null;

        IS_ACTIVE = false;
    }

    // --- HELPERS ---

    private static <T extends Named> void delete(List<T> list, int idx, String kind) {
        Director.LOGGER.info("Deleted {} {}", kind, list.remove(idx).name);
    }

    private static <T extends Named> void rename(List<T> list, int idx, String newName, String kind) {
        T item = list.get(idx);
        Director.LOGGER.info("Renamed {} {} to {}", kind, item.name, newName);
        item.name = newName;
    }

    // --- MOVIE ---
    public static void createAndSetCurrentMovie(String name) {
        MovieData movieData = new MovieData(name);
        reset();
        CURRENT_MOVIE = movieData;

        Director.LOGGER.info("Created movie {}", movieData.name);
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

        Director.LOGGER.info("Created scene {}", name);

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

    public static int createAndAddTrack(String name, String type) {
        if (CURRENT_TIMELINE == null) return -1;

        AbstractTrack track = TrackRegistry.getTrackFactory(type).get();
        track.name = name;
        track.uuid = UUID.randomUUID();

        CURRENT_TIMELINE.tracks.add(track);

        Director.LOGGER.info("Created track {}", name);

        return CURRENT_TIMELINE.tracks.size() - 1;
    }
}
