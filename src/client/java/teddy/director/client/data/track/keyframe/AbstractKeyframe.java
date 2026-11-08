package teddy.director.client.data.track.keyframe;

import imgui.ImGui;
import imgui.type.ImBoolean;

public abstract class AbstractKeyframe {
    public transient ImBoolean windowOpen = new ImBoolean(true);

    public int tick;

    protected AbstractKeyframe() {}

    public AbstractKeyframe(int tick) {
        this.tick = tick;
    }

    public abstract void drawImGuiWindow();

    public void openImGuiWindow() {
        this.windowOpen.set(true);
    }

    protected void drawImGuiWindowContents() {
        ImGui.text("Editing keyframe");

        ImGui.separator();

        int[] ptr = new int[]{tick};
        ImGui.dragInt("Tick", ptr, 1, 0, Integer.MAX_VALUE);
        tick = ptr[0];
    }
}
