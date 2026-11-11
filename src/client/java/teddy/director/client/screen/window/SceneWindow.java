package teddy.director.client.screen.window;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.FilteredCombo;
import teddy.director.client.utils.NameInputModal;

import java.util.List;

public class SceneWindow {
    private static final int ERROR_TEXT_COLOR = ImColor.rgba(245, 13, 13, 255);

    private static final NameInputModal CREATE_TIMELINE_POPUP = new NameInputModal("Create Timeline");
    private static final NameInputModal RENAME_TIMELINE_MODAL = new NameInputModal("Rename Timeline");
    private static final ConfirmModal DELETE_TIMELINE_MODAL = new ConfirmModal("Delete Timeline");

    private static final FilteredCombo TIMELINES_COMBO = new FilteredCombo("Timelines", "Select a timeline...");
    private static final FilteredCombo ENTITY_TYPE_COMBO = new FilteredCombo("Display Entity", "Select a display entity...");

    private static final String CREATE_DUMMY_POPUP_NAME = "Create Dummy";
    private static final ImString dummyNameImString = new ImString();
    private static boolean hasEmptyTrackNameError = false;
    private static boolean hasNotSelectedEntityType = false;
    private static EntityType<?> selectedEntityType = null;

    private static int currentSelectedTimelineIdx = -1;
    private static boolean openTimelineRenameModal = false;
    private static boolean openTimelineDeleteModal = false;

    private static final ImBoolean dummyEditWindowOpened = new ImBoolean(false);
    private static DummyData selectedDummy = null;
    private static final ImString dummyRenameImString = new ImString();
    private static boolean hasEmptyRenameDummyStrErr = false;

    public static void draw() {
        if (ImGui.begin("Scene")) {
            if (DirectorState.CURRENT_MOVIE == null) {
                ImGui.text("Please load a movie to edit a scene");
                ImGui.end();
                return;
            }

            if (DirectorState.CURRENT_SCENE == null) {
                ImGui.text("Please select a scene");
                ImGui.end();
                return;
            }

            ImGui.text("Current scene: %s".formatted(DirectorState.CURRENT_SCENE.name));

            drawDummiesControl();
            drawTimelinesControl();
            drawDummyEditWindow();
        }
        ImGui.end();
    }

    public static void reset() {
        TIMELINES_COMBO.reset();
        currentSelectedTimelineIdx = -1;

        TimelineWindow.reset();
    }

    private static void drawDummiesControl() {
        ImGui.separatorText("Dummies");

        if (ImGui.button("Create dummy..."))
            ImGui.openPopup(CREATE_DUMMY_POPUP_NAME);

        if (ImGui.treeNode("All Dummies")) {
            for (DummyData dummy : DirectorState.CURRENT_SCENE.dummies.values()) {
                if (ImGui.selectable(dummy.name)) {
                    dummyRenameImString.set(dummy.name);
                    selectedDummy = dummy;

                    Identifier id = Identifier.parse(dummy.displayEntityID);
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
                    int idx = BuiltInRegistries.ENTITY_TYPE.getId(entityType);

                    ENTITY_TYPE_COMBO.setSelectedItemIdx(idx);

                    dummyEditWindowOpened.set(true);
                }
            }

            ImGui.treePop();
        }

        drawCreateDummyPopup();
    }

