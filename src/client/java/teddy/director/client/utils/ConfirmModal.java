package teddy.director.client.utils;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;

import java.util.function.Consumer;

public class ConfirmModal {
    public final String name;

    public ConfirmModal(String name) {
        this.name = name;
    }

    public void open() {
        ImGui.openPopup(name);
    }

    public void draw(String title, Consumer<Boolean> out) {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(name, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text(title);
            ImGui.separator();

            if (ImGui.button("Confirm")) {
                out.accept(true);
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel")) {
                out.accept(false);
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }
}
