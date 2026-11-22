package teddy.director.client.screen.window.scene;

import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.lwjgl.opengl.GL11;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.NameInputModal;

public class CamerasControl {
    private final NameInputModal createCameraPopup = new NameInputModal("Create Camera");
    private final NameInputModal renameCameraPopup = new NameInputModal("Rename Camera");
    private final ConfirmModal deleteCameraPopup = new ConfirmModal("Delete Camera");

    private final ImBoolean camPreviewWinOpen = new ImBoolean(false);

    public CamerasControl() {}

    public void draw() {
        ImGui.separatorText("Camera");

        if (DirectorState.currentScene.camera == null) {
            if (ImGui.button("Create Camera..."))
                createCameraPopup.open();

            createCameraPopup.draw(name -> DirectorState.createAndAddCamera(name));
            return;
        }

        String camName = DirectorState.currentScene.camera.name;
        ImGui.text("Current camera :");
        ImGui.bullet(); ImGui.selectable(camName);

        boolean openDelete = false;
        boolean openRename = false;

        if (ImGui.beginPopupContextItem()) {
            if (ImGui.menuItem("Rename")) openRename = true;
            if (ImGui.menuItem("Delete")) openDelete = true;
            ImGui.separator();
            if (ImGui.menuItem("Create associated track")) {

            }
            ImGui.setItemTooltip("Creates and adds a track that controls this camera to the current timeline");

            ImGui.endPopup();
        }

        if (openDelete) deleteCameraPopup.open();
        if (openRename) renameCameraPopup.open();

        deleteCameraPopup.draw("Are you sure you want to delete camera %s".formatted(camName), confirm -> {
            if (!confirm) return;

            DirectorState.currentScene.camera = null;
        });

        renameCameraPopup.draw(name -> DirectorState.currentScene.camera.name = name);

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
        ImGui.begin("Camera preview \"%s\"###cam_preview_window"
                .formatted(DirectorState.currentScene.camera.name),
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
