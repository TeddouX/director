package teddy.director.client.utils.imgui;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import teddy.director.client.screen.Freecam;

public class ImGuiRendering {
    private static final int[][] CUBE_EDGES = {
            {0,1},{1,3},{3,2},{2,0},   // bottom
            {4,5},{5,7},{7,6},{6,4},   // top
            {0,4},{1,5},{2,6},{3,7}    // verticals
    };

    public static void drawBoundingBox(AABB bb, int color) {
        Vec3[] c = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            c[i] = new Vec3(
                    (i & 0b001) == 0 ? bb.minX : bb.maxX,
                    (i & 0b010) == 0 ? bb.minY : bb.maxY,
                    (i & 0b100) == 0 ? bb.minZ : bb.maxZ
            );
        }

        ImDrawList dl = ImGui.getBackgroundDrawList();
        for (int[] e : CUBE_EDGES)
            drawLine3D(dl, c[e[0]], c[e[1]], color, 1.5f);
    }

    public static void drawFrustum(
            Vec3 pos,
            float yaw, float pitch,
            float fovDeg,
            float aspect,
            float near, float far,
            int color
    ) {
        float yawRads = (float) Math.toRadians(yaw);
        float pitchRads = (float) Math.toRadians(pitch);

        Vec3 look = new Vec3(
                -Mth.sin(yawRads) * Mth.cos(pitchRads),
                -Mth.sin(pitchRads),
                Mth.cos(yawRads) * Mth.cos(pitchRads)
        );

        Vec3 up = new Vec3(
                -Mth.sin(yawRads) * Mth.sin(pitchRads),
                Mth.cos(pitchRads),
                Mth.cos(yawRads) * Mth.sin(pitchRads)
        );

        Vec3 right = look.cross(up);

        double tanHalf = Math.tan(Math.toRadians(fovDeg) * 0.5);

        Vec3[] c = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            double d = (i & 4) == 0 ? near : far;
            double hh = tanHalf * d;
            double hw = hh * aspect;
            double sx = (i & 1) == 0 ? -1 : 1;
            double sy = (i & 2) == 0 ? -1 : 1;
            c[i] = pos
                    .add(look.scale(d))
                    .add(right.scale(sx * hw))
                    .add(up.scale(sy * hh));
        }

        ImDrawList dl = ImGui.getBackgroundDrawList();
        for (int[] e : CUBE_EDGES)
            drawLine3D(dl, c[e[0]], c[e[1]], color, 1.5f);
    }

    public static void drawLine3D(ImDrawList dl, Vec3 a, Vec3 b, int color, float thickness) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 viewPos = Freecam.pos;
        Vec3 look = Freecam.forwards;
        float nearEps = 0.05f;

        double da = a.subtract(viewPos).dot(look);
        double db = b.subtract(viewPos).dot(look);

        if (da < nearEps && db < nearEps)
            return;

        if (da < nearEps)
            a = a.add(b.subtract(a).scale((nearEps - da) / (db - da)));
        else if (db < nearEps)
            b = b.add(a.subtract(b).scale((nearEps - db) / (da - db)));

        ImVec2 size = ImGui.getIO().getDisplaySize();
        Vec3 aScreen = mc.gameRenderer.projectPointToScreen(a);
        Vec3 bScreen = mc.gameRenderer.projectPointToScreen(b);

        dl.addLine(
                (float) ((aScreen.x * 0.5 + 0.5) * size.x), (float) ((1.0 - (aScreen.y * 0.5 + 0.5)) * size.y),
                (float) ((bScreen.x * 0.5 + 0.5) * size.x), (float) ((1.0 - (bScreen.y * 0.5 + 0.5)) * size.y),
                color, thickness
        );
    }

    @Nullable
    public static Matrix4f drawRXRYTGizmos(float[] view, float[] proj, float[] model, int op) {
        ImGuizmo.setID(1);
        ImGuizmo.manipulate(view, proj, Operation.TRANSLATE | Operation.ROTATE_Y, Mode.WORLD, model);
        boolean usedA = ImGuizmo.isUsing();

        ImGuizmo.setID(2);
        ImGuizmo.manipulate(view, proj, Operation.ROTATE_X, Mode.LOCAL, model);
        boolean usedB = ImGuizmo.isUsing();

        if (usedA || usedB)
            return new Matrix4f().set(model);
        return null;
    }
}
