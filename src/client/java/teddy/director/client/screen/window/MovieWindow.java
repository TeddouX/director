package teddy.director.client.screen.window;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.flag.ImGuiWindowFlags;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.NameInputModal;

import java.io.IOException;
import java.util.List;

public class MovieWindow {
    private static final NameInputModal CREATE_MOVIE_MODAL = new NameInputModal("Create Movie");
    private static final NameInputModal CREATE_SCENE_MODAL = new NameInputModal("Create Scene");
    private static final NameInputModal RENAME_SCENE_MODAL = new NameInputModal("Rename Scene");
    private static final ConfirmModal DELETE_SCENE_MODAL = new ConfirmModal("Delete Scene");

    private static final ImGuiTextFilter SCENE_SELECT_FILTER = new ImGuiTextFilter();

    private static boolean saveError = false;
    private static boolean loadError = false;
    private static int currentSelectedSceneIdx = -1;

    public static void draw() {
        if (ImGui.begin("Movie", null, ImGuiWindowFlags.MenuBar)) {
            drawMenuBar();

            if (saveError)
                ImGui.textColored(255, 0, 0, 255, "Failed to save movie. See logs for more info");

            if (loadError)
                ImGui.textColored(255, 0, 0, 255, "Failed to load movie. See logs for more info");

            if (DirectorState.CURRENT_MOVIE == null) {
                ImGui.text("No movie currently loaded");
            }
            else {
                ImGui.text("Current movie: %s".formatted(DirectorState.CURRENT_MOVIE.name));
                ImGui.button("Export...");

                drawSceneControl();
            }

            CREATE_MOVIE_MODAL.draw(name -> {
                reset();

                DirectorState.createAndSetCurrentMovie(name);
            });
        }

        ImGui.end();
    }

    public static void reset() {
        currentSelectedSceneIdx = -1;
    }

    private static void drawMenuBar() {
        boolean openCreateMoviePopup = false;
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("New"))
                    openCreateMoviePopup = true;

                if (ImGui.beginMenu("Open")) {
                    boolean reloadCache = ImGui.menuItem("Reload");

                    ImGui.separator();

                    for (MovieData.SaveData saveData : MovieData.listAllSavedMovies(reloadCache)) {
                        if (ImGui.menuItem(saveData.name + "##" + saveData.uuid.toString())) {
                            try {
                                DirectorState.reset();
                                DirectorState.CURRENT_MOVIE = MovieData.load(saveData.uuid);

                                reset();
                            }
                            catch (IOException e) {
                                loadError = true;
                            }
                        }
                    }

                    ImGui.endMenu();
                }

                if (ImGui.menuItem("Save")) {
                    try {
                        DirectorState.CURRENT_MOVIE.save();
                    }
                    catch (IOException _) {
                        saveError = true;
                    }
                }

                ImGui.endMenu();
            }

            ImGui.endMenuBar();
        }

        if (openCreateMoviePopup)
            CREATE_MOVIE_MODAL.open();
    }

    private static void drawSceneControl() {
        List<SceneData> scenes = DirectorState.CURRENT_MOVIE.scenes;

        ImGui.separatorText("Scene");

        if (ImGui.button("New scene..."))
            CREATE_SCENE_MODAL.open();

        String preview = currentSelectedSceneIdx >= 0 ? scenes.get(currentSelectedSceneIdx).name : "Select a scene...";

        boolean openRenameModal = false;
        boolean openDeleteModal = false;
        if (ImGui.beginCombo("##scene_combo", preview)) {
            SCENE_SELECT_FILTER.draw("Search");

            for (int i = 0; i < scenes.size(); i++) {
                SceneData scene = scenes.get(i);
                if (!SCENE_SELECT_FILTER.passFilter(scene.name))
                    continue;

                boolean selected = i == currentSelectedSceneIdx;
                if (ImGui.selectable(scenes.get(i).name + "###" + i, selected)) {
                    currentSelectedSceneIdx = i;
                    DirectorState.setCurrentScene(currentSelectedSceneIdx);

                    SceneWindow.reset();
                }

                if (ImGui.beginPopupContextItem()) {
                    currentSelectedSceneIdx = i;
                    DirectorState.setCurrentScene(currentSelectedSceneIdx);

                    if (ImGui.menuItem("Rename"))
                        openRenameModal = true;

                    if (ImGui.menuItem("Delete"))
                        openDeleteModal = true;

                    ImGui.endPopup();
                }
            }

            ImGui.endCombo();
        }

        if (openDeleteModal) DELETE_SCENE_MODAL.open();
        if (openRenameModal) RENAME_SCENE_MODAL.open();

        RENAME_SCENE_MODAL.draw(name -> DirectorState.renameScene(currentSelectedSceneIdx, name));

        if (currentSelectedSceneIdx >= 0) {
            String deleteSceneTitle = "Are you sure you want to delete scene %s ?"
                    .formatted(DirectorState.getSceneName(currentSelectedSceneIdx));

            DELETE_SCENE_MODAL.draw(deleteSceneTitle, confirm -> {
                if (confirm)
                    DirectorState.deleteScene(currentSelectedSceneIdx);
            });
        }

        CREATE_SCENE_MODAL.draw(name -> {
            DirectorState.createAndAddScene(name);
            currentSelectedSceneIdx = DirectorState.CURRENT_MOVIE.scenes.size() - 1;
        });
    }
}
