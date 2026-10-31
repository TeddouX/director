package teddy.director.client.entity.rendering;

import java.util.HashMap;
import java.util.Map;

public class DummyEntityAnimationState {
    public record Transform(float dx, float dy, float dz, float rx, float ry, float rz) {
    }

    public Map<String, Transform> partTransforms = new HashMap<>();
}
