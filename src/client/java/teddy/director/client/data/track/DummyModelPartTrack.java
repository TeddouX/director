package teddy.director.client.data.track;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import imgui.ImGui;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.data.track.keyframe.DummyModelPartKeyframe;

import java.lang.reflect.Type;
import java.util.*;

public class DummyModelPartTrack extends AbstractTrack {
    public static final String TYPE_ID = "dummy_model_part";

    private TreeMap<Integer, DummyModelPartKeyframe> keyframes = new TreeMap<>();
    private List<UUID> controlledDummies = new ArrayList<>();

    @Override
    public String getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void save(JsonObject obj, Gson gson) {
        super.save(obj, gson);

        obj.add("keyframes", gson.toJsonTree(keyframes));
        obj.add("controlled_dummies", gson.toJsonTree(controlledDummies));
    }

    @Override
    public void load(JsonObject obj, Gson gson) {
        super.load(obj, gson);

        Type keyframesTy = new TypeToken<TreeMap<Integer, DummyModelPartKeyframe>>(){}.getType();
        Type dummyUUIDsTy = new TypeToken<List<UUID>>(){}.getType();

        this.keyframes = gson.fromJson(obj.get("keyframes"), keyframesTy);
        this.controlledDummies = gson.fromJson(obj.get("controlled_dummies"), dummyUUIDsTy);
    }

    @Override
    public AbstractKeyframe createKeyframe(int tick) {
        DummyModelPartKeyframe kf = new DummyModelPartKeyframe(tick);
        keyframes.put(tick, kf);
        return kf;
    }

    @Override
    public TreeMap<Integer, ? extends AbstractKeyframe> getKeyframes() {
        return keyframes;
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

                anyShown = true;
                ImGui.pushID(i);

                ImGui.alignTextToFramePadding();
                ImGui.text(dummy.name);

                ImGui.sameLine(ImGui.getWindowWidth() - btnSize - padding);
                if (ImGui.button("+", btnSize, btnSize))
                    controlledDummies.add(dummy.uuid);

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

        ImGui.end();
    }
}
