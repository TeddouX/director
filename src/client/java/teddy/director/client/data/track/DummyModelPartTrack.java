package teddy.director.client.data.track;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import imgui.ImGui;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.data.track.keyframe.DummyModelPartKeyframe;
import teddy.director.client.data.track.keyframe.KeyframePair;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.entity.rendering.ModelPartLookup;
import teddy.director.client.playback.ScenePlaybackController;

import java.lang.reflect.Type;
import java.util.*;

public class DummyModelPartTrack extends AbstractTrack {
    public static final String TYPE_ID = "dummy_model_part";

    private TreeMap<Integer, DummyModelPartKeyframe> keyframes = new TreeMap<>();
    private List<UUID> controlledDummies = new ArrayList<>();
    private String dummyEntityTypeId = "";
    private EntityType<?> dummyEntityType = null;
    private String dummyModelPart = "";

    @Override
    public String getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void save(JsonObject obj, Gson gson) {
        super.save(obj, gson);

        obj.add("keyframes", gson.toJsonTree(keyframes));
        obj.add("controlled_dummies", gson.toJsonTree(controlledDummies));
        obj.addProperty("controlled_entity_type", dummyEntityTypeId);
        obj.addProperty("controlled_model_part", dummyModelPart);
    }

    @Override
    public void load(JsonObject obj, Gson gson) {
        super.load(obj, gson);

        Type keyframesTy = new TypeToken<TreeMap<Integer, DummyModelPartKeyframe>>(){}.getType();
        Type dummyUUIDsTy = new TypeToken<List<UUID>>(){}.getType();

        this.keyframes = gson.fromJson(obj.get("keyframes"), keyframesTy);
        this.controlledDummies = gson.fromJson(obj.get("controlled_dummies"), dummyUUIDsTy);
        this.dummyEntityTypeId = obj.get("controlled_entity_type").getAsString();
        this.dummyModelPart = obj.get("controlled_model_part").getAsString();

        for (DummyModelPartKeyframe kf : this.keyframes.values())
            kf.setParentTrack(this);
    }

    @Override
    public void update(int tick) {
        if (keyframes.isEmpty())
            return;

        KeyframePair keyframes = getKeyframesAround(tick);
        Vec3 lerpedPos;
        Vec3 lerpedRot;

        DummyModelPartKeyframe before = (DummyModelPartKeyframe) keyframes.before();
        DummyModelPartKeyframe after = (DummyModelPartKeyframe) keyframes.after();
        if (before == null && after == null) {
            return;
        }
        else if (after == null) {
            lerpedPos = before.position;
            lerpedRot = before.rotation;
        }
        else if (before == null) {
            lerpedPos = after.position;
            lerpedRot = after.rotation;
        }
        else {
            int dBefAf = after.tick - before.tick;
            int dBefTick = tick - before.tick;

            float x = dBefAf == 0 ? 0f : (float) dBefTick / dBefAf;
            x = Math.clamp(x, 0f, 1f);

            lerpedPos = before.position.lerp(after.position, x);
            lerpedRot = before.rotation.lerp(after.rotation, x);
        }

        for (UUID controlledDummyUUID : controlledDummies) {
            DummyEntity dummyEntity = ScenePlaybackController.dummyEntities.get(controlledDummyUUID);
            dummyEntity.updateModelPart(dummyModelPart, lerpedPos, lerpedRot);
        }
    }

    @Override
    public AbstractKeyframe createKeyframe(int tick) {
        DummyModelPartKeyframe kf = new DummyModelPartKeyframe(tick);
        keyframes.put(tick, kf);
        kf.setParentTrack(this);

        return kf;
    }

    @Override
    public TreeMap<Integer, ? extends AbstractKeyframe> getKeyframes() {
        return keyframes;
    }

    @Override
    public void moveKeyframe(int fromTick, int toTick) {
        if (fromTick == toTick) return;

        DummyModelPartKeyframe kf = keyframes.remove(fromTick);
        if (kf == null) return;

        kf.tick = toTick;
        keyframes.put(toTick, kf);
    }

