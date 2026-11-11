package teddy.director.client.utils;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import org.jetbrains.annotations.Nullable;
import teddy.director.Director;

import java.util.function.Consumer;
import java.util.function.Function;

public class FilteredCombo {
    private final ImGuiTextFilter textFilter = new ImGuiTextFilter();
    private final String name;
    private final String defaultPreview;
    private int selectedItemIdx = -1;

    public FilteredCombo(String name, String defaultPreview) {
        this.name = name;
        this.defaultPreview = defaultPreview;
    }

    public int draw(
            int numItems,
            Function<Integer, String> getItemAt,
            @Nullable Consumer<Integer> eachItem
    ) {
        if (selectedItemIdx >= numItems)
            selectedItemIdx = -1;

        String preview = selectedItemIdx >= 0 ? getItemAt.apply(selectedItemIdx) : defaultPreview;
        int clicked = -1;

        if (ImGui.beginCombo(name, preview)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();

            textFilter.draw("Search");

            for (int i = 0; i < numItems; i++) {
                String nameAt = getItemAt.apply(i);
                if (nameAt.isEmpty())
                    continue;

                if (!textFilter.passFilter(nameAt))
                    continue;

                boolean selected = i == selectedItemIdx;
                if (ImGui.selectable(nameAt + "###" + i, selected)) {
                    selectedItemIdx = i;
                    clicked = i;
                }

                if (eachItem != null) eachItem.accept(i);
            }

            ImGui.endCombo();
        }

        return clicked;
    }

    public int draw(int numItems, Function<Integer, String> getItemAt) {
        return draw(numItems, getItemAt, null);
    }

    public void setSelectedItemIdx(int idx) {
        selectedItemIdx = idx;
    }

    public void reset() {
        selectedItemIdx = -1;
    }
}
