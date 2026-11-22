package teddy.director.client.screen.window.timeline;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import teddy.director.client.playback.ScenePlaybackController;

public class TimelinePlayhead {
    private static final int PLAYHEAD_COLOR = ImColor.rgba(255, 80, 80, 255);
    private static final float PLAYHEAD_WIDTH = 2;
    public static final float DRAGGABLE_AREA_HEIGHT = 15;

    private long playheadMs = 0;
    private boolean isMovingPlayhead = false;

    public TimelinePlayhead() {}

    public long getMs() {
        return playheadMs;
    }

    public void reset() {
        playheadMs = 0;
        isMovingPlayhead = false;
    }

    public void draw(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        handleMove(windowOrigin, windowSize);
        drawPlayhead(dl, windowOrigin, windowSize);
    }

    private void handleMove(ImVec2 windowOrigin, ImVec2 windowSize) {
        if (isMovingPlayhead && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            isMovingPlayhead = false;
            ImGui.setMouseCursor(ImGuiMouseCursor.Arrow);
        }

        boolean mouseIsInDraggableArea = TimelineWindow.mouseIsInRect(
                TimelineWindow.tracks.startPosX, windowOrigin.y,
                windowSize.x + windowOrigin.x, DRAGGABLE_AREA_HEIGHT + windowOrigin.y
        );

        if (ScenePlaybackController.playbackRunning()) {
            playheadMs = ScenePlaybackController.playbackCurrMilli();
        }
        else if (isMovingPlayhead || mouseIsInDraggableArea) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW);

            if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                isMovingPlayhead = true;

                float mouseX = TimelineWindow.mousePos.x;
                if (mouseX >= TimelineWindow.tracks.startPosX && mouseX <= windowSize.x + windowOrigin.x) {
                    playheadMs = TimelineWindow.xToMs(mouseX);
                    ScenePlaybackController.updateMilli(playheadMs);
                }
            }
        }
    }

    private void drawPlayhead(ImDrawList dl, ImVec2 windowOrigin, ImVec2 windowSize) {
        float playheadX = TimelineWindow.msToX(playheadMs);
        if (playheadX < TimelineWindow.tracks.startPosX || playheadX > windowOrigin.x + windowSize.x)
            return;

        dl.addLine(
                playheadX, windowOrigin.y,
                playheadX, TimelineWindow.tracks.endPosY,
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
