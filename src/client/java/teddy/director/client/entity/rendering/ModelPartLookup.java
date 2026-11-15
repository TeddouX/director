package teddy.director.client.entity.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ModelPartLookup {
    private static final Map<Model<?>, Map<String, ModelPart>> MODEL_PARTS_CACHE = new WeakHashMap<>();
    private static final Map<EntityType<?>, Model<?>> MODEL_CACHE = new WeakHashMap<>();

    public static Map<String, ModelPart> get(Model<?> model) {
        return MODEL_PARTS_CACHE.computeIfAbsent(model, m -> {
            Map<String, ModelPart> out = new HashMap<>();
            walk("", m.root(), out);

            return out;
        });
    }

    public static Model<?> getEntityModel(EntityType<?> entityType) {
        return MODEL_CACHE.computeIfAbsent(entityType, _ -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return null;

            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            Entity entity = entityType.create(mc.level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true));
            if (entity == null) return null;

            EntityRenderer<?, ?> renderer = dispatcher.getRenderer(entity);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> living)
                return living.getModel();

            return null;
        });
    }

    private static void walk(String prefix, ModelPart part, Map<String, ModelPart> out) {
        part.children.forEach((name, child) -> {
            String path = prefix.isEmpty() ? name : prefix + "/" + name;
            out.put(path, child);
            walk(path, child, out);
        });
    }
}
