package teddy.director.client.screen.window;

import imgui.*;
import imgui.flag.*;
import imgui.type.ImString;
import teddy.director.client.DirectorState;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.TrackRegistry;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.NameInputModal;

import java.util.List;

public class TimelineWindow {
    private static final int WHITE_COLOR = ImColor.rgba(255, 255, 255, 255);
    private static final int ERROR_TEXT_COLOR = ImColor.rgba(245, 13, 13, 255);

    private static final int PLAYHEAD_COLOR = ImColor.rgba(255, 80, 80, 255);
    private static final float PLAYHEAD_DRAGGABLE_AREA_HEIGHT = 15;
    private static final float PLAYHEAD_WIDTH = 2;
    private static boolean isMovingPlayhead = false;
    private static int playheadTick = 0;

    private static final int TRACK_SEPARATOR_COLOR = ImColor.rgba(50, 50, 50, 255);
    private static final int KEYFRAME_SELECT_COLOR = ImColor.rgba(50, 80, 255, 255);
    private static final float TRACK_HEIGHT = 26;
    private static final float TRACK_NAME_PADDING = 5;
    private static final float KEYFRAME_HALF_WIDTH = 6;
    private static final float KEYFRAME_HALF_HEIGHT = 6;
    private static float trackStartPosX = 0;
    private static float trackEndPosY = 0;
    private static int rightClickMenuTrackIdx = -1;
    private static AbstractKeyframe selectedKeyframe = null;
    private static final ConfirmModal deleteTrackModal = new ConfirmModal("Delete Track");
    private static final NameInputModal renameTrackModal = new NameInputModal("Rename Track");

    private static final String CREATE_TRACK_MODAL_NAME = "Create Track";
    private static final ImString createTrackNameInput = new ImString();
    private static int currentSelectedTrackType = -1;
    private static boolean hasEmptyCreateTrackNameInput = false;
    private static boolean hasNotSelectedTrackType = false;

    private static final float TICK_BASE_PIXEL_SEPARATION_SIZE = 4;
    private static final float TICK_LINE_HEIGHT = 6;
    private static final float SECOND_LINE_HEIGHT = 10;

    private static final float SCROLL_TICK_SENS = 4f;
    private static int scrollTick = 0;

    private static final float ZOOM_SENS = 0.2f;
    private static float zoomValue = 1;

    private static boolean hovered = false;
    private static ImVec2 mousePos = new ImVec2();

    public static void draw() {
        int windowFlags = 0;

        boolean hasTimeline = DirectorState.CURRENT_TIMELINE != null;
        if (hasTimeline)
            windowFlags |= ImGuiWindowFlags.MenuBar;

        if (ImGui.begin("Timeline", null, windowFlags)) {
            if (DirectorState.CURRENT_MOVIE == null) {
                ImGui.text("Please load a movie to edit a timeline");
                ImGui.end();
                return;
            }

            if (DirectorState.CURRENT_SCENE == null) {
                ImGui.text("Please select a scene to edit a timeline");
                ImGui.end();
                return;
            }

            if (DirectorState.CURRENT_TIMELINE == null) {
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

            handleTracks(dl, origin, size);
            drawTicks(dl, origin, size);
            handlePlayhead(dl, origin, size);

            drawCreateTrackModal();
            renameTrackModal.draw(name -> DirectorState.renameTrack(rightClickMenuTrackIdx, name));

            if (selectedKeyframe != null)
                selectedKeyframe.drawImGuiWindow();
        }

        ImGui.end();
    }

    private static void drawMenuBar() {
        boolean openCreateTrackPopup = false;
        if (ImGui.beginMenuBar()) {
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

    private static void handlePlayhead(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        // Moving the playhead logic

        if (isMovingPlayhead && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            isMovingPlayhead = false;
            ImGui.setMouseCursor(ImGuiMouseCursor.Arrow);
        }

        boolean mouseIsInDraggableArea = mouseIsInRect(
                trackStartPosX, windowOrigin.y,
                windowSize.x + windowOrigin.x, PLAYHEAD_DRAGGABLE_AREA_HEIGHT + windowOrigin.y
        );

        if (isMovingPlayhead || (hovered && mouseIsInDraggableArea)) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW);

            if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                isMovingPlayhead = true;

                if (mousePos.x >= trackStartPosX && mousePos.x <= windowSize.x + windowOrigin.x)
                    playheadTick = xToTick(mousePos.x);
            }
        }

        // Drawing the playhead

        float playheadX = tickToX(playheadTick);
        if (playheadX >= trackStartPosX && playheadX <= windowOrigin.x + windowSize.x)
        {
            dl.addLine(
                    playheadX, windowOrigin.y,
                    playheadX, trackEndPosY,
                    PLAYHEAD_COLOR,
                    PLAYHEAD_WIDTH
            );

            dl.addTriangleFilled(
                    playheadX - 6, windowOrigin.y,
                    playheadX + 6, windowOrigin.y,
                    playheadX, windowOrigin.y + 10,
                    PLAYHEAD_COLOR
            );
        }
    }

