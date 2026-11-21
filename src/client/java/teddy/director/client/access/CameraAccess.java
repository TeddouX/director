package teddy.director.client.access;

import net.minecraft.world.phys.Vec3;

public interface CameraAccess {
    void director$setPosition(Vec3 pos);
    void director$setRotation(float yaw, float pitch);
    void director$overrideTransform();
}
