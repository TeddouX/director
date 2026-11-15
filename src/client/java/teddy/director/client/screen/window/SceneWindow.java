package teddy.director.client.screen.window;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.DirectorState;
import teddy.director.client.data.DummyData;
import teddy.director.client.data.TimelineData;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.screen.Freecam;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.FilteredCombo;
import teddy.director.client.utils.NameInputModal;

import java.util.List;
import java.util.Optional;

public class SceneWindow {
    static final int[][] CUBE_EDGES = {
            {0,1},{1,3},{3,2},{2,0},   // bottom
            {4,5},{5,7},{7,6},{6,4},   // top
            {0,4},{1,5},{2,6},{3,7}    // verticals
    };

    private static final int ERROR_TEXT_COLOR = ImColor.rgba(245, 13, 13, 255);
    private static final int BB_SELECTED = ImColor.rgba(0, 106, 255, 255);
    private static final int BB_HOVERED = ImColor.rgba(88, 157, 252, 255);

    private static final NameInputModal CREATE_TIMELINE_POPUP = new NameInputModal("Create Timeline");
    private static final NameInputModal RENAME_TIMELINE_MODAL = new NameInputModal("Rename Timeline");
    private static final ConfirmModal DELETE_TIMELINE_MODAL = new ConfirmModal("Delete Timeline");

    private static final FilteredCombo TIMELINES_COMBO = new FilteredCombo("Timelines", "Select a timeline...");
    private static final FilteredCombo ENTITY_TYPE_COMBO = new FilteredCombo("Display Entity", "Select a display entity...");

    private static final String CREATE_DUMMY_POPUP_NAME = "Create Dummy";
    private static final ImString dummyNameImString = new ImString();
    private static boolean hasEmptyTrackNameError = false;
    private static boolean hasNotSelectedEntityType = false;
    private static EntityType<?> selectedEntityType = null;

    private static int currentSelectedTimelineIdx = -1;
    private static boolean openTimelineRenameModal = false;
    private static boolean openTimelineDeleteModal = false;

    private static final ImBoolean dummyEditWindowOpen = new ImBoolean(false);
    private static boolean dummyEditWindowWasOpened = false;
    private static DummyData selectedDummy = null;
    private static final ImString dummyRenameImString = new ImString();
    private static boolean hasEmptyRenameDummyStrErr = false;

    public static void draw() {
        if (ImGui.begin("Scene")) {
            if (DirectorState.CURRENT_MOVIE == null) {
                ImGui.text("Please load a movie to edit a scene");
                ImGui.end();
                return;
            }

            if (DirectorState.CURRENT_SCENE == null) {
                ImGui.text("Please select a scene");
                ImGui.end();
                return;
            }

            ImGui.text("Current scene: %s".formatted(DirectorState.CURRENT_SCENE.name));

            drawDummiesControl();
            drawTimelinesControl();
            drawDummyEditWindow();
            handleEntitySelection();
        }

        ImGui.end();
    }

    public static void reset() {
        TIMELINES_COMBO.reset();
        currentSelectedTimelineIdx = -1;

        TimelineWindow.reset();
    }

    private static void drawDummiesControl() {
        ImGui.separatorText("Dummies");

        if (ImGui.button("Create dummy..."))
            ImGui.openPopup(CREATE_DUMMY_POPUP_NAME);

        if (ImGui.treeNode("All Dummies")) {
            for (DummyData dummy : DirectorState.CURRENT_SCENE.dummies.values()) {
                if (ImGui.selectable(dummy.name))
                    openDummyEditWindow(dummy);
            }

            ImGui.treePop();
        }

        drawCreateDummyPopup();
    }

    private static void drawCreateDummyPopup() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(CREATE_DUMMY_POPUP_NAME, null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.isWindowAppearing())
                ImGui.setKeyboardFocusHere();
            ImGui.inputText("Name", dummyNameImString);

            if (hasEmptyTrackNameError)
                ImGui.textColored(ERROR_TEXT_COLOR, "Please input a track name");

            int selectedEntityId = drawEntityTypeCombo();
            if (selectedEntityId >= 0)
                selectedEntityType = BuiltInRegistries.ENTITY_TYPE.byId(selectedEntityId);

            ImGui.separator();