    private static void handleTracks(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        // Drawing the tracks
        trackStartPosX = windowOrigin.x;
        float trackY = windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT;
        dl.addLine(
                windowOrigin.x, trackY,
                windowOrigin.x + windowSize.x, trackY,
                TRACK_SEPARATOR_COLOR
        );

        List<AbstractTrack> timelineTracks = DirectorState.CURRENT_TIMELINE.tracks;
        for (AbstractTrack track : timelineTracks) {
            ImVec2 nameSize = ImGui.calcTextSize(track.name);
            trackStartPosX = Math.max(windowOrigin.x + nameSize.x, trackStartPosX);
        }
        trackStartPosX += TRACK_NAME_PADDING * 2;

        for (int i = 0; i < timelineTracks.size(); i++) {
            AbstractTrack track = timelineTracks.get(i);
            ImVec2 nameSize = ImGui.calcTextSize(track.name);

            // Place the text in the middle
            float textPlaceY = trackY + (TRACK_HEIGHT / 2f) - (nameSize.y / 2f);
            // Name text
            dl.addText(
                    windowOrigin.x + TRACK_NAME_PADDING, textPlaceY,
                    WHITE_COLOR,
                    track.name
            );

            trackY += TRACK_HEIGHT;
            // Bottom line
            dl.addLine(
                    windowOrigin.x, trackY,
                    windowOrigin.x + windowSize.x, trackY,
                    TRACK_SEPARATOR_COLOR
            );

            boolean mouseIsInTrack = mouseIsInRect(
                    trackStartPosX, trackY - TRACK_HEIGHT,
                    windowSize.x, trackY
            );

            boolean mouseIsInTrackName = mouseIsInRect(
                    windowOrigin.x, trackY - TRACK_HEIGHT,
                    trackStartPosX, trackY
            );

            if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                if (mouseIsInTrack) {
                    ImGui.openPopup("track_right_click_menu");
                    rightClickMenuTrackIdx = i;
                }
                else if (mouseIsInTrackName) {
                    ImGui.openPopup("track_name_right_click_menu");
                    rightClickMenuTrackIdx = i;
                }
            }
        }

        // Apply left and right padding of the names
        trackEndPosY = trackY;

        trackY = windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT;
        for (AbstractTrack track : DirectorState.CURRENT_TIMELINE.tracks) {
            for (AbstractKeyframe kf : track.getKeyframes().values()) {
                float kfX = tickToX(kf.tick);
                // Do not draw if the keyframe is out of bounds
                if (kfX <= trackStartPosX || kfX > windowOrigin.x + windowSize.x)
                    continue;

                float trackMiddle = trackY + TRACK_HEIGHT / 2;
                // Draw a diamond for the keyframe
                dl.addQuadFilled(
                        kfX, trackMiddle - KEYFRAME_HALF_HEIGHT,   // top
                        kfX + KEYFRAME_HALF_WIDTH, trackMiddle,    // right
                        kfX, trackMiddle + KEYFRAME_HALF_HEIGHT,   // bottom
                        kfX - KEYFRAME_HALF_WIDTH, trackMiddle,    // left
                        WHITE_COLOR
                );

                boolean isHoveringKf = mouseIsInRect(
                        kfX - KEYFRAME_HALF_WIDTH, trackY,
                        kfX + KEYFRAME_HALF_HEIGHT, trackY + TRACK_HEIGHT
                );
                // Selecting and moving the keyframe
                if (isHoveringKf && ImGui.isMouseDown(ImGuiMouseButton.Left) && !isMovingPlayhead) {
                    openKeyframeWindow(kf);

                    kf.tick = xToTick(mousePos.x);

                    // Draw the selection outline
                    dl.addQuad(
                            kfX, trackMiddle - KEYFRAME_HALF_HEIGHT,   // top
                            kfX + KEYFRAME_HALF_WIDTH, trackMiddle,    // right
                            kfX, trackMiddle + KEYFRAME_HALF_HEIGHT,   // bottom
                            kfX - KEYFRAME_HALF_WIDTH, trackMiddle,    // left
                            KEYFRAME_SELECT_COLOR,
                            1f
                    );
                }
            }

            trackY += TRACK_HEIGHT;
        }

        // Track vertical closing lines
        dl.addLine(
                trackStartPosX, windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT,
                trackStartPosX, trackY,
                TRACK_SEPARATOR_COLOR
        );

        if (ImGui.beginPopup("track_right_click_menu")) {
            if (ImGui.selectable("Add keyframe"))
                createKeyframe(xToTick(ImGui.getWindowPos().x));
            else if (ImGui.selectable("Add keyframe at playhead"))
                createKeyframe(playheadTick);

            ImGui.endPopup();
        }