    @Override
    public void drawEditWindow() {
        if (!windowOpen.get())
            return;

        ImGui.begin("Edit Track: \"%s\"###%s".formatted(name, uuid.toString()), windowOpen);

        ImGui.separatorText("Track Info");
        ImGui.text("Name: %s".formatted(name));
        ImGui.text("Type: %s".formatted(TYPE_ID));
        ImGui.text("UUID: %s".formatted(uuid.toString()));

        ImGui.separatorText("Controlled Dummies");

        if (!controlledDummies.isEmpty())
            ImGui.text("Controlled dummy entity type: %s".formatted(dummyEntityTypeId));

        float btnSize = ImGui.getFrameHeight();
        float padding = ImGui.getStyle().getWindowPaddingX();

        Map<UUID, DummyData> allDummies = DirectorState.getAllDummies();

        ImGui.setNextItemWidth(-1);
        if (ImGui.beginCombo("##combo", "Add controlled dummy...")) {
            boolean anyShown = false;

            int i = 0;
            for (DummyData dummy : allDummies.values()) {
                if (controlledDummies.contains(dummy.uuid))
                    continue;

                boolean showWarnOnAdd = false;
                if (!dummyEntityTypeId.isEmpty() && !Objects.equals(dummyEntityTypeId, dummy.displayEntityID)) {
                    if (controlledDummies.isEmpty())
                        showWarnOnAdd = true;
                    else continue;
                }

                anyShown = true;
                ImGui.pushID(i);

                ImGui.alignTextToFramePadding();
                ImGui.text(dummy.name);

                ImGui.sameLine(ImGui.getWindowWidth() - btnSize - padding);
                if (ImGui.button("+###%s".formatted(dummy.uuid), btnSize, btnSize)) {
                    if (showWarnOnAdd)
                        Director.LOGGER.info("bla");
                    else
                        controlledDummies.add(dummy.uuid);
                }

                ImGui.popID();
            }

            if (!anyShown)
                ImGui.textDisabled("Every dummy is already added");

            ImGui.endCombo();
        }

        ImGui.separator();

        if (ImGui.treeNode("Controlled Dummies")) {
            int toRemove = -1;
            for (int i = 0; i < controlledDummies.size(); i++) {
                UUID dummyUUID = controlledDummies.get(i);
                DummyData dummyData = DirectorState.getDummy(dummyUUID);

                if (dummyData == null)
                    continue;

                ImGui.pushID(i);

                ImGui.alignTextToFramePadding();
                ImGui.bulletText(dummyData.name);

                ImGui.sameLine(ImGui.getWindowWidth() - btnSize - padding);
                if (ImGui.button("x", btnSize, btnSize))
                    toRemove = i;

                ImGui.popID();
            }

            if (toRemove >= 0)
                controlledDummies.remove(toRemove);

            ImGui.treePop();
        }

        ImGui.separatorText("Controlled model part");

        EntityType<?> entityType = getDummyEntityType();
        if (entityType != null) {
            Model<?> dummyModel = ModelPartLookup.getEntityModel(entityType);
            if (ImGui.beginCombo("Model part", dummyModelPart.isEmpty() ? "Select a model part..." : dummyModelPart)) {
                for (String modelPart : ModelPartLookup.get(dummyModel).keySet()) {
                    if (ImGui.selectable(modelPart, dummyModelPart.equals(modelPart)))
                        dummyModelPart = modelPart;
                }

                ImGui.endCombo();
            }
        }

        ImGui.end();
    }

    private EntityType<?> getDummyEntityType() {
        if (dummyEntityTypeId.isEmpty() && !controlledDummies.isEmpty())
            dummyEntityTypeId = DirectorState.getDummy(controlledDummies.getFirst()).displayEntityID;

        if (dummyEntityType == null && !dummyEntityTypeId.isEmpty())
            dummyEntityType = Director.entityTypeFromStringId(dummyEntityTypeId);
        return dummyEntityType;
    }
}
