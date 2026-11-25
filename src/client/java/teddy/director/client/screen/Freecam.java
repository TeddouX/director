package teddy.director.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import teddy.director.client.access.CameraAccess;

public class Freecam {
    private static final double ORIG_SPEED = 15;
    public static double speed = ORIG_SPEED;

    public static boolean active = false;
    public static Vec3 pos = Vec3.ZERO, prevPos = Vec3.ZERO;
    public static float yaw, pitch;
    public static Matrix4f projMat;
    public static Matrix4f viewMat;

    public static Vec3 forwards;
    public static Vec3 up;
    public static Vec3 right;

    private static long lastNanos;

    public static void copyPlayerCam() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        prevPos = pos = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();

        recalculateVectors();
    }

    public static void toggle() {
        active = !active;
        speed = ORIG_SPEED;
    }

    public static void update(Minecraft mc) {
        if (DirectorScreen.active) {
            CameraAccess cam = (CameraAccess) mc.gameRenderer.mainCamera();
            cam.director$setRotation(yaw, pitch);
            cam.director$setPosition(pos);
            cam.director$overrideTransform();
        }

        if (!active)
            return;

        long now = System.nanoTime();
        double dtSec = Math.min((now - lastNanos) / 1_000_000_000.0, 0.1);
        lastNanos = now;

        prevPos = pos;

        Options o = mc.options;

        double fwd  = (down(mc, o.keyUp) ? 1 : 0)    - (down(mc, o.keyDown) ? 1 : 0);
        double side = (down(mc, o.keyRight) ? 1 : 0) - (down(mc, o.keyLeft) ? 1 : 0);
        double vert = (down(mc, o.keyJump) ? 1 : 0)  - (down(mc, o.keyShift) ? 1 : 0);

        Vec3 look  = Vec3.directionFromRotation(pitch, yaw);
        Vec3 right = look.cross(Vec3.Y_AXIS).normalize();

        Vec3 move = look
                .scale(fwd)
                .add(right.scale(side))
                .add(0, vert, 0);

        if (move.lengthSqr() > 0)
            pos = pos.add(move.normalize().scale(speed * dtSec));
    }

    public static void rotateWithSens(Minecraft mc, float newYaw, float newPitch) {
        double ss = mc.options.sensitivity().get() * 0.3F + 0.2F;
        double sensitivityMod = ss * ss * ss;
        double sens = sensitivityMod * 8.0;

        yaw += (float)(newYaw * sens);

        pitch += (float)(newPitch * sens);
        pitch = Math.clamp(pitch, -90, 90);

        recalculateVectors();
    }

    public static void scrollWheel(double value) {
        speed += value;
        speed = Math.clamp(speed, 1, 100);
    }

    private static void recalculateVectors() {
        forwards = forwardVec(yaw, pitch);
        up = forwardVec(yaw, pitch - 90);
        right = forwards.cross(up);
    }

    private static Vec3 forwardVec(float camYaw, float camPitch) {
        float yawRads = (float) Math.toRadians(camYaw);
        float pitchRads = (float) Math.toRadians(camPitch);
        double cosPitch = Mth.cos(pitchRads);

        return new Vec3(
                -Mth.sin(yawRads) * cosPitch,
                -Mth.sin(pitchRads),
                Mth.cos(yawRads) * cosPitch
        );
    }

    public static float[] getModelMat(Vec3 otherPos, float yaw, float pitch) {
        return new Matrix4f()
                .translation((float)(otherPos.x - pos.x),
                        (float)(otherPos.y - pos.y),
                        (float)(otherPos.z - pos.z))
                .rotateY((float)(Mth.PI - Math.toRadians(yaw)))
                .rotateX((float) -Math.toRadians(pitch))
                .get(new float[16]);
    }

    public static boolean isInFront(Vec3 otherPos) {
        Vec3 dPos = otherPos.subtract(pos);
        double dot = dPos.dot(Freecam.forwards);
        return dot > 0;
    }

    private static boolean down(Minecraft mc, KeyMapping mapping) {
        int code = KeyMappingHelper.getBoundKeyOf(mapping).getValue();
        return InputConstants.isKeyDown(mc.getWindow(), code);
    }
}
