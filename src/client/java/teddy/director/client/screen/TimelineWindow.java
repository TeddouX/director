package teddy.director.client.screen;

import imgui.*;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.TimelineData;

public class TimelineWindow {
    private static final int WHITE_COLOR = ImColor.rgba(255, 255, 255, 255);

    private static final int PLAYHEAD_COLOR = ImColor.rgba(255, 80, 80, 255);
    private static final float PLAYHEAD_DRAGGABLE_AREA_HEIGHT = 15;
    private static final float PLAYHEAD_WIDTH = 2;
    private static boolean isMovingPlayhead = false;

    private static final int TRACK_SEPARATOR_COLOR = ImColor.rgba(50, 50, 50, 255);
    private static final float TRACK_HEIGHT = 26;
    private static final float TRACK_NAME_PADDING = 5;
    private static float trackStartPosX = 0;
    private static float trackEndPosY = 0;
    private static int playheadTick = 0;

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
        ImGui.begin("Timeline");

        if (DirectorState.CURRENT_MOVIE == null) {
            ImGui.text("Please load a movie to edit a timeline");
            ImGui.end();
            return;
        }

        ImGuiIO io = ImGui.getIO();
        ImDrawList dl = ImGui.getWindowDrawList();
        ImVec2 origin = ImGui.getCursorScreenPos();
        ImVec2 size = ImGui.getContentRegionAvail();

        ImGui.invisibleButton("##timeline", size);
        hovered = ImGui.isItemHovered();
        mousePos = io.getMousePos();

        handleTimelineScrollAndZoom();

        handleTracks(dl, origin, size);
        drawTicks(dl, origin, size);
        handlePlayhead(dl, origin, size);