        boolean openDeleteTrackModal = false;
        boolean openRenameTrackModal = false;
        if (ImGui.beginPopup("track_name_right_click_menu")) {
            if (ImGui.selectable("Edit"));

            ImGui.separator();

            if (ImGui.selectable("Rename"))
                openRenameTrackModal = true;
            else if (ImGui.selectable("Delete"))
                openDeleteTrackModal = true;

            ImGui.endPopup();
        }

        if (openRenameTrackModal)
            renameTrackModal.open();

        if (openDeleteTrackModal)
            deleteTrackModal.open();

        if (rightClickMenuTrackIdx != -1) {
            String deleteTrackTitle = "Are you sure you want to delete track %s ?"
                    .formatted(DirectorState.getTrackName(rightClickMenuTrackIdx));

            deleteTrackModal.draw(deleteTrackTitle, confirmed -> {
                if (!confirmed)
                    return;

                DirectorState.deleteTrack(rightClickMenuTrackIdx);
                rightClickMenuTrackIdx = -1;
            });
        }
    }

    private static void createKeyframe(int tick) {
        AbstractTrack track = DirectorState.getTrack(rightClickMenuTrackIdx);
        AbstractKeyframe kf = track.createKeyframe(tick);

        openKeyframeWindow(kf);
    }

    private static void drawTicks(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        int numLines = scrollTick;
        float separation = tickSeparation();

        for (int i = 0; ; i++) {
            float drawX = trackStartPosX + i * separation;
            if (drawX >= windowOrigin.x + windowSize.x)
                break;

            float lineHeight;
            if (numLines % 20 == 0) {
                lineHeight = SECOND_LINE_HEIGHT;

                String secondsString = String.valueOf(numLines / 20);
                ImVec2 textDims = ImGui.calcTextSize(secondsString);

                dl.addText(
                        drawX - (textDims.x / 2), windowOrigin.y + lineHeight - textDims.y - 5,
                        TRACK_SEPARATOR_COLOR,
                        secondsString
                );
            }
            else {
                lineHeight = TICK_LINE_HEIGHT;
            }

            float drawYBase = windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT;
            dl.addLine(
                    drawX, drawYBase,
                    drawX, drawYBase - lineHeight,
                    TRACK_SEPARATOR_COLOR
            );

            numLines++;
        }
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
            int rawScroll = (int) (mouseWheel * SCROLL_TICK_SENS);
            scrollTick -= rawScroll;
            scrollTick = Math.max(0, scrollTick);
        }
    }

    private static void openKeyframeWindow(AbstractKeyframe kf) {
        selectedKeyframe = kf;
        selectedKeyframe.openImGuiWindow();
    }

    private static void drawCreateTrackModal() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(CREATE_TRACK_MODAL_NAME, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();
            ImGui.inputText("Name", createTrackNameInput);

            if (hasEmptyCreateTrackNameInput)
                ImGui.textColored(ERROR_TEXT_COLOR, "Please input a track name");

            List<String> trackTypes = TrackRegistry.getAllTrackTypeIDs();
            String preview = currentSelectedTrackType >= 0 ? trackTypes.get(currentSelectedTrackType) : "Select a track type...";

            if (ImGui.beginCombo("##timeline_combo", preview)) {
                for (int i = 0; i < trackTypes.size(); i++) {
                    String trackTypeId = trackTypes.get(i);
                    boolean selected = i == currentSelectedTrackType;
                    if (ImGui.selectable(trackTypeId + "###" + i, selected))
                        currentSelectedTrackType = i;
                }
                ImGui.endCombo();
            }

            if (hasNotSelectedTrackType)
                ImGui.textColored(ERROR_TEXT_COLOR, "Please select a track type");

            ImGui.separator();

            if (ImGui.button("Create")) {
                hasEmptyCreateTrackNameInput = createTrackNameInput.isEmpty();
                hasNotSelectedTrackType = currentSelectedTrackType == -1;

                if (!hasEmptyCreateTrackNameInput && !hasNotSelectedTrackType) {
                    DirectorState.createAndAddTrack(
                            createTrackNameInput.get(),
                            TrackRegistry.getAllTrackTypeIDs().get(currentSelectedTrackType)
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
        currentSelectedTrackType = -1;
        createTrackNameInput.clear();
    }

    private static boolean mouseIsInRect(float rectULx, float rectULy, float rectDRx, float rectDRy) {
        return mousePos.x >= rectULx && mousePos.y >= rectULy &&
                mousePos.x <= rectDRx && mousePos.y <= rectDRy;
    }

    private static float tickSeparation() {
        return TICK_BASE_PIXEL_SEPARATION_SIZE / zoomValue;
    }

    private static float tickToX(int tick) {
        return trackStartPosX + (tick - scrollTick) * tickSeparation();
    }

    private static int xToTick(float screenX) {
        return (int) Math.floor((screenX - trackStartPosX) / tickSeparation()) + scrollTick;
    }
}
