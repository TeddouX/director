package teddy.director.client.utils;

import imgui.ImGui;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class Vec3Input {
    private final String label;
    private final float[] value = new float[]{0, 0, 0};
    private final float speed;

    public Vec3Input(String label, float speed) {
        this.label = label;
        this.speed = speed;
    }

    public void draw(Consumer<Vec3> onChange) {
        if (ImGui.dragFloat3(label, value, speed))
            onChange.accept(getValue());
    }

    public Vec3 getValue() {
        return new Vec3(value[0], value[1], value[2]);
    }
}
