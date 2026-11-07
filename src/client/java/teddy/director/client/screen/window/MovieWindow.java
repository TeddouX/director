package teddy.director.client.screen.window;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.MovieData;
import teddy.director.client.data.SceneData;

import java.io.IOException;
import java.util.List;

public class MovieWindow {
    private static final String CREATE_MOVIE_POPUP_NAME = "Create Movie";
    private static final ImString createMoviePopupMovieName = new ImString();

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
                ImGui.openPopup(CREATE_MOVIE_POPUP_NAME);
            drawCreateMoviePopup();
        }

        ImGui.end();
    }

    private static void drawCreateMoviePopup() {
        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center, ImGuiCond.Appearing, new ImVec2(0.5f, 0.5f));

        if (ImGui.beginPopupModal(CREATE_MOVIE_POPUP_NAME, null, ImGuiWindowFlags.AlwaysAutoResize))
        {
            ImGui.inputText("Name", createMoviePopupMovieName);

            ImGui.separator();

            if (ImGui.button("Create", new ImVec2(120, 0))) {
                if (!createMoviePopupMovieName.isEmpty()) {
                    createNewMovie(createMoviePopupMovieName.get());
                    ImGui.closeCurrentPopup();
                }
                else {
                    // Show error
                }
            }

            ImGui.sameLine();

            if (ImGui.button("Cancel", new ImVec2(120, 0)))
                ImGui.closeCurrentPopup();

            ImGui.endPopup();
        }
    }

    private static void createNewMovie(String name) {
        MovieData movieData = new MovieData(name);

        DirectorState.CURRENT_MOVIE = movieData;

        Director.LOGGER.info("Created movie {}", movieData.name);
        createMoviePopupMovieName.clear();
    }

    private static void drawSceneControl() {
        List<SceneData> scenes = DirectorState.CURRENT_MOVIE.scenes;

        ImGui.separatorText("Scene");

        if (ImGui.button("Create new scene")) {

        }

        String preview = currentSelectedSceneIdx >= 0 ? scenes.get(currentSelectedSceneIdx).name : "Select a scene...";
        if (ImGui.beginCombo("##scene_combo", preview)) {
            for (int i = 0; i < scenes.size(); i++) {
                boolean selected = i == currentSelectedSceneIdx;

                if (ImGui.selectable(scenes.get(i).name + "###" + i, selected))
                    currentSelectedSceneIdx = i;
            }
            ImGui.endCombo();
        }
    }
}
