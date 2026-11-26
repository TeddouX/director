package teddy.director.client.utils.imgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.function.Consumer;

public class NameInputModal {
    private final String name;
    private final ImString input = new ImString();
    private boolean hasEmptyNameError = false;

    public NameInputModal(String name) {
        this.name = name;
    }

    public void open() {
        ImGui.openPopup(name);
    }

    public void draw(Consumer<String> onConfirm) {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(name, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();
            ImGui.inputText("Name", input);

            if (hasEmptyNameError)
                ImGui.textColored(ImGuiColors.ERROR_TEXT, "Name is empty");

            ImGui.separator();

            if (ImGui.button("Done")) {
                if (!input.isEmpty()) {
                    onConfirm.accept(input.get());
                    resetData();
                    ImGui.closeCurrentPopup();
                }
                else {
                    hasEmptyNameError = true;
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel")) {
                resetData();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void resetData() {
        hasEmptyNameError = false;
        input.clear();
    }
}
