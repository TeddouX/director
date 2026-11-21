package teddy.director.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import teddy.director.Director;
import teddy.director.client.access.CameraAccess;

public class Freecam {
    private static final double ORIG_SPEED = 15;
    public static double speed = ORIG_SPEED;

    public static boolean active = false;
    public static Vec3 pos = Vec3.ZERO, prevPos = Vec3.ZERO;
    public static float yaw, pitch;
    private static long lastNanos;

    public static void copyPlayerCam() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        prevPos = pos = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
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

    public static void rotateWithSens(Minecraft mc, float yaw, float pitch) {
        double ss = mc.options.sensitivity().get() * 0.3F + 0.2F;
        double sensitivityMod = ss * ss * ss;
        double sens = sensitivityMod * 8.0;

        Freecam.yaw += (float)(yaw * sens);

        Freecam.pitch += (float)(pitch * sens);
        Freecam.pitch = Math.clamp(Freecam.pitch, -90, 90);
    }

    public static void scrollWheel(double value) {
        speed += value;
        speed = Math.clamp(speed, 1, 100);
    }

    private static boolean down(Minecraft mc, KeyMapping mapping) {
        int code = KeyMappingHelper.getBoundKeyOf(mapping).getValue();
        return InputConstants.isKeyDown(mc.getWindow(), code);
    }
}
