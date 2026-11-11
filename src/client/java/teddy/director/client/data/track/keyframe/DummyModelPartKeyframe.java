package teddy.director.client.data.track.keyframe;

import imgui.ImGui;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.utils.Vec3Input;

public class DummyModelPartKeyframe extends AbstractKeyframe {
    public Vec3 position = new Vec3(0, 0, 0);
    public Vec3 rotation = new Vec3(0, 0, 0);

    private final transient Vec3Input posInput = new Vec3Input("Position", 0.01f);
    private final transient Vec3Input rotInput = new Vec3Input("Rotation", 0.01f);

    public DummyModelPartKeyframe() {
        super();
    }

    public DummyModelPartKeyframe(int tick) {
        super(tick);
    }

    @Override
    public void drawImGuiWindow() {
        if (!windowOpen.get())
            return;

        if (ImGui.begin("Keyframe", windowOpen)) {
            drawImGuiWindowContents();

            ImGui.separatorText("Edit model part");

            posInput.draw(pos -> position = pos);
            rotInput.draw(rot -> rotation = rot);
        }

        ImGui.end();
    }
}
