package teddy.director.client.data.track.keyframe;

import net.minecraft.world.phys.Vec3;

public class DummyPositionKeyframe extends AbstractKeyframe {
    public Vec3 position;

    public DummyPositionKeyframe(int tick) {
        super(tick);
    }
}
