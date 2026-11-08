package teddy.director.client.screen.window;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.TimelineData;
import teddy.director.client.screen.window.utils.ConfirmModal;
import teddy.director.client.screen.window.utils.NameInputModal;

import java.util.List;

public class SceneWindow {
    private static final NameInputModal CREATE_TIMELINE_POPUP = new NameInputModal("Create Timeline");
    private static final NameInputModal RENAME_TIMELINE_MODAL = new NameInputModal("Rename Timeline");
    private static final ConfirmModal DELETE_TIMELINE_MODAL = new ConfirmModal("Delete Timeline");

    private static final ImGuiTextFilter TIMELINE_SELECT_FILTER = new ImGuiTextFilter();
    private static int currentSelectedTimelineIdx = -1;

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
        }
        ImGui.end();
    }

    public static void reset() {
        currentSelectedTimelineIdx = -1;
    }

    private static void drawDummiesControl() {
        ImGui.separatorText("Dummies");
    }

    private static void drawTimelinesControl() {
        ImGui.separatorText("Timelines");

        List<TimelineData> timelines = DirectorState.CURRENT_SCENE.timelines;

        if (ImGui.button("New timeline..."))
            CREATE_TIMELINE_POPUP.open();

        boolean openRenameModal = false;
        boolean openDeleteModal = false;
        String preview = currentSelectedTimelineIdx >= 0 ? timelines.get(currentSelectedTimelineIdx).name : "Select a timeline...";
        if (ImGui.beginCombo("##timeline_combo", preview)) {
            TIMELINE_SELECT_FILTER.draw("Search");

            for (int i = 0; i < timelines.size(); i++) {
                TimelineData timeline = timelines.get(i);
                if (!TIMELINE_SELECT_FILTER.passFilter(timeline.name))
                    continue;

                boolean selected = i == currentSelectedTimelineIdx;
                if (ImGui.selectable(timeline.name + "###" + i, selected)) {
                    currentSelectedTimelineIdx = i;
                    DirectorState.setCurrentTimeline(currentSelectedTimelineIdx);
                }

                if (ImGui.beginPopupContextItem()) {
                    currentSelectedTimelineIdx = i;
                    DirectorState.setCurrentTimeline(currentSelectedTimelineIdx);

                    if (ImGui.menuItem("Rename"))
                        openRenameModal = true;

                    if (ImGui.menuItem("Delete"))
                        openDeleteModal = true;

                    ImGui.endPopup();
                }
            }
            ImGui.endCombo();
        }

        if (openDeleteModal) DELETE_TIMELINE_MODAL.open();
        if (openRenameModal) RENAME_TIMELINE_MODAL.open();

        RENAME_TIMELINE_MODAL.draw(name -> DirectorState.renameTimeline(currentSelectedTimelineIdx, name));

        if (currentSelectedTimelineIdx >= 0) {
            String deleteTimelineTitle = "Are you sure you want to delete timeline %s ?"
                    .formatted(DirectorState.getTimelineName(currentSelectedTimelineIdx));

            DELETE_TIMELINE_MODAL.draw(deleteTimelineTitle, confirm -> {
                if (confirm)
                    DirectorState.deleteTimeline(currentSelectedTimelineIdx);
            });
        }

        CREATE_TIMELINE_POPUP.draw(name -> {
            TimelineData timelineData = new TimelineData(name);
            DirectorState.CURRENT_SCENE.timelines.add(timelineData);
            DirectorState.CURRENT_TIMELINE = timelineData;

            currentSelectedTimelineIdx = DirectorState.CURRENT_SCENE.timelines.size() - 1;

            Director.LOGGER.info("Created timeline {}", name);
        });
    }
}
