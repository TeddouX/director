package teddy.director.client.screen.window;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;

import java.io.IOException;
import java.util.List;

public class MovieWindow {
    private static final NameInputModal CREATE_MOVIE_MODAL = new NameInputModal("Create Movie");
    private static final NameInputModal CREATE_SCENE_MODAL = new NameInputModal("Create Scene");

    private static final ImGuiTextFilter SCENE_SELECT_FILTER = new ImGuiTextFilter();

    private static boolean saveError = false;
    private static boolean loadError = false;
    private static int currentSelectedSceneIdx = -1;

    public static void draw() {
        if (ImGui.begin("Movie", null, ImGuiWindowFlags.MenuBar)) {
            boolean openCreateMoviePopup = false;

            if (ImGui.beginMenuBar()) {
                if (ImGui.beginMenu("File")) {
                    if (ImGui.menuItem("New"))
                        openCreateMoviePopup = true;

                    if (ImGui.beginMenu("Open")) {
                        boolean reloadCache = ImGui.menuItem("Reload");

                        ImGui.separator();

                        for (MovieData.SaveData saveData : MovieData.listAllSavedMovies(reloadCache)) {
                            if (ImGui.menuItem(saveData.name)) {
                                try {
                                    DirectorState.CURRENT_MOVIE = MovieData.load(saveData.uuid);
                                    currentSelectedSceneIdx = -1;
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

            if (openCreateMoviePopup)
                CREATE_MOVIE_MODAL.open();

            CREATE_MOVIE_MODAL.draw(name -> {
                MovieData movieData = new MovieData(name);
                DirectorState.CURRENT_MOVIE = movieData;

                Director.LOGGER.info("Created movie {}", movieData.name);
            });
        }

        ImGui.end();
    }

    private static void drawSceneControl() {
        List<SceneData> scenes = DirectorState.CURRENT_MOVIE.scenes;

        ImGui.separatorText("Scene");

        if (ImGui.button("New scene..."))
            CREATE_SCENE_MODAL.open();

        String preview = currentSelectedSceneIdx >= 0 ? scenes.get(currentSelectedSceneIdx).name : "Select a scene...";
        if (ImGui.beginCombo("##scene_combo", preview)) {
            SCENE_SELECT_FILTER.draw("Search");

            for (int i = 0; i < scenes.size(); i++) {
                SceneData scene = scenes.get(i);
                if (!SCENE_SELECT_FILTER.passFilter(scene.name))
                    continue;

                boolean selected = i == currentSelectedSceneIdx;
                if (ImGui.selectable(scenes.get(i).name + "###" + i, selected)) {
                    currentSelectedSceneIdx = i;

                    DirectorState.CURRENT_SCENE = scene;
                    SceneWindow.reset();
                }

                if (ImGui.beginPopupContextItem()) {
                    if (ImGui.menuItem("Rename"))
                        Director.LOGGER.info("Rename");

                    if (ImGui.menuItem("Delete"))
                        Director.LOGGER.info("Delete");

                    ImGui.endPopup();
                }
            }
            ImGui.endCombo();
        }

        CREATE_SCENE_MODAL.draw(name -> {
            SceneData sceneData = new SceneData(name);

            DirectorState.CURRENT_MOVIE.scenes.add(sceneData);
            DirectorState.CURRENT_SCENE = sceneData;

            currentSelectedSceneIdx = DirectorState.CURRENT_MOVIE.scenes.size() - 1;

            Director.LOGGER.info("Created scene {}", name);
        });
    }
}
