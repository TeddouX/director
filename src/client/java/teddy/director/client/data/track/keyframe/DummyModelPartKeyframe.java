package teddy.director.client.data.track.keyframe;

import net.minecraft.world.phys.Vec3;

public class DummyModelPartKeyframe extends AbstractKeyframe {
    public String modelPartKey;
    public Vec3 position;
    public Vec3 rotation;

    public DummyModelPartKeyframe(int tick) {
        super(tick);
    }
}
