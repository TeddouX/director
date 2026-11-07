package teddy.director.client.screen.window;

import imgui.ImGui;
import teddy.director.client.DirectorState;

public class SceneWindow {
    public static void draw() {
        if (ImGui.begin("Scene")) {
            if (DirectorState.CURRENT_MOVIE == null) {
                ImGui.text("Please load a movie to edit a scene");
                ImGui.end();
                return;
            }

            if (DirectorState.CURRENT_SCENE == null) {
                ImGui.text("Please select a scene");
                ImGui.end();
                return;
            }
        }
        ImGui.end();
    }
}
