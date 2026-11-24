package teddy.director.client.data;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CameraData {
    public Vec3 origPos = Vec3.ZERO;
    public float origYaw = 0f, origPitch = 0f;

    public CameraData() {}
}
