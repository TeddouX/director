package teddy.director.client.playback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import teddy.director.Director;
import teddy.director.client.DirectorClient;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.entity.ClientEntities;
import teddy.director.client.entity.DummyEntity;

import java.util.List;

public class MoviePlaybackController {
    public static DummyPlaybackController dummyPlaybackController = null;

    public static void loadMovieData(MovieData movieData) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            Director.LOGGER.error("Must be in a world to load a movie");
            return;
        }

        DirectorState.CURRENT_MOVIE = movieData;

        if (dummyPlaybackController != null) {
            dummyPlaybackController.removeAllEntities();
        }
        dummyPlaybackController = new DummyPlaybackController(level);

        summonAllDummies(movieData.dummies, level);

        // Original positions and state
        update(0);
    }

    public static void update(int tick) {
        MovieData currMovie = DirectorState.CURRENT_MOVIE;
        if (currMovie == null)
            return;

        for (TimelineData timeline : currMovie.timelines)
            dummyPlaybackController.update(tick, timeline);
    }

    private static void summonAllDummies(List<DummyData> dummies, ClientLevel level) {
        for (DummyData dummyData : dummies) {
            Identifier id = Identifier.tryParse(dummyData.displayEntityID);
            if (id == null) {
                Director.LOGGER.error("Malformed identifier {}, skipping dummy {}",
                        dummyData.displayEntityID,
                        dummyData.name
                );
                continue;
            }

            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                Director.LOGGER.error("Entity type {} doesn't exist in the registry, skipping dummy {}",
                        dummyData.displayEntityID,
                        dummyData.name
                );
                continue;
            }

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);

            DummyEntity dummyEntity = new DummyEntity(ClientEntities.DUMMY, level);
            dummyEntity.data = dummyData;
            dummyEntity.displayEntityType = entityType;

            dummyEntity.setId(DirectorClient.nextClientID());

            level.addEntity(dummyEntity);

            dummyPlaybackController.addDummyEntity(dummyEntity);
        }
    }
}
