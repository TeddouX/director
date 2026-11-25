package teddy.director.client.screen.window.scene;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiMouseButton;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.screen.Freecam;
import teddy.director.client.screen.window.timeline.TimelineWindow;
import teddy.director.client.utils.ImGuiColors;

import java.util.Optional;

public class SceneWindow {
    static final int[][] CUBE_EDGES = {
            {0,1},{1,3},{3,2},{2,0},   // bottom
            {4,5},{5,7},{7,6},{6,4},   // top
            {0,4},{1,5},{2,6},{3,7}    // verticals
    };

    private static final CameraControl camerasControl = new CameraControl();
    private static final TimelinesControl timelinesControl = new TimelinesControl();
    private static final DummyEditWindow dummyEditWindow = new DummyEditWindow();
    private static final CreateDummyPopup createDummyPopup = new CreateDummyPopup("Create Dummy");

    private static final double DUMMY_SELECT_REACH = 75;
    private static final double CAMERA_SELECT_REACH = 100;

    private static DummyData selectedDummy = null;
    private static boolean hasSelectedCamera = false;

    public static void draw() {
        if (ImGui.begin("Scene")) {
            if (DirectorState.currentMovie == null) {
                ImGui.text("Please load a movie to edit a scene");
                ImGui.end();
                return;
            }

            if (DirectorState.currentScene == null) {
                ImGui.text("Please select a scene");
                ImGui.end();
                return;
            }

            ImGui.text("Current scene: %s".formatted(DirectorState.currentScene.name));

            timelinesControl.draw();
            drawDummiesControl();
            camerasControl.draw();
            dummyEditWindow.draw();
            createDummyPopup.draw();

            handleEntitySelection();
            handleCameraSelection();
            drawGizmos();
        }

        ImGui.end();
    }

    public static void reset() {
        camerasControl.reset();
        createDummyPopup.reset();
        dummyEditWindow.close();
        timelinesControl.reset();

        TimelineWindow.reset();
    }

    private static void drawDummiesControl() {
        ImGui.separatorText("Dummies");

        if (ImGui.button("Create dummy..."))
            createDummyPopup.open();

        if (ImGui.treeNode("All Dummies")) {
            for (DummyData dummy : DirectorState.currentScene.dummies.values()) {
                if (ImGui.selectable(dummy.name)) {
                    dummyEditWindow.open(dummy);
                    selectedDummy = dummy;
                }
            }

            ImGui.treePop();
        }
    }

    private static void handleEntitySelection() {
        boolean selected = false;

        if (selectedDummy != null) {
            DummyEntity dummyEntity = ScenePlaybackController.getDummyEntity(selectedDummy.uuid);
            drawBoundingBox(dummyEntity.getBoundingBox(), ImGuiColors.BB_SELECTED);
        }

        Entity hoveredEntity = getHoveredDummyEntity();
        if (hoveredEntity instanceof DummyEntity dummyEntity && !Freecam.active) {
            drawBoundingBox(hoveredEntity.getBoundingBox(), ImGuiColors.BB_HOVERED);

            if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                dummyEditWindow.open(dummyEntity.data);
                hasSelectedCamera = false;
                selectedDummy = dummyEntity.data;
                selected = true;
            }
        }