            if (ImGui.button("Create")) {
                hasEmptyTrackNameError = dummyNameImString.isEmpty();
                hasNotSelectedEntityType = selectedEntityType == null;

                if (!hasEmptyTrackNameError && !hasNotSelectedEntityType) {
                    DirectorState.createAndSummonDummy(dummyNameImString.get(), selectedEntityType);

                    resetCreateDummyPopupData();
                    ImGui.closeCurrentPopup();
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel")) {
                resetCreateDummyPopupData();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private static void resetCreateDummyPopupData() {
        hasEmptyTrackNameError = false;
        hasNotSelectedEntityType = false;
        ENTITY_TYPE_COMBO.reset();

        selectedEntityType = null;
        dummyNameImString.clear();
    }

    private static void drawTimelinesControl() {
        ImGui.separatorText("Timelines");

        List<TimelineData> timelines = DirectorState.CURRENT_SCENE.timelines;

        if (ImGui.button("New timeline..."))
            CREATE_TIMELINE_POPUP.open();

        int selected = TIMELINES_COMBO.draw(
                timelines.size(),
                i -> timelines.get(i).name,
                i -> {
                    if (ImGui.beginPopupContextItem()) {
                        TIMELINES_COMBO.setSelectedItemIdx(i);
                        currentSelectedTimelineIdx = i;

                        DirectorState.setCurrentTimeline(i);

                        if (ImGui.menuItem("Rename"))
                            openTimelineRenameModal = true;

                        if (ImGui.menuItem("Delete"))
                            openTimelineDeleteModal = true;

                        ImGui.endPopup();
                    }
                }
        );

        if (selected >= 0)
            DirectorState.setCurrentTimeline(selected);

        if (openTimelineDeleteModal) {
            DELETE_TIMELINE_MODAL.open();
            openTimelineDeleteModal = false;
        }

        if (openTimelineRenameModal) {
            RENAME_TIMELINE_MODAL.open();
            openTimelineRenameModal = false;
        }

        RENAME_TIMELINE_MODAL.draw(name -> DirectorState.renameTimeline(currentSelectedTimelineIdx, name));

        if (currentSelectedTimelineIdx >= 0) {
            String deleteTimelineTitle = "Are you sure you want to delete timeline \"%s\" ?"
                    .formatted(DirectorState.getTimelineName(currentSelectedTimelineIdx));

            DELETE_TIMELINE_MODAL.draw(deleteTimelineTitle, confirm -> {
                if (confirm) {
                    DirectorState.deleteTimeline(currentSelectedTimelineIdx);
                    DirectorState.CURRENT_TIMELINE = null;
                    TimelineWindow.reset();

                    TIMELINES_COMBO.setSelectedItemIdx(-1);
                    currentSelectedTimelineIdx = -1;
                }
            });
        }

        CREATE_TIMELINE_POPUP.draw(name -> {
            currentSelectedTimelineIdx = DirectorState.createAndAddTimeline(name);
            TIMELINES_COMBO.setSelectedItemIdx(currentSelectedTimelineIdx);
            DirectorState.setCurrentTimeline(currentSelectedTimelineIdx);
        });
    }

    private static void drawDummyEditWindow() {
        if (!dummyEditWindowOpen.get() || selectedDummy == null)
            return;

        // TODO : special case for a player to set the skin etc

        ImGui.begin("Edit Dummy", dummyEditWindowOpen);

        ImGui.separatorText("Info");

        ImGui.inputText("Name", dummyRenameImString);
        if (hasEmptyRenameDummyStrErr)
            ImGui.textColored(ERROR_TEXT_COLOR, "Please enter a name");

        ImGui.text("UUID: %s".formatted(selectedDummy.uuid.toString()));

        int selectedEntityId = drawEntityTypeCombo();
        if (selectedEntityId >= 0)
            selectedEntityType = BuiltInRegistries.ENTITY_TYPE.byId(selectedEntityId);

        Vec3 pos = selectedDummy.pos;
        ImGui.text("Original position: (%.2f, %.2f, %.2f)".formatted(pos.x, pos.y, pos.z));

        ImGui.separator();

        if (ImGui.button("Done")) {
            hasEmptyRenameDummyStrErr = dummyRenameImString.isEmpty();

            if (!hasEmptyRenameDummyStrErr) {
                selectedDummy.name = dummyRenameImString.get();

                String newEntityId = BuiltInRegistries.ENTITY_TYPE.getKey(selectedEntityType).toString();

                if (!newEntityId.equals(selectedDummy.displayEntityID)) {
                    ScenePlaybackController.removeDummyEntity(selectedDummy.uuid);

                    selectedDummy.displayEntityID = newEntityId;

                    ScenePlaybackController.summonDummy(selectedDummy);
                }

                closeDummyEditWindow();

            }
        }

        ImGui.sameLine();

        if (ImGui.button("Cancel"))
            closeDummyEditWindow();

        ImGui.end();

        if (dummyEditWindowWasOpened && !dummyEditWindowOpen.get())
            closeDummyEditWindow();

        dummyEditWindowWasOpened = dummyEditWindowOpen.get();
    }

    private static void closeDummyEditWindow() {
        dummyRenameImString.clear();

        selectedDummy = null;
        selectedEntityType = null;

        ENTITY_TYPE_COMBO.setSelectedItemIdx(-1);

        dummyEditWindowOpen.set(false);
    }

    private static void openDummyEditWindow(DummyData dummyData) {
        dummyEditWindowOpen.set(true);
        selectedDummy = dummyData;
        dummyRenameImString.set(dummyData.name);

        Identifier id = Identifier.parse(dummyData.displayEntityID);
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        int idx = BuiltInRegistries.ENTITY_TYPE.getId(entityType);

        ENTITY_TYPE_COMBO.setSelectedItemIdx(idx);
    }

    private static int drawEntityTypeCombo() {
        return ENTITY_TYPE_COMBO.draw(BuiltInRegistries.ENTITY_TYPE.size(), i -> {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(i);
            if (!DefaultAttributes.hasSupplier(entityType))
                return "";

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

            return id.toString();
        });
    }

    private static void handleEntitySelection() {
        if (selectedDummy != null) {
            DummyEntity dummyEntity = ScenePlaybackController.getDummyEntity(selectedDummy.uuid);
            drawEntityBoundingBox(dummyEntity, BB_SELECTED);
        }

        Entity hoveredEntity = getHoveredDummyEntity(100);
        if (!(hoveredEntity instanceof DummyEntity dummyEntity))
            return;

        if (!Freecam.active)
            drawEntityBoundingBox(hoveredEntity, BB_HOVERED);

        if (ImGui.isMouseDown(ImGuiMouseButton.Left))
            openDummyEditWindow(dummyEntity.data);
    }

    private static Entity getHoveredDummyEntity(double reach) {
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
        Vec3 end = start.add(dir.scale(reach));

        AABB searchBox = new AABB(start, end).inflate(1.0);

        Entity closest = null;
        double closestDist2 = reach * reach;

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

        ImVec2 size = ImGui.getIO().getDisplaySize(); // use the same size ImGui is rendering to
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
