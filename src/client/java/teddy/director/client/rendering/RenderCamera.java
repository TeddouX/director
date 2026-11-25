package teddy.director.client.rendering;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import teddy.director.client.access.CameraAccess;
import teddy.director.client.data.CameraData;

import java.util.Optional;
import java.util.OptionalDouble;

public class RenderCamera {
    private static final double BB_HALF_SIZE = 0.5;

    private final RenderTarget renderTarget;
    public CameraData cameraData;
    public boolean active = false;

    public Vector4f fogColor = new Vector4f();
    public Vec3 pos = Vec3.ZERO;
    public float yaw, pitch;
    public float fov = 70f;
    public float aspect = 1920f/1080f;

    public RenderCamera() {
        this.renderTarget = new MainTarget(1920, 1080);
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

    public void fixRenderTargetAlpha() {
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "Force alpha",
                        renderTarget.getColorTextureView(),
                        Optional.empty(),
                        null,
                        OptionalDouble.empty()
                )
        ) {
            pass.setPipeline(RenderPipelines.FORCE_ALPHA);
            pass.draw(3, 1, 0, 0);
        }
    }

    public AABB getBoundingBox() {
        return new AABB(
                this.pos.x - BB_HALF_SIZE,
                this.pos.y - BB_HALF_SIZE,
                this.pos.z - BB_HALF_SIZE,
                this.pos.x + BB_HALF_SIZE,
                this.pos.y + BB_HALF_SIZE,
                this.pos.z + BB_HALF_SIZE
        );
    }

    public ImGuiTextureProvider getColorTex() {
        return ImGuiMC.getColorTexture(renderTarget);
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }
}