        boolean isMouseOverWindow = ImGui.getIO().getWantCaptureMouse();
        if (ImGui.isMouseDown(ImGuiMouseButton.Left) && !selected && !isMouseOverWindow)
            selectedDummy = null;
    }

    private static void handleCameraSelection() {
        boolean selected = false;

        ImGui.getIO().getWantCaptureMouse();

        int color;
        if (isHoveringRenderCamera() && !Freecam.active) {
            color = ImGuiColors.BB_HOVERED;

            if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                hasSelectedCamera = true;
                selectedDummy = null;
                selected = true;

                camerasControl.openPreview();
            }
        }
        else if (hasSelectedCamera)
            color = ImGuiColors.BB_SELECTED;
        else
            color = ImGuiColors.WHITE;

        drawBoundingBox(DirectorState.renderCamera.getBoundingBox(), color);
        drawRenderCamFrustum(color);

        boolean isMouseOverWindow = ImGui.getIO().getWantCaptureMouse();
        if (ImGui.isMouseDown(ImGuiMouseButton.Left) && !selected && !isMouseOverWindow)
            hasSelectedCamera = false;
    }

    private static Entity getHoveredDummyEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;

        Vec3 start = mc.gameRenderer.mainCamera().position();
        Vec3 end = getMouseWorldVec(DUMMY_SELECT_REACH);
        AABB searchBox = new AABB(start, end).inflate(1.0f);

        Entity closest = null;
        double closestDist2 = DUMMY_SELECT_REACH * DUMMY_SELECT_REACH;

        for (Entity e : mc.level.getEntities(mc.player, searchBox, e -> e instanceof DummyEntity)) {
            AABB box = e.getBoundingBox();
            Optional<Vec3> hit = box.clip(start, end);
            if (hit.isPresent()) {
                double d = start.distanceToSqr(hit.get());
                if (d < closestDist2) {
                    closestDist2 = d;
                    closest = e;
                }
            }
        }

        return closest;
    }

    private static boolean isHoveringRenderCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return false;

        Vec3 start = mc.gameRenderer.mainCamera().position();
        Vec3 end = getMouseWorldVec(CAMERA_SELECT_REACH);

        AABB box = DirectorState.renderCamera.getBoundingBox();
        Optional<Vec3> hit = box.clip(start, end);

        return hit.isPresent();
    }

    private static Vec3 getMouseWorldVec(double reach) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();

        double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

        double ndcX = (mouseX / mc.getWindow().getGuiScaledWidth()) * 2.0 - 1.0;
        double ndcY = 1.0 - (mouseY / mc.getWindow().getGuiScaledHeight()) * 2.0;

        Matrix4f proj = Freecam.projMat;
        Matrix4f invProj = new Matrix4f(proj).invert();

        Vector4f p = new Vector4f((float) ndcX, (float) ndcY, 1f, 1f);
        invProj.transform(p);
        p.div(p.w);

        Vector3f viewDir = new Vector3f(p.x, p.y, p.z).normalize();
        Vec3 worldDir = new Vec3(camera.rotation().transform(viewDir, new Vector3f()));
        Vec3 start = camera.position();
        return start.add(worldDir.scale(reach));
    }

    private static void drawBoundingBox(AABB bb, int color) {
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

    private static void drawRenderCamFrustum(int color) {
        RenderCamera renderCam = DirectorState.renderCamera;
        Vec3 camPos = renderCam.pos;
        float yaw = renderCam.yaw;
        float pitch = renderCam.pitch;
        float fovDeg = renderCam.fov;
        float aspect = renderCam.aspect;
        float near = .5f;
        float far = 10f;
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
            c[i] = camPos
                    .add(look.scale(d))
                    .add(right.scale(sx * hw))
                    .add(up.scale(sy * hh));
        }

        ImDrawList dl = ImGui.getBackgroundDrawList();
        for (int[] e : CUBE_EDGES)
            drawLine3D(dl, c[e[0]], c[e[1]], color, 1.5f);
    }

    private static void drawLine3D(ImDrawList dl, Vec3 a, Vec3 b, int color, float thickness) {
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

    private static void drawGizmos() {
        RenderCamera cam = DirectorState.renderCamera;
        if (!Freecam.isInFront(cam.pos))
            return;

        float[] model = Freecam.getModelMat(cam.pos, cam.yaw, cam.pitch);
        float[] proj = Freecam.projMat.get(new float[16]);
        float[] view = Freecam.viewMat.get(new float[16]);

        int op = Operation.ROTATE_X | Operation.ROTATE_Y | Operation.ROTATE_Z | Operation.TRANSLATE;
        ImGuizmo.manipulate(view, proj, op, Mode.LOCAL, model);

        if (ImGuizmo.isUsing()) {
            Matrix4f m = new Matrix4f().set(model);

            Vector3f t = m.getColumn(3, new Vector3f());

            DirectorState.renderCamera.pos = new Vec3(t.x + Freecam.pos.x, t.y + Freecam.pos.y, t.z + Freecam.pos.z);

            Vector3f f = m.getColumn(2, new Vector3f()).negate().normalize();
            DirectorState.renderCamera.yaw   = (float) Math.toDegrees(Math.atan2(-f.x, f.z));
            DirectorState.renderCamera.pitch = (float) Math.toDegrees(Math.asin(Math.clamp(-f.y, -1, 1)));
        }
    }
}