    private static void drawCreateDummyPopup() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(CREATE_DUMMY_POPUP_NAME, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();
            ImGui.inputText("Name", dummyNameImString);

            if (hasEmptyTrackNameError)
                ImGui.textColored(ERROR_TEXT_COLOR, "Please input a track name");

            int selectedEntityId = drawEntityTypeCombo();
            if (selectedEntityId >= 0)
                selectedEntityType = BuiltInRegistries.ENTITY_TYPE.byId(selectedEntityId);

            ImGui.separator();

            if (ImGui.button("Create")) {
                hasEmptyTrackNameError = dummyNameImString.isEmpty();
                hasNotSelectedEntityType = selectedEntityType == null;

                if (!hasEmptyTrackNameError && !hasNotSelectedEntityType) {
                    DirectorState.createAndSummonDummy(dummyNameImString.get(), selectedEntityType);

                    resetCreateDummyPopupData();
                    ImGui.closeCurrentPopup();
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel")) {
                resetCreateDummyPopupData();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private static void resetCreateDummyPopupData() {
        hasEmptyTrackNameError = false;
        hasNotSelectedEntityType = false;
        ENTITY_TYPE_COMBO.reset();

        selectedEntityType = null;
        dummyNameImString.clear();
    }

    private static void drawTimelinesControl() {
        ImGui.separatorText("Timelines");

        List<TimelineData> timelines = DirectorState.CURRENT_SCENE.timelines;

        if (ImGui.button("New timeline..."))
            CREATE_TIMELINE_POPUP.open();

        int selected = TIMELINES_COMBO.draw(
                timelines.size(),
                i -> timelines.get(i).name,
                i -> {
                    if (ImGui.beginPopupContextItem()) {
                        TIMELINES_COMBO.setSelectedItemIdx(i);
                        currentSelectedTimelineIdx = i;

                        DirectorState.setCurrentTimeline(i);

                        if (ImGui.menuItem("Rename"))
                            openTimelineRenameModal = true;

                        if (ImGui.menuItem("Delete"))
                            openTimelineDeleteModal = true;

                        ImGui.endPopup();
                    }
                }
        );

        if (selected >= 0)
            DirectorState.setCurrentTimeline(selected);

        if (openTimelineDeleteModal) {
            DELETE_TIMELINE_MODAL.open();
            openTimelineDeleteModal = false;
        }

        if (openTimelineRenameModal) {
            RENAME_TIMELINE_MODAL.open();
            openTimelineRenameModal = false;
        }

        RENAME_TIMELINE_MODAL.draw(name -> DirectorState.renameTimeline(currentSelectedTimelineIdx, name));

        if (currentSelectedTimelineIdx >= 0) {
            String deleteTimelineTitle = "Are you sure you want to delete timeline \"%s\" ?"
                    .formatted(DirectorState.getTimelineName(currentSelectedTimelineIdx));

            DELETE_TIMELINE_MODAL.draw(deleteTimelineTitle, confirm -> {
                if (confirm) {
                    DirectorState.deleteTimeline(currentSelectedTimelineIdx);
                    DirectorState.CURRENT_TIMELINE = null;
                    TimelineWindow.reset();

                    TIMELINES_COMBO.setSelectedItemIdx(-1);
                    currentSelectedTimelineIdx = -1;
                }
            });
        }

        CREATE_TIMELINE_POPUP.draw(name -> {
            currentSelectedTimelineIdx = DirectorState.createAndAddTimeline(name);
            TIMELINES_COMBO.setSelectedItemIdx(currentSelectedTimelineIdx);
            DirectorState.setCurrentTimeline(currentSelectedTimelineIdx);
        });
    }

    private static void drawDummyEditWindow() {
        if (!dummyEditWindowOpened.get() || selectedDummy == null)
            return;

        // TODO : special case for a player to set the skin etc

        ImGui.begin("Edit Dummy", dummyEditWindowOpened);

        ImGui.separatorText("Info");

        ImGui.inputText("Name", dummyRenameImString);
        if (hasEmptyRenameDummyStrErr)
            ImGui.textColored(ERROR_TEXT_COLOR, "Please enter a name");

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

                resetDummyEditWindowData();

                dummyEditWindowOpened.set(false);
            }
        }

        ImGui.sameLine();

        if (ImGui.button("Cancel")) {
            resetDummyEditWindowData();

            dummyEditWindowOpened.set(false);
        }

        ImGui.end();
    }

    private static void resetDummyEditWindowData() {
        dummyRenameImString.clear();
        selectedDummy = null;
        selectedEntityType = null;
        ENTITY_TYPE_COMBO.setSelectedItemIdx(-1);
    }

    private static int drawEntityTypeCombo() {
        return ENTITY_TYPE_COMBO.draw(BuiltInRegistries.ENTITY_TYPE.size(), i -> {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(i);
            if (!DefaultAttributes.hasSupplier(entityType))
                return "";

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

            return id.toString();
        });
    }
}
