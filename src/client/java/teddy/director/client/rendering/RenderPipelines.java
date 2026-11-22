package teddy.director.client.rendering;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import teddy.director.Director;

import java.util.Optional;

public class RenderPipelines {
    public static final RenderPipeline FORCE_ALPHA = net.minecraft.client.renderer.RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Director.id("pipeline/force_alpha"))
                    .withVertexShader(Director.id("core/force_alpha"))
                    .withFragmentShader(Director.id("core/force_alpha"))
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withCull(false)
                    .withColorTargetState(new ColorTargetState(
                            Optional.empty(),
                            GpuFormat.RGBA8_UNORM,
                            ColorTargetState.WRITE_ALPHA
                    ))
                    .build()
    );
}
