package teddy.director.client.data.track;

import imgui.ImGui;
import imgui.type.ImBoolean;

public abstract class AbstractKeyframe {
    protected transient AbstractTrack track;
    public transient ImBoolean windowOpen = new ImBoolean(true);

    public long ms;

    public AbstractKeyframe(long ms) {
        this.ms = ms;
    }

    protected abstract void drawImGuiWindowContents();

    public void openImGuiWindow() {
        this.windowOpen.set(true);
    }

    public void setParentTrack(AbstractTrack track) {
        this.track = track;
    }

    public void drawImGuiWindow() {
        if (!windowOpen.get())
            return;

        if (ImGui.begin("Keyframe", windowOpen)) {
            ImGui.text("Editing keyframe");

            ImGui.separator();

            long[] ptr = new long[]{ms};
            ImGui.dragScalar("Millisecond", ptr, 20, 0, Integer.MAX_VALUE);

            track.moveKeyframe(this.ms, ptr[0]);

            drawImGuiWindowContents();
        }

        ImGui.end();
    }
}
