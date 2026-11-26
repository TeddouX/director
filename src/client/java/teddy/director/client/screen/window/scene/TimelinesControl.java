package teddy.director.client.screen.window.scene;

import imgui.ImGui;
import teddy.director.client.DirectorState;
import teddy.director.client.data.TimelineData;
import teddy.director.client.screen.window.timeline.TimelineWindow;
import teddy.director.client.utils.imgui.ConfirmModal;
import teddy.director.client.utils.imgui.FilteredCombo;
import teddy.director.client.utils.imgui.NameInputModal;

import java.util.List;

public class TimelinesControl {
    private final NameInputModal CREATE_TIMELINE_POPUP = new NameInputModal("Create Timeline");
    private final NameInputModal RENAME_TIMELINE_MODAL = new NameInputModal("Rename Timeline");
    private final ConfirmModal DELETE_TIMELINE_MODAL = new ConfirmModal("Delete Timeline");
    private final FilteredCombo TIMELINES_COMBO = new FilteredCombo("Timelines", "Select a timeline...");

    private int currentSelectedTimelineIdx = -1;
    private boolean openTimelineRenameModal = false;
    private boolean openTimelineDeleteModal = false;

    public TimelinesControl() {}

    public void reset() {
        TIMELINES_COMBO.reset();
        currentSelectedTimelineIdx = -1;
    }

    public void draw() {
        ImGui.separatorText("Timelines");

        List<TimelineData> timelines = DirectorState.currentScene.timelines;

        if (ImGui.button("New timeline..."))
            CREATE_TIMELINE_POPUP.open();

        if (DirectorState.currentTimeline != null) {
            currentSelectedTimelineIdx = DirectorState.currentTimelineIdx;
            TIMELINES_COMBO.setSelectedItemIdx(currentSelectedTimelineIdx);
        }

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
                    DirectorState.currentTimeline = null;
                    TimelineWindow.reset();

                    TIMELINES_COMBO.reset();
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
}
