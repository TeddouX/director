package teddy.director.client.screen.window.scene;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import teddy.director.client.DirectorState;
import teddy.director.client.utils.FilteredCombo;
import teddy.director.client.utils.ImGuiColors;

public class CreateDummyPopup {
    private final FilteredCombo entityTypeCombo = new FilteredCombo("Display Entity", "Select a display entity...");

    private final String title;

    private final ImString dummyNameImString = new ImString();
    private EntityType<?> selectedEntityType = null;

    private boolean hasEmptyNameErr = false;
    private boolean hasNotSelectedEntityType = false;


    public CreateDummyPopup(String title) {
        this.title = title;
    }

    public void open() {
        ImGui.openPopup(title);
    }

    public void draw() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(title, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();
            ImGui.inputText("Name", dummyNameImString);

            if (hasEmptyNameErr)
                ImGui.textColored(ImGuiColors.ERROR_TEXT, "Please input a track name");

            int selectedEntityId = drawEntityTypeCombo();
            if (selectedEntityId >= 0)
                selectedEntityType = BuiltInRegistries.ENTITY_TYPE.byId(selectedEntityId);

            ImGui.separator();

            if (ImGui.button("Create")) {
                hasEmptyNameErr = dummyNameImString.isEmpty();
                hasNotSelectedEntityType = selectedEntityType == null;

                if (!hasEmptyNameErr && !hasNotSelectedEntityType) {
                    DirectorState.createAndSummonDummy(dummyNameImString.get(), selectedEntityType);

                    reset();
                    ImGui.closeCurrentPopup();
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel")) {
                reset();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    public void reset() {
        entityTypeCombo.reset();
        dummyNameImString.clear();

        selectedEntityType = null;

        hasEmptyNameErr = false;
        hasNotSelectedEntityType = false;
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
