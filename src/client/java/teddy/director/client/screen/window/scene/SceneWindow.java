package teddy.director.client.screen.window.scene;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.playback.ScenePlaybackController;
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

    private static final CamerasControl CAMERAS_CONTROL = new CamerasControl();
    private static final DummyEditWindow DUMMY_EDIT_WINDOW = new DummyEditWindow();
    private static final CreateDummyPopup CREATE_DUMMY_POPUP = new CreateDummyPopup("Create Dummy");
    private static final TimelinesControl TIMELINES_CONTROL = new TimelinesControl();

    private static final double DUMMY_SELECT_REACH = 75;

    private static DummyData selectedDummy = null;

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

            TIMELINES_CONTROL.draw();
            drawDummiesControl();
            CAMERAS_CONTROL.draw();
            DUMMY_EDIT_WINDOW.draw();
            CREATE_DUMMY_POPUP.draw();

            handleEntitySelection();
        }

        ImGui.end();
    }

    public static void reset() {
        CAMERAS_CONTROL.reset();
        CREATE_DUMMY_POPUP.reset();
        DUMMY_EDIT_WINDOW.close();
        TIMELINES_CONTROL.reset();

        TimelineWindow.reset();
    }

    private static void drawDummiesControl() {
        ImGui.separatorText("Dummies");

        if (ImGui.button("Create dummy..."))
            CREATE_DUMMY_POPUP.open();

        if (ImGui.treeNode("All Dummies")) {
            for (DummyData dummy : DirectorState.currentScene.dummies.values()) {
                if (ImGui.selectable(dummy.name)) {
                    DUMMY_EDIT_WINDOW.open(dummy);
                    selectedDummy = dummy;
                }
            }

            ImGui.treePop();
        }
    }

    private static void handleEntitySelection() {
        if (selectedDummy != null) {
            DummyEntity dummyEntity = ScenePlaybackController.getDummyEntity(selectedDummy.uuid);
            drawEntityBoundingBox(dummyEntity, ImGuiColors.BB_SELECTED);
        }

        Entity hoveredEntity = getHoveredDummyEntity();
        if (!(hoveredEntity instanceof DummyEntity dummyEntity))
            return;

        if (!Freecam.active)
            drawEntityBoundingBox(hoveredEntity, ImGuiColors.BB_HOVERED);

        if (ImGui.isMouseDown(ImGuiMouseButton.Left))
            DUMMY_EDIT_WINDOW.open(dummyEntity.data);
    }

    private static Entity getHoveredDummyEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;

        double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

        double ndcX = (mouseX / mc.getWindow().getGuiScaledWidth()) * 2.0 - 1.0;
        double ndcY = 1.0 - (mouseY / mc.getWindow().getGuiScaledHeight()) * 2.0;

        double aspect = (double) mc.getWindow().getWidth() / mc.getWindow().getHeight();
        double tanHalfFov = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);

        Camera camera = mc.gameRenderer.mainCamera();

        Vec3 forward = new Vec3(camera.forwardVector());
        Vec3 up = new Vec3(camera.upVector());
        Vec3 right = new Vec3(camera.leftVector()).scale(-1);

        Vec3 dir = forward
                .add(right.scale(ndcX * tanHalfFov * aspect))
                .add(up.scale(ndcY * tanHalfFov))
                .normalize();

        Vec3 start = camera.position();
        Vec3 end = start.add(dir.scale(DUMMY_SELECT_REACH));

        AABB searchBox = new AABB(start, end).inflate(1.0);

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

    private static void drawEntityBoundingBox(Entity entity, int color) {
        Minecraft mc = Minecraft.getInstance();
        AABB bb = entity.getBoundingBox();

        ImVec2 size = ImGui.getIO().getDisplaySize();
        float displayW = size.x, displayH = size.y;

        float[][] screen = new float[8][2];
        boolean[] ok = new boolean[8];
        for (int i = 0; i < 8; i++) {
            Vec3 c = new Vec3(
                    (i & 1) == 0 ? bb.minX : bb.maxX,
                    (i & 2) == 0 ? bb.minY : bb.maxY,
                    (i & 4) == 0 ? bb.minZ : bb.maxZ);

            Vec3 ndc = mc.gameRenderer.projectPointToScreen(c);
            ok[i] = ndc.z > 0 && ndc.z < 1.0;
            screen[i][0] = (float) ((ndc.x * 0.5 + 0.5) * displayW);
            screen[i][1] = (float) ((1.0 - (ndc.y * 0.5 + 0.5)) * displayH);
        }

        ImDrawList dl = ImGui.getBackgroundDrawList();
        for (int[] e : CUBE_EDGES) {
            if (ok[e[0]] && ok[e[1]])
                dl.addLine(screen[e[0]][0], screen[e[0]][1], screen[e[1]][0], screen[e[1]][1], color, 1.5f);
        }
    }
}
