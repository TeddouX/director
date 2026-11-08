package teddy.director.client.utils;

import imgui.ImGui;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class Vec3Input {
    private final String label;
    private final float[] value = new float[]{0, 0, 0};

    public Vec3Input(String label) {
        this.label = label;
    }

    public Vec3Input(String label, Vec3 value) {
        this.label = label;

        this.value[0] = (float)value.x;
        this.value[1] = (float)value.y;
        this.value[2] = (float)value.z;
    }

    public void draw(Consumer<Vec3> onChange) {
        if (ImGui.dragFloat3(label, value))
            onChange.accept(getValue());
    }

    public Vec3 getValue() {
        return new Vec3(value[0], value[1], value[2]);
    }
}
