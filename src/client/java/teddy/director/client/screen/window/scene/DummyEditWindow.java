package teddy.director.client.screen.window.scene;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.data.DummyData;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.utils.FilteredCombo;
import teddy.director.client.utils.ImGuiColors;

public class DummyEditWindow {
    private final FilteredCombo entityTypeCombo = new FilteredCombo("Display Entity", "Select a display entity...");

    private final ImBoolean dummyEditWindowOpen = new ImBoolean(false);
    private final ImString dummyRenameImString = new ImString();

    private boolean dummyEditWindowWasOpened = false;
    private boolean hasEmptyRenameDummyStrErr = false;

    private static EntityType<?> selectedEntityType = null;
    private DummyData selectedDummy = null;

    public DummyEditWindow() {}

    public void draw() {
        if (!dummyEditWindowOpen.get() || selectedDummy == null)
            return;

        // TODO : special case for a player to set the skin etc

        ImGui.begin("Edit Dummy", dummyEditWindowOpen);

        ImGui.separatorText("Info");

        ImGui.inputText("Name", dummyRenameImString);
        if (hasEmptyRenameDummyStrErr)
            ImGui.textColored(ImGuiColors.ERROR_TEXT, "Please enter a name");

        ImGui.text("UUID: %s".formatted(selectedDummy.uuid.toString()));

        int selectedEntityId = drawEntityTypeCombo();
        if (selectedEntityId >= 0)
            selectedEntityType = BuiltInRegistries.ENTITY_TYPE.byId(selectedEntityId);

        Vec3 pos = selectedDummy.pos;
        ImGui.text("Original position: (%.2f, %.2f, %.2f)".formatted(pos.x, pos.y, pos.z));

        ImGui.separator();

        if (ImGui.button("Done")) {
            hasEmptyRenameDummyStrErr = dummyRenameImString.isEmpty();

            if (!hasEmptyRenameDummyStrErr) {
                selectedDummy.name = dummyRenameImString.get();

                String newEntityId = BuiltInRegistries.ENTITY_TYPE.getKey(selectedEntityType).toString();

                if (!newEntityId.equals(selectedDummy.displayEntityID)) {
                    ScenePlaybackController.removeDummyEntity(selectedDummy.uuid);

                    selectedDummy.displayEntityID = newEntityId;

                    ScenePlaybackController.summonDummy(selectedDummy);
                }

                close();

            }
        }

        ImGui.sameLine();

        if (ImGui.button("Cancel"))
            close();

        ImGui.end();

        if (dummyEditWindowWasOpened && !dummyEditWindowOpen.get())
            close();

        dummyEditWindowWasOpened = dummyEditWindowOpen.get();
    }

    public void close() {
        entityTypeCombo.reset();
        dummyEditWindowOpen.set(false);
        dummyRenameImString.clear();

        hasEmptyRenameDummyStrErr = false;

        selectedEntityType = null;
        selectedDummy = null;
    }

    public void open(DummyData dummyData) {
        dummyEditWindowOpen.set(true);
        selectedDummy = dummyData;
        dummyRenameImString.set(dummyData.name);

        Identifier id = Identifier.parse(dummyData.displayEntityID);
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        int idx = BuiltInRegistries.ENTITY_TYPE.getId(entityType);

        entityTypeCombo.setSelectedItemIdx(idx);
    }

    private int drawEntityTypeCombo() {
        return entityTypeCombo.draw(BuiltInRegistries.ENTITY_TYPE.size(), i -> {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(i);
            if (!DefaultAttributes.hasSupplier(entityType))
                return "";

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

            return id.toString();
        });
    }
}
