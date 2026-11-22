package teddy.director.client.screen.window.timeline;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import teddy.director.client.DirectorState;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.AbstractKeyframe;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.ImGuiColors;
import teddy.director.client.utils.NameInputModal;

import java.util.List;

public class TimelineTracks {
    private static final float TRACK_HEIGHT = 26;
    private static final float TRACK_NAME_PADDING = 5;
    private static final float KEYFRAME_HALF_WIDTH = 6;
    private static final float KEYFRAME_HALF_HEIGHT = 6;

    private static final float MS_LINE_HEIGHT = 6;
    private static final float SECOND_LINE_HEIGHT = 10;

    private final ConfirmModal deleteTrackModal = new ConfirmModal("Delete Track");
    private final NameInputModal renameTrackModal = new NameInputModal("Rename Track");

    public float startPosX = 0f;
    public float endPosY = 0f;
    public int rightClickMenuTrackIdx = -1;
    private AbstractKeyframe selectedKeyframe = null;

    public TimelineTracks() {}

    public void reset() {
        rightClickMenuTrackIdx = -1;
        selectedKeyframe = null;
    }

    public void draw(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        drawTracks(dl, windowOrigin, windowSize);
        drawMillis(dl, windowOrigin, windowSize);

        if (selectedKeyframe != null)
            selectedKeyframe.drawImGuiWindow();

        if (rightClickMenuTrackIdx >= 0)
            DirectorState.getTrack(rightClickMenuTrackIdx).drawEditWindow();

        renameTrackModal.draw(name -> DirectorState.renameTrack(rightClickMenuTrackIdx, name));

        if (ImGui.beginPopup("track_right_click_menu")) {
            if (ImGui.selectable("Add keyframe"))
                createKeyframe(TimelineWindow.xToMs(ImGui.getWindowPos().x));
            else if (ImGui.selectable("Add keyframe at playhead"))
                createKeyframe(TimelineWindow.playhead.getMs());

            ImGui.endPopup();
        }

        boolean openDeleteTrackModal = false;
        boolean openRenameTrackModal = false;
        if (ImGui.beginPopup("track_name_right_click_menu")) {
            if (ImGui.selectable("Edit")) {
                AbstractTrack<?> track = DirectorState.currentTimeline.tracks.get(rightClickMenuTrackIdx);
                track.showEditWindow();
            }

            ImGui.separator();

            if (ImGui.selectable("Rename"))
                openRenameTrackModal = true;
            else if (ImGui.selectable("Delete"))
                openDeleteTrackModal = true;

            ImGui.endPopup();
        }

        if (openRenameTrackModal) renameTrackModal.open();
        if (openDeleteTrackModal) deleteTrackModal.open();

        if (rightClickMenuTrackIdx != -1) {
            String deleteTrackTitle = "Are you sure you want to delete track \"%s\" ?"
                    .formatted(DirectorState.getTrackName(rightClickMenuTrackIdx));

            deleteTrackModal.draw(deleteTrackTitle, confirmed -> {
                if (!confirmed)
                    return;

                DirectorState.deleteTrack(rightClickMenuTrackIdx);
                rightClickMenuTrackIdx = -1;
            });
        }
    }

    private void drawTracks(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        // Drawing the tracks
        startPosX = windowOrigin.x;
        float trackY = windowOrigin.y + TimelinePlayhead.DRAGGABLE_AREA_HEIGHT;
        dl.addLine(
                windowOrigin.x, trackY,
                windowOrigin.x + windowSize.x, trackY,
                ImGuiColors.TRACKS_GRAY
        );

        List<AbstractTrack<?>> timelineTracks = DirectorState.currentTimeline.tracks;
        for (AbstractTrack<?> track : timelineTracks) {
            ImVec2 nameSize = ImGui.calcTextSize(track.name);
            startPosX = Math.max(windowOrigin.x + nameSize.x, startPosX);
        }
        startPosX += TRACK_NAME_PADDING * 2;

        for (int i = 0; i < timelineTracks.size(); i++) {
            AbstractTrack<?> track = timelineTracks.get(i);
            drawTrack(track, trackY, dl, windowOrigin, windowSize);
            trackY += TRACK_HEIGHT;
            handleTrackRightClickMenu(i, trackY, windowOrigin, windowSize);
        }

        // Apply left and right padding of the names
        endPosY = trackY;

        trackY = windowOrigin.y + TimelinePlayhead.DRAGGABLE_AREA_HEIGHT;
        for (AbstractTrack<?> track : DirectorState.currentTimeline.tracks) {
            for (AbstractKeyframe kf : track.getKeyframes().values()) {
                drawKeyframe(kf, trackY, dl, windowOrigin, windowSize);
                handleKeyframeSelectAndDrag();
            }

            trackY += TRACK_HEIGHT;
        }

        // Track vertical closing lines
        dl.addLine(
                startPosX, windowOrigin.y + TimelinePlayhead.DRAGGABLE_AREA_HEIGHT,
                startPosX, trackY,
                ImGuiColors.TRACKS_GRAY
        );
    }

