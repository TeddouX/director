package teddy.director.client.screen.window.timeline;

import imgui.*;
import imgui.flag.*;
import imgui.type.ImString;
import teddy.director.client.DirectorState;
import teddy.director.client.data.track.TrackRegistry;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.utils.ImGuiColors;

import java.util.List;

public class TimelineWindow {
    public static TimelinePlayhead playhead = new TimelinePlayhead();
    public static TimelineTracks tracks = new TimelineTracks();

    private static final String CREATE_TRACK_MODAL_NAME = "Create Track";
    private static final ImString createTrackNameInput = new ImString();
    private static int currentSelectedTrackTypeIdx = -1;
    private static boolean hasEmptyCreateTrackNameInput = false;
    private static boolean hasNotSelectedTrackType = false;

    private static final float MS_BASE_PIXEL_SEPARATION_SIZE = 4;
    private static final float SCROLL_MS_SENS = 100f;
    private static final float ZOOM_SENS = 0.2f;

    public static long scrollMs = 0;
    public static float zoomValue = 34;

    public static boolean hovered = false;
    public static ImVec2 mousePos = new ImVec2();

    public static void draw() {
        int windowFlags = 0;

        if (DirectorState.currentTimeline != null)
            windowFlags |= ImGuiWindowFlags.MenuBar;

        if (ImGui.begin("Timeline", null, windowFlags)) {
            if (DirectorState.currentMovie == null) {
                ImGui.text("Please load a movie to edit a timeline");
                ImGui.end();
                return;
            }

            if (DirectorState.currentScene == null) {
                ImGui.text("Please select a scene to edit a timeline");
                ImGui.end();
                return;
            }

            if (DirectorState.currentTimeline == null) {
                ImGui.text("Please select a timeline to edit");
                ImGui.end();
                return;
            }

            drawMenuBar();

            ImGuiIO io = ImGui.getIO();
            ImDrawList dl = ImGui.getWindowDrawList();
            ImVec2 origin = ImGui.getCursorScreenPos();
            ImVec2 size = ImGui.getContentRegionAvail();

            ImGui.setNextItemAllowOverlap();
            ImGui.invisibleButton("##timeline", size);
            hovered = ImGui.isItemHovered();
            mousePos = io.getMousePos();

            handleTimelineScrollAndZoom();

            tracks.draw(dl, origin, size);
            playhead.draw(dl, origin, size);

            drawCreateTrackModal();
        }

        ImGui.end();
    }

    public static void reset() {
        playhead.reset();
        tracks.reset();

        scrollMs = 0;
        zoomValue = 34;
    }

    private static void drawMenuBar() {
        boolean openCreateTrackPopup = false;
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("Playback")) {
                if (ImGui.menuItem("Start", "Ctrl+Space"))
                    ScenePlaybackController.startPlayback(playhead.getMs());

                if (ImGui.menuItem("Pause", "Ctrl+Space"))
                    ScenePlaybackController.pausePlayback();

                if (ImGui.menuItem("Beginning", "Ctrl+Left Arrow"));

                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Track")) {
                if (ImGui.menuItem("New"))
                    openCreateTrackPopup = true;

                ImGui.endMenu();
            }

            ImGui.endMenuBar();
        }

        if (openCreateTrackPopup)
            ImGui.openPopup(CREATE_TRACK_MODAL_NAME);
    }

    private static void handleTimelineScrollAndZoom() {
        if (!hovered)
            return;

        float mouseWheel = ImGui.getIO().getMouseWheel();
        if (ImGui.isKeyDown(ImGuiKey.LeftCtrl)) {
            float rawScroll = mouseWheel * ZOOM_SENS;
            zoomValue -= rawScroll;
            zoomValue = Math.max(0.1f, zoomValue);
        }
        else {
            int rawScroll = (int) (mouseWheel * SCROLL_MS_SENS);
            scrollMs -= rawScroll;
            scrollMs = Math.max(0, scrollMs);
        }
    }

    private static void drawCreateTrackModal() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(CREATE_TRACK_MODAL_NAME, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();
            ImGui.inputText("Name", createTrackNameInput);

            if (hasEmptyCreateTrackNameInput)
                ImGui.textColored(ImGuiColors.ERROR_TEXT, "Please input a track name");

            List<String> trackTypes = TrackRegistry.getAllTrackTypeIDs();
            String preview = currentSelectedTrackTypeIdx >= 0 ? trackTypes.get(currentSelectedTrackTypeIdx) : "Select a track type...";

            if (ImGui.beginCombo("##timeline_combo", preview)) {
                for (int i = 0; i < trackTypes.size(); i++) {
                    String trackTypeId = trackTypes.get(i);
                    boolean selected = i == currentSelectedTrackTypeIdx;
                    if (ImGui.selectable(trackTypeId + "###" + i, selected))
                        currentSelectedTrackTypeIdx = i;
                }
                ImGui.endCombo();
            }

            if (hasNotSelectedTrackType)
                ImGui.textColored(ImGuiColors.ERROR_TEXT, "Please select a track type");

            ImGui.separator();

            if (ImGui.button("Create")) {
                hasEmptyCreateTrackNameInput = createTrackNameInput.isEmpty();
                hasNotSelectedTrackType = currentSelectedTrackTypeIdx == -1;

                if (!hasEmptyCreateTrackNameInput && !hasNotSelectedTrackType) {
                    DirectorState.createAndAddTrack(
                            createTrackNameInput.get(),
                            TrackRegistry.getAllTrackTypeIDs().get(currentSelectedTrackTypeIdx)
                    );

                    resetCreateTrackModalData();
                    ImGui.closeCurrentPopup();
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel")) {
                resetCreateTrackModalData();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private static void resetCreateTrackModalData() {
        currentSelectedTrackTypeIdx = -1;
        createTrackNameInput.clear();
    }

    public static boolean mouseIsInRect(float rectULx, float rectULy, float rectDRx, float rectDRy) {
        return mousePos.x >= rectULx && mousePos.y >= rectULy &&
                mousePos.x <= rectDRx && mousePos.y <= rectDRy;
    }

    public static float tickSeparation() {
        return MS_BASE_PIXEL_SEPARATION_SIZE / zoomValue;
    }

    public static float msToX(long ms) {
        return tracks.startPosX + (ms - scrollMs) * tickSeparation();
    }

    public static long xToMs(float screenX) {
        return (long) Math.floor((screenX - tracks.startPosX) / tickSeparation()) + scrollMs;
    }
}
