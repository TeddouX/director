package teddy.director.client.screen.window.scene;

import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import imgui.ImGui;
import teddy.director.client.DirectorState;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.NameInputModal;

public class CamerasControl {
    private final NameInputModal createCameraPopup = new NameInputModal("Create Camera");
    private final NameInputModal renameCameraPopup = new NameInputModal("Rename Camera");
    private final ConfirmModal deleteCameraPopup = new ConfirmModal("Delete Camera");

    public CamerasControl() {}

    public void draw() {
        ImGui.separatorText("Camera");

        if (DirectorState.currentScene.camera == null) {
            if (ImGui.button("Create Camera..."))
                createCameraPopup.open();

            createCameraPopup.draw(name -> DirectorState.createAndAddCamera(name));
        }
        else {
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

            if (DirectorState.renderCamera != null) {
                DirectorState.renderCamera.active = true;
                ImGuiTextureProvider tex = DirectorState.renderCamera.getColorTex();
                ImGuiMC.image(tex, (float) 1920 /4, (float) 1080 /4, 0, 1, 1, 0);
            }
        }
    }
}
