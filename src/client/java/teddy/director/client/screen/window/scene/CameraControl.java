package teddy.director.client.screen.window.scene;

import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import teddy.director.client.DirectorState;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.NameInputModal;

public class CameraControl {
    private final ImBoolean camPreviewWinOpen = new ImBoolean(false);

    public CameraControl() {}

    public void reset() {
        camPreviewWinOpen.set(false);
    }

    public void openPreview() {
        camPreviewWinOpen.set(true);
    }

    public void draw() {
        ImGui.separatorText("Camera");

        if (ImGui.button("Create associated timeline")) {
            int idx = DirectorState.createAndAddTimeline("Main Camera");
            DirectorState.setCurrentTimeline(idx);
        }

        if (ImGui.button("Open preview"))
            camPreviewWinOpen.set(true);
        else
            DirectorState.renderCamera.active = false;

        drawCamPreview();
    }

    private void drawCamPreview() {
        if (!camPreviewWinOpen.get())
            return;

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("Camera preview###cam_preview_window",
                camPreviewWinOpen,
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse
        );

        RenderCamera renderCam = DirectorState.renderCamera;
        renderCam.active = true;

        ImGuiTextureProvider tex = renderCam.getColorTex();

        ImVec2 avail = ImGui.getContentRegionAvail();
        float texW = renderCam.getRenderTarget().width;
        float texH = renderCam.getRenderTarget().height;

        if (avail.x > 1 && avail.y > 1) {
            float scale = Math.min(avail.x / texW, avail.y / texH);
            float w = texW * scale;
            float h = texH * scale;

            ImGui.setCursorPos(
                    ImGui.getCursorPosX() + (avail.x - w) * 0.5f,
                    ImGui.getCursorPosY() + (avail.y - h) * 0.5f
            );

            ImGuiMC.image(tex, w, h, 0, 1, 1, 0);
        }

        ImGui.end();
        ImGui.popStyleVar();
    }
}
