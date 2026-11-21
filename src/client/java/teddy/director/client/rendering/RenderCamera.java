package teddy.director.client.rendering;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.access.CameraAccess;
import teddy.director.client.data.CameraData;

public class RenderCamera {
    private final RenderTarget renderTarget;
    public CameraData cameraData;
    public boolean active = false;

    public Vec3 pos = Vec3.ZERO;
    public float yaw, pitch;

    public RenderCamera(Minecraft mc) {
        this.renderTarget = new MainTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight());;
    }

    public void close() {
        renderTarget.destroyBuffers();
    }

    public void updateMcCam(Minecraft mc) {
        if (!active)
            return;

        CameraAccess mcCam = (CameraAccess) mc.gameRenderer.mainCamera();
        mcCam.director$setRotation(yaw, pitch);
        mcCam.director$setPosition(pos);
        mcCam.director$overrideTransform();
    }

    public ImGuiTextureProvider getColorTex() {
        return ImGuiMC.getColorTexture(renderTarget);
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }
}