        ImGui.end();
    }

    private static void handlePlayhead(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        // Moving the playhead logic

        if (isMovingPlayhead && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            isMovingPlayhead = false;
            ImGui.setMouseCursor(ImGuiMouseCursor.Arrow);
        }

        ImVec2 playheadDraggableAreaUL = new ImVec2(trackStartPosX, windowOrigin.y);
        ImVec2 playheadDraggableAreaDR = new ImVec2(
                windowSize.x,
                PLAYHEAD_DRAGGABLE_AREA_HEIGHT
        ).plus(windowOrigin);

        boolean mouseIsInDraggableArea = mouseIsInRect(playheadDraggableAreaUL, playheadDraggableAreaDR);

        if (isMovingPlayhead || (hovered && mouseIsInDraggableArea)) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW);

            if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                isMovingPlayhead = true;

                if (mousePos.x >= playheadDraggableAreaUL.x
                        && mousePos.x <= playheadDraggableAreaDR.x)
                    playheadTick = Math.max(scrollTick, xToTick(mousePos.x));
            }
        }

        // Drawing the playhead

        float playheadX = tickToX(playheadTick);
        if (playheadX >= trackStartPosX && playheadX <= windowOrigin.x + windowSize.x)
        {
            dl.addLine(
                    new ImVec2(playheadX, windowOrigin.y),
                    new ImVec2(playheadX, trackEndPosY),
                    PLAYHEAD_COLOR,
                    PLAYHEAD_WIDTH
            );

            dl.addTriangleFilled(
                    new ImVec2(playheadX - 6, windowOrigin.y),
                    new ImVec2(playheadX + 6, windowOrigin.y),
                    new ImVec2(playheadX, windowOrigin.y + 10),
                    PLAYHEAD_COLOR
            );
        }
    }

    private static void handleTracks(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        // Drawing the tracks

        // TEMP
        TimelineData.Track[] tracks = {
                new TimelineData.Track("Hello"),
                new TimelineData.Track("Hello1"),
                new TimelineData.Track("Hello2"),
                new TimelineData.Track("Hello3"),
                new TimelineData.Track("Hello4"),
        };

        // Drawing the tracks
        trackStartPosX = windowOrigin.x;
        float trackY = windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT;
        dl.addLine(
                new ImVec2(windowOrigin.x, trackY),
                new ImVec2(windowOrigin.x + windowSize.x, trackY),
                TRACK_SEPARATOR_COLOR
        );

        int rightClickMenuTrackIdx = -1;
        for (int i = 0; i < tracks.length; i++) {
            TimelineData.Track track = tracks[i];
            ImVec2 nameSize = ImGui.calcTextSize(track.name);
            trackStartPosX = Math.max(windowOrigin.x + nameSize.x, trackStartPosX);

            // Place the text in the middle
            float textPlaceY = trackY + (TRACK_HEIGHT / 2f) - (nameSize.y / 2f);
            // Name text
            dl.addText(
                    new ImVec2(windowOrigin.x + TRACK_NAME_PADDING, textPlaceY),
                    WHITE_COLOR,
                    track.name
            );

            trackY += TRACK_HEIGHT;
            // Bottom line
            dl.addLine(
                    new ImVec2(windowOrigin.x, trackY),
                    new ImVec2(windowOrigin.x + windowSize.x, trackY),
                    TRACK_SEPARATOR_COLOR
            );

            boolean mouseIsInTrack = mouseIsInRect(new ImVec2(trackStartPosX, trackY - TRACK_HEIGHT), new ImVec2(windowSize.x, trackY));
            if (ImGui.isMouseClicked(ImGuiMouseButton.Right) && mouseIsInTrack) {
                ImGui.openPopup("track_right_click_menu");
                rightClickMenuTrackIdx = i;
            }
        }

        // Apply left and right padding of the names
        trackStartPosX += TRACK_NAME_PADDING * 2;
        trackEndPosY = trackY;

        // Track vertical closing lines
        dl.addLine(
                new ImVec2(trackStartPosX, windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT),
                new ImVec2(trackStartPosX, trackY),
                TRACK_SEPARATOR_COLOR
        );

        dl.addLine(
                new ImVec2(windowOrigin.x, windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT),
                new ImVec2(windowOrigin.x, trackY),
                TRACK_SEPARATOR_COLOR
        );

        if (ImGui.beginPopup("track_right_click_menu")) {
            if (ImGui.selectable("Add keyframe"))
                Director.LOGGER.info("Add keyframe {}", rightClickMenuTrackIdx);
            else if (ImGui.selectable("Add keyframe at playhead"))
                Director.LOGGER.info("Add keyframe at playhead {}", rightClickMenuTrackIdx);

            ImGui.endPopup();
        }
    }

    private static void drawTicks(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        int numLines = scrollTick;
        float zoomedSeparation = TICK_BASE_PIXEL_SEPARATION_SIZE / zoomValue;

        for (int i = 0; ; i++) {
            float drawX = trackStartPosX + i * zoomedSeparation;
            if (drawX >= windowOrigin.x + windowSize.x)
                break;

            float lineHeight;
            if (numLines % 20 == 0) {
                lineHeight = SECOND_LINE_HEIGHT;

                String secondsString = String.valueOf(numLines / 20);
                ImVec2 textDims = ImGui.calcTextSize(secondsString);

                dl.addText(
                        new ImVec2(drawX - (textDims.x / 2), windowOrigin.y + lineHeight - textDims.y - 5),
                        TRACK_SEPARATOR_COLOR,
                        secondsString
                );
            }
            else {
                lineHeight = TICK_LINE_HEIGHT;
            }

            float drawYBase = windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT;
            dl.addLine(
                    new ImVec2(drawX, drawYBase),
                    new ImVec2(drawX, drawYBase - lineHeight),
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
            float rawScroll = mouseWheel * 0.2f;
            zoomValue -= rawScroll;
            zoomValue = Math.max(0.1f, zoomValue);
        }
        else {
            int rawScroll = (int) (mouseWheel * SCROLL_TICK_SENS);
            scrollTick += rawScroll;
            scrollTick = Math.max(0, scrollTick);
        }
    }

    private static boolean mouseIsInRect(ImVec2 rectUL, ImVec2 rectDR) {
        return mousePos.x >= rectUL.x && mousePos.y >= rectUL.y &&
                mousePos.x <= rectDR.x && mousePos.y <= rectDR.y;
    }

    private static float tickSeparation() {
        return TICK_BASE_PIXEL_SEPARATION_SIZE / zoomValue;
    }

    private static float tickToX(int tick) {
        return trackStartPosX + (tick - scrollTick) * tickSeparation();
    }

    private static int xToTick(float screenX) {
        return Math.round((screenX - trackStartPosX) / tickSeparation()) + scrollTick;
    }
}
