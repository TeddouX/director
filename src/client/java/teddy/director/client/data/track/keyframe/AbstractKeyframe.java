package teddy.director.client.data.track.keyframe;

import imgui.ImGui;
import imgui.type.ImBoolean;
import teddy.director.client.data.track.AbstractTrack;

public abstract class AbstractKeyframe {
    private transient AbstractTrack track;
    public transient ImBoolean windowOpen = new ImBoolean(true);

    public long ms;

    protected AbstractKeyframe() {}

    public AbstractKeyframe(long ms) {
        this.ms = ms;
    }

    public abstract void drawImGuiWindow();

    public void openImGuiWindow() {
        this.windowOpen.set(true);
    }

    public void setParentTrack(AbstractTrack track) {
        this.track = track;
    }

    protected void drawImGuiWindowContents() {
        ImGui.text("Editing keyframe");

        ImGui.separator();

        long[] ptr = new long[]{ms};
        ImGui.dragScalar("Millisecond", ptr, 1, 0, Integer.MAX_VALUE);

        track.moveKeyframe(this.ms, ptr[0]);
    }
}
