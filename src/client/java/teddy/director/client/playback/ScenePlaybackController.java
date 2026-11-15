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
import teddy.director.client.data.SceneData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.entity.ClientEntities;
import teddy.director.client.entity.DummyEntity;

import java.util.*;

public class ScenePlaybackController {
    public static Map<UUID, DummyEntity> dummyEntities = new HashMap<>();
    private static long currSceneMs = 0;
    private static boolean running = false;

    public static void refreshScene() {
        SceneData newData = DirectorState.CURRENT_SCENE;

        removeAllDummyEntities();

        if (newData != null) {
            summonAllDummies(newData.dummies);

            // Set everything to its original state
            updataMilli(0);
        }
    }

    public static void startPlayback(long ms) {
        currSceneMs = ms;
        running = true;
    }

    public static void pausePlayback() {
        running = false;
    }

    public static long playbackCurrMilli() {
        if (!running) return -1;
        return currSceneMs;
    }

    public static boolean playbackRunning() {
        return running;
    }

    public static void frame(long delta) {
        if (!running) return;

        currSceneMs += delta;
        updataMilli(currSceneMs);
    }

    public static void updataMilli(long ms) {
        if (DirectorState.CURRENT_SCENE == null) return;

        for (TimelineData timeline : DirectorState.CURRENT_SCENE.timelines) {
            for (AbstractTrack track : timeline.tracks) {
                track.update(ms);
            }
        }
    }

    private static void summonAllDummies(Map<UUID, DummyData> dummies) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        for (DummyData dummyData : dummies.values())
            summonDummy(dummyData);
    }

    public static void summonDummy(DummyData dummy) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        Identifier id = Identifier.tryParse(dummy.displayEntityID);
        if (id == null) {
            Director.LOGGER.error("Malformed identifier {}, skipping dummy {}",
                    dummy.displayEntityID,
                    dummy.name
            );
            return;
        }

        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            Director.LOGGER.error("Entity type {} doesn't exist in the registry, skipping dummy {}",
                    dummy.displayEntityID,
                    dummy.name
            );
            return;
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);

        DummyEntity dummyEntity = new DummyEntity(ClientEntities.DUMMY, level);
        dummyEntity.data = dummy;
        dummyEntity.displayEntityType = entityType;

        dummyEntity.setPos(dummy.pos);
        dummyEntity.setId(DirectorClient.nextClientID());

        level.addEntity(dummyEntity);

        dummyEntities.put(dummy.uuid, dummyEntity);
    }

    public static void removeDummyEntity(UUID uuid) {
        DummyEntity dummyEntity = dummyEntities.remove(uuid);

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        level.removeEntity(dummyEntity.getId(), Entity.RemovalReason.DISCARDED);
    }

    public static void removeAllDummyEntities() {
        for (DummyEntity entity : dummyEntities.values()) {
            Minecraft.getInstance().level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
        }
        dummyEntities.clear();
    }
}
