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
import teddy.director.client.screen.window.MovieWindow;
import teddy.director.client.screen.window.SceneWindow;
import teddy.director.client.screen.window.TimelineWindow;

import java.io.IOException;

public class DirectorScreen extends Screen {
    private static boolean isInFreecam = false;
    private static boolean isMouseOverGUI = false;

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

        MovieWindow.draw();
        SceneWindow.draw();
        TimelineWindow.draw();
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
