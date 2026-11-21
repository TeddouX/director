package teddy.director.client.screen.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import teddy.director.client.DirectorState;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;
import teddy.director.client.screen.window.scene.SceneWindow;
import teddy.director.client.utils.ConfirmModal;
import teddy.director.client.utils.FilteredCombo;
import teddy.director.client.utils.NameInputModal;

import java.io.IOException;
import java.util.List;

public class MovieWindow {
    private static final NameInputModal CREATE_MOVIE_MODAL = new NameInputModal("Create Movie");
    private static final NameInputModal CREATE_SCENE_MODAL = new NameInputModal("Create Scene");
    private static final NameInputModal RENAME_SCENE_MODAL = new NameInputModal("Rename Scene");
    private static final ConfirmModal DELETE_SCENE_MODAL = new ConfirmModal("Delete Scene");
    private static final FilteredCombo SCENES_COMBO = new FilteredCombo("Scenes", "Select a scene...");

    private static boolean saveError = false;
    private static boolean loadError = false;
    private static int currentSelectedSceneIdx = -1;
    private static boolean openSceneRenameModal = false;
    private static boolean openSceneDeleteModal = false;

    public static void draw() {
        if (ImGui.begin("Movie", null, ImGuiWindowFlags.MenuBar)) {
            drawMenuBar();

            if (saveError)
                ImGui.textColored(255, 0, 0, 255, "Failed to save movie. See logs for more info");

            if (loadError)
                ImGui.textColored(255, 0, 0, 255, "Failed to load movie. See logs for more info");

            if (DirectorState.currentMovie == null) {
                ImGui.text("No movie currently loaded");
            }
            else {
                ImGui.text("Current movie: %s".formatted(DirectorState.currentMovie.name));
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
                                DirectorState.currentMovie = MovieData.load(saveData.uuid);

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
                        DirectorState.currentMovie.save();
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
        List<SceneData> scenes = DirectorState.currentMovie.scenes;

        ImGui.separatorText("Scene");

        if (ImGui.button("New scene..."))
            CREATE_SCENE_MODAL.open();

        int selected = SCENES_COMBO.draw(
                scenes.size(),
                i -> scenes.get(i).name,
                i -> {
                    if (ImGui.beginPopupContextItem()) {
                        SCENES_COMBO.setSelectedItemIdx(i);
                        currentSelectedSceneIdx = i;

                        SceneWindow.reset();
                        DirectorState.setCurrentScene(i);

                        if (ImGui.menuItem("Rename"))
                            openSceneRenameModal = true;

                        if (ImGui.menuItem("Delete"))
                            openSceneDeleteModal = true;

                        ImGui.endPopup();
                    }
                }
        );

        if (selected >= 0) {
            SceneWindow.reset();
            DirectorState.setCurrentScene(selected);
        }

        if (openSceneDeleteModal) {
            DELETE_SCENE_MODAL.open();
            openSceneDeleteModal = false;
        }

        if (openSceneRenameModal) {
            RENAME_SCENE_MODAL.open();
            openSceneRenameModal = false;
        }

        RENAME_SCENE_MODAL.draw(name -> DirectorState.renameScene(currentSelectedSceneIdx, name));

        if (currentSelectedSceneIdx >= 0) {
            String deleteSceneTitle = "Are you sure you want to delete scene \"%s\" ?"
                    .formatted(DirectorState.getSceneName(currentSelectedSceneIdx));

            DELETE_SCENE_MODAL.draw(deleteSceneTitle, confirm -> {
                if (confirm) {
                    DirectorState.deleteScene(currentSelectedSceneIdx);
                    DirectorState.currentScene = null;

                    SceneWindow.reset();
                    SCENES_COMBO.setSelectedItemIdx(-1);
                    currentSelectedSceneIdx = -1;
                }
            });
        }

        CREATE_SCENE_MODAL.draw(name -> {
            currentSelectedSceneIdx = DirectorState.createAndAddScene(name);
            SCENES_COMBO.setSelectedItemIdx(currentSelectedSceneIdx);
            DirectorState.setCurrentScene(currentSelectedSceneIdx);
        });
    }
}
