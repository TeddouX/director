package teddy.director.client.screen.window;

import imgui.*;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import imgui.type.ImBoolean;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.track.AbstractTrack;
import teddy.director.client.data.track.keyframe.AbstractKeyframe;

import java.util.List;

public class TimelineWindow {
    private static final int WHITE_COLOR = ImColor.rgba(255, 255, 255, 255);

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
    private static final ImBoolean keyframeWindowOpen = new ImBoolean(false);
    private static AbstractKeyframe keyframeWindowKeyframe = null;

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
        if (ImGui.begin("Timeline")) {
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

            drawKeyframeWindow();
        }

        ImGui.end();
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

        for (int i = 0; i < timelineTracks.size(); i++) {
            AbstractTrack track = timelineTracks.get(i);
            ImVec2 nameSize = ImGui.calcTextSize(track.name);
            trackStartPosX = Math.max(windowOrigin.x + nameSize.x, trackStartPosX);

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

            if (ImGui.isMouseClicked(ImGuiMouseButton.Right) && mouseIsInTrack) {
                ImGui.openPopup("track_right_click_menu");
                rightClickMenuTrackIdx = i;
            }
        }

        // Apply left and right padding of the names
        trackStartPosX += TRACK_NAME_PADDING * 2;
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

        dl.addLine(
                windowOrigin.x, windowOrigin.y + PLAYHEAD_DRAGGABLE_AREA_HEIGHT,
                windowOrigin.x, trackY,
                TRACK_SEPARATOR_COLOR
        );

        if (ImGui.beginPopup("track_right_click_menu")) {
            if (ImGui.selectable("Add keyframe")) {
                Director.LOGGER.info("Added keyframe");
            }
            else if (ImGui.selectable("Add keyframe at playhead")) {
                Director.LOGGER.info("Added keyframe at playhead");
            }

            ImGui.endPopup();
        }
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
        keyframeWindowOpen.set(true);
        keyframeWindowKeyframe = kf;
    }

    private static void drawKeyframeWindow() {
        if (!keyframeWindowOpen.get())
            return;

        ImGui.begin("Keyframe", keyframeWindowOpen);
        ImGui.textColored(255, 0, 0, 255, "WIP");
        ImGui.text(String.format("Tick: %d", keyframeWindowKeyframe.tick));
        ImGui.end();
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
