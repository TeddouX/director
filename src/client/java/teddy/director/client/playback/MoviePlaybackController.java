package teddy.director.client.playback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import teddy.director.Director;
import teddy.director.client.DirectorClient;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.entity.ClientEntities;
import teddy.director.client.entity.DummyEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoviePlaybackController {
    public static Map<UUID, DummyEntity> dummyEntities = new HashMap<>();

    public static void loadMovieData(MovieData movieData) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            Director.LOGGER.error("Must be in a world to load a movie");
            return;
        }

        DirectorState.CURRENT_MOVIE = movieData;

        removeAllDummyEntities();
        summonAllDummies(movieData.dummies, level);

        // Original positions and state
        update(0);
    }

    public static void update(int tick) {
        MovieData currMovie = DirectorState.CURRENT_MOVIE;
        if (currMovie == null)
            return;

        for (TimelineData timeline : currMovie.timelines) {
            for (TimelineData.Track track : timeline.tracks) {
                List<AbstractTrack> keyframes = track.getKeyFramesAffectingTick(tick);
                boolean firstExists = keyframes.get(0) == null;
                boolean secondExists = keyframes.get(1) == null;

                if (!firstExists && !secondExists) {
                    continue;
                }
                else if (!firstExists) {
                    AbstractTrack kf2 = keyframes.get(1);
                    kf2.update(tick, kf2);
                }
                else if (!secondExists) {
                    AbstractTrack kf1 = keyframes.get(0);
                    kf1.update(tick, kf1);
                }
                else {
                    AbstractTrack kf1 = keyframes.get(0);
                    AbstractTrack kf2 = keyframes.get(1);
                    kf1.update(tick, kf2);
                }
            }
        }
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

            addDummyEntity(dummyEntity);
        }
    }

    public static void addDummyEntity(DummyEntity entity) {
        dummyEntities.put(entity.data.uuid, entity);
    }

    public static void removeDummyEntity(UUID uuid) {
        dummyEntities.remove(uuid);
    }

    public static void removeAllDummyEntities() {
        for (DummyEntity entity : dummyEntities.values()) {
            Minecraft.getInstance().level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
        }
    }
}
