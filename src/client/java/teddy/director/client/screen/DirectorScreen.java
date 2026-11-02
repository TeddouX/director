package teddy.director.client.screen;

import com.mojang.blaze3d.platform.Window;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import teddy.director.Director;
import teddy.director.client.DirectorState;
import teddy.director.client.data.MovieData;
import teddy.director.client.playback.MoviePlaybackController;

import java.io.IOException;

public class DirectorScreen extends Screen {
    private static final String CREATE_MOVIE_POPUP_NAME = "Create Movie";

    private static boolean isInFreecam = false;
    private static boolean isMouseOverGUI = false;
    private static boolean saveError = false;
    private static final ImString createMoviePopupMovieName = new ImString();

    public static boolean ACTIVE = false;

    public DirectorScreen(Component title) {
        super(title);

        ACTIVE = true;
    }

    @Override
    public void onClose() {
        ACTIVE = false;

        super.onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Do not render any background
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !isInFreecam;
    }

    public static void renderImGui() {
        if (!ACTIVE)
            return;

        isMouseOverGUI = ImGui.getIO().getWantCaptureMouse();

        ImGui.showDemoWindow(null);

        handleFreecam();
        renderMovieWindow();
        TimelineWindow.draw();
    }

    private static void renderMovieWindow() {
        if (ImGui.begin("Movie", null, ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoTitleBar)) {
            boolean openCreateMoviePopup = false;

            if (ImGui.beginMenuBar()) {
                if (ImGui.beginMenu("Movie")) {
                    if (ImGui.menuItem("New"))
                        openCreateMoviePopup = true;

                    if (ImGui.beginMenu("Open")) {
                        boolean reloadCache = ImGui.menuItem("Reload");

                        ImGui.separator();

                        for (MovieData movie : MovieData.listAllSavedMovies(reloadCache)) {
                            if (ImGui.menuItem(movie.name)) {
                                MoviePlaybackController.loadMovieData(movie);

                                Director.LOGGER.info("Loaded movie {}", movie.name);
                            }
                        }

                        ImGui.endMenu();
                    }

                    if (ImGui.menuItem("Save"))
                    {
                        try {
                            MovieData.save(DirectorState.CURRENT_MOVIE);
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
                ImGui.textColored(255, 0, 0, 255, "Failed to save the movie.");

            if (DirectorState.CURRENT_MOVIE == null)
                ImGui.text("No movie currently loaded");
            else
            {
                ImGui.text("Current movie: %s".formatted(DirectorState.CURRENT_MOVIE.name));
                ImGui.button("Export...");
            }


            if (openCreateMoviePopup)
                ImGui.openPopup(CREATE_MOVIE_POPUP_NAME);
            renderCreateMoviePopup();
        }

        ImGui.end();
    }

    private static void renderCreateMoviePopup() {
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && !isMouseOverGUI) {
            Window win = Minecraft.getInstance().getWindow();
            GLFW.glfwSetCursorPos(win.handle(), (double) win.getScreenWidth() / 2, (double) win.getScreenHeight() / 2);
            GLFW.glfwSetInputMode(win.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

            isInFreecam = true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 1) {
            Window win = Minecraft.getInstance().getWindow();
            GLFW.glfwSetCursorPos(win.handle(), (double) win.getScreenWidth() / 2, (double) win.getScreenHeight() / 2);
            GLFW.glfwSetInputMode(win.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);

            isInFreecam = false;
        }

        return super.mouseReleased(event);
    }

    private static void handleFreecam() {

    }
}