    private void drawTrack(AbstractTrack<?> track, float trackY, ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        ImVec2 nameSize = ImGui.calcTextSize(track.name);

        // Place the text in the middle
        float textPlaceY = trackY + (TRACK_HEIGHT / 2f) - (nameSize.y / 2f);
        // Name text
        dl.addText(
                windowOrigin.x + TRACK_NAME_PADDING, textPlaceY,
                ImGuiColors.WHITE,
                track.name
        );

        trackY += TRACK_HEIGHT;
        // Bottom line
        dl.addLine(
                windowOrigin.x, trackY,
                windowOrigin.x + windowSize.x, trackY,
                ImGuiColors.TRACKS_GRAY
        );
    }

    private void handleTrackRightClickMenu(int idx, float trackY, ImVec2 windowOrigin, ImVec2 windowSize) {
        boolean mouseIsInTrack = TimelineWindow.mouseIsInRect(
                startPosX, trackY - TRACK_HEIGHT,
                windowSize.x, trackY
        );

        boolean mouseIsInTrackName = TimelineWindow.mouseIsInRect(
                windowOrigin.x, trackY - TRACK_HEIGHT,
                startPosX, trackY
        );

        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            if (mouseIsInTrack) {
                ImGui.openPopup("track_right_click_menu");
                rightClickMenuTrackIdx = idx;
            }
            else if (mouseIsInTrackName) {
                ImGui.openPopup("track_name_right_click_menu");
                rightClickMenuTrackIdx = idx;
            }
        }
    }

    private void drawKeyframe(AbstractKeyframe kf, float trackY, ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        float kfX = TimelineWindow.msToX(kf.ms);
        if (kfX <= startPosX || kfX > windowOrigin.x + windowSize.x)
            return;

        float trackMiddle = trackY + TRACK_HEIGHT / 2;
        dl.addQuadFilled(
                kfX, trackMiddle - KEYFRAME_HALF_HEIGHT,   // top
                kfX + KEYFRAME_HALF_WIDTH, trackMiddle,    // right
                kfX, trackMiddle + KEYFRAME_HALF_HEIGHT,   // bottom
                kfX - KEYFRAME_HALF_WIDTH, trackMiddle,    // left
                ImGuiColors.WHITE
        );
    }

    private void handleKeyframeSelectAndDrag() {

    }

    private void drawMillis(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        long numLines = TimelineWindow.scrollMs;
        float separation = TimelineWindow.tickSeparation();

        for (int i = 0; ; i++) {
            float drawX = TimelineWindow.tracks.startPosX + i * separation;
            if (drawX >= windowOrigin.x + windowSize.x)
                break;

            if (numLines % 100 != 0)
            {
                numLines++;
                continue;
            }

            float lineHeight;
            if (numLines % 1000 == 0) {
                lineHeight = SECOND_LINE_HEIGHT;

                String secondsString = String.valueOf(numLines / 1000);
                ImVec2 textDims = ImGui.calcTextSize(secondsString);

                dl.addText(
                        drawX - (textDims.x / 2), windowOrigin.y + lineHeight - textDims.y - 5,
                        ImGuiColors.TRACKS_GRAY,
                        secondsString
                );
            }
            else {
                lineHeight = MS_LINE_HEIGHT;
            }

            float drawYBase = windowOrigin.y + TimelinePlayhead.DRAGGABLE_AREA_HEIGHT;
            dl.addLine(
                    drawX, drawYBase,
                    drawX, drawYBase - lineHeight,
                    ImGuiColors.TRACKS_GRAY
            );

            numLines++;
        }
    }

    private void createKeyframe(long ms) {
        AbstractTrack<?> track = DirectorState.getTrack(rightClickMenuTrackIdx);
        AbstractKeyframe kf = track.createKeyframe(ms);
        openKeyframeWindow(kf);
    }

    private void openKeyframeWindow(AbstractKeyframe kf) {
        selectedKeyframe = kf;
        selectedKeyframe.openImGuiWindow();
    }
}
