package teddy.director.client;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import teddy.director.Director;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.TrackRegistry;

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

    public static String getTrackName(int trackIdx) {
        return CURRENT_TIMELINE.tracks.get(trackIdx).name;
    }

    public static void renameTrack(int trackIdx, String newName) {
        AbstractTrack track = CURRENT_TIMELINE.tracks.get(trackIdx);
        String oldName = track.name;
        track.name = newName;
        
        Director.LOGGER.info("Renamed track {} to {}", oldName, newName);
    }

    public static int createAndAddTrack(String name, String type) {
        if (CURRENT_TIMELINE == null)
            return -1;

        AbstractTrack abstractTrack = TrackRegistry.getTrackFactory(type).get();
        abstractTrack.name = name;

        CURRENT_TIMELINE.tracks.add(abstractTrack);
        Director.LOGGER.info("Created track {}", name);

        return CURRENT_TIMELINE.tracks.size() - 1;
    }

    public static void deleteTrack(int trackIdx) {
        if (CURRENT_TIMELINE == null)
            return;

        String trackName = getTrackName(trackIdx);
        CURRENT_TIMELINE.tracks.remove(trackIdx);
        Director.LOGGER.info("Deleted track {}", trackName);
    }
}
