package teddy.director.client.screen;

import com.mojang.blaze3d.platform.Window;
import imgui.ImGui;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import teddy.director.client.DirectorClient;
import teddy.director.client.DirectorState;
import teddy.director.client.screen.window.MovieWindow;
import teddy.director.client.screen.window.scene.SceneWindow;
import teddy.director.client.screen.window.TimelineWindow;

public class DirectorScreen extends Screen {
    private static boolean isInFreecam = false;
    private static boolean isMouseOverGUI = false;
    private double lastMouseX = 0, lastMouseY = 0;

    public static boolean active = false;

    public DirectorScreen(Component title) {
        super(title);

        active = true;

        Freecam.copyPlayerCam();
    }

    @Override
    public void onClose() {
        active = false;
        DirectorState.renderCamera.active = false;
        DirectorState.freezeCulling = false;

        // Reload all chunks
        minecraft.levelExtractor.allChanged();

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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void renderImGui() {
        if (!active)
            return;

        isMouseOverGUI = ImGui.getIO().getWantCaptureMouse();

        ImGui.showDemoWindow(null);

        MovieWindow.draw();
        SceneWindow.draw();
        TimelineWindow.draw();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && !isMouseOverGUI && !isInFreecam) {
            Window win = this.minecraft.getWindow();
            float middleX = (float) win.getWidth() / 2;
            float middleY = (float) win.getHeight() / 2;

            GLFW.glfwSetCursorPos(win.handle(), middleX, middleY);
            GLFW.glfwSetInputMode(win.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

            lastMouseX = MouseHandler.getScaledXPos(win, middleX);
            lastMouseY = MouseHandler.getScaledYPos(win, middleY);

            Freecam.toggle();

            isInFreecam = true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 1) {
            Window win = this.minecraft.getWindow();

            GLFW.glfwSetCursorPos(win.handle(), (float) win.getWidth() / 2, (float) win.getHeight() / 2);
            GLFW.glfwSetInputMode(win.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);

            Freecam.toggle();

            isInFreecam = false;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void mouseMoved(double x, double y) {
        double dx = x - lastMouseX;
        double dy = y - lastMouseY;

        if (isInFreecam)
            Freecam.rotateWithSens(this.minecraft, (float)dx, (float)dy);

        lastMouseX = x;
        lastMouseY = y;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        Freecam.scrollWheel(scrollY);

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }
}
