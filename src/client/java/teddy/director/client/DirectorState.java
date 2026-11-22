package teddy.director.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import teddy.director.Director;
import teddy.director.client.data.*;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.TrackRegistry;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.screen.Freecam;
import teddy.director.client.utils.Named;

import java.util.*;

public class DirectorState {
    public static boolean renderingUsingRenderCam = false;
    public static boolean freezeCulling = false;

    public static MovieData currentMovie = null;
    public static SceneData currentScene = null;
    public static TimelineData currentTimeline = null;

    public static RenderCamera renderCamera = null;

    public static boolean isActive = true;

    public static void reset() {
        currentTimeline = null;
        currentScene = null;
        currentMovie = null;

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
        currentMovie = movieData;

        Director.LOGGER.info("Created movie \"{}\"", movieData.name);
    }

    // --- SCENES ---

    public static String getSceneName(int idx) {
        return currentMovie == null ? "ERROR: No movie selected" : currentMovie.scenes.get(idx).name;
    }

    public static void deleteScene(int idx) {
        if (currentMovie != null) delete(currentMovie.scenes, idx, "scene");
    }

    public static void renameScene(int idx, String newName) {
        if (currentMovie != null) rename(currentMovie.scenes, idx, newName, "scene");
    }

    public static void setCurrentScene(int idx) {
        currentScene = currentMovie.scenes.get(idx);
        renderCamera.cameraData = currentScene.camera;
        if (currentScene.camera != null)
            renderCamera.pos = currentScene.camera.origPos;

        ScenePlaybackController.refreshScene();
    }

    public static int createAndAddScene(String name) {
        SceneData sceneData = new SceneData(name);

        currentMovie.scenes.add(sceneData);

        ScenePlaybackController.refreshScene();

        Director.LOGGER.info("Created scene \"{}\"", name);

        return currentMovie.scenes.size() - 1;
    }

    public static void createAndSummonDummy(String name, EntityType<?> displayEntityType) {
        if (currentScene == null) return;

        DummyData dummyData = new DummyData(name);
        dummyData.displayEntityID = BuiltInRegistries.ENTITY_TYPE.getKey(displayEntityType).toString();
        dummyData.pos = Freecam.pos;

        currentScene.dummies.put(dummyData.uuid, dummyData);
        ScenePlaybackController.summonDummy(dummyData);
    }

    public static Map<UUID, DummyData> getAllDummies() {
        if (currentScene == null) return new HashMap<>();
        return currentScene.dummies;
    }

    @Nullable
    public static DummyData getDummy(UUID uuid) {
        if (currentScene == null) return null;
        return currentScene.dummies.get(uuid);
    }

    public static void createAndAddCamera(String name) {
        if (currentScene == null) return;

        CameraData camera = new CameraData(name);
        camera.origPos = Freecam.pos;

        renderCamera.yaw = Freecam.yaw;
        renderCamera.pitch = Freecam.pitch;
        renderCamera.pos = Freecam.pos;
        renderCamera.cameraData = camera;

        currentScene.camera = camera;
    }

    // --- TIMELINES ---

    public static String getTimelineName(int idx) {
        return currentScene == null ? "ERROR: No scene selected" : currentScene.timelines.get(idx).name;
    }

    public static void deleteTimeline(int idx) {
        if (currentScene != null) delete(currentScene.timelines, idx, "timeline");
    }

    public static void renameTimeline(int idx, String newName) {
        if (currentScene != null) rename(currentScene.timelines, idx, newName, "timeline");
    }

    public static void setCurrentTimeline(int idx) {
        currentTimeline = currentScene.timelines.get(idx);
    }

    public static int createAndAddTimeline(String name) {
        if (currentScene == null) return -1;

        TimelineData timeline = new TimelineData(name);
        currentScene.timelines.add(timeline);

        Director.LOGGER.info("Created timeline {}", name);

        return currentScene.timelines.size() - 1;
    }

    // --- TRACKS ---

    public static AbstractTrack<?> getTrack(int idx) {
        return currentTimeline.tracks.get(idx);
    }

    public static String getTrackName(int idx) {
        return currentTimeline == null ? "ERROR: No timeline loaded" : currentTimeline.tracks.get(idx).name;
    }

    public static void deleteTrack(int idx) {
        if (currentTimeline != null) delete(currentTimeline.tracks, idx, "track");
    }

    public static void renameTrack(int idx, String newName) {
        if (currentTimeline != null) rename(currentTimeline.tracks, idx, newName, "track");
    }

    public static void createAndAddTrack(String name, String type) {
        if (currentTimeline == null) return;

        AbstractTrack<?> track = TrackRegistry.getTrackFactory(type).get();
        track.name = name;
        track.uuid = UUID.randomUUID();

        currentTimeline.tracks.add(track);

        Director.LOGGER.info("Created track \"{}\"", name);

    }
}
