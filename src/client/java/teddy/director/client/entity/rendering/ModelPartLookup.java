package teddy.director.client.entity.rendering;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ModelPartLookup {
    private static final Map<Model<?>, Map<String, ModelPart>> CACHE = new WeakHashMap<>();

    public static Map<String, ModelPart> get(Model<?> model) {
        return CACHE.computeIfAbsent(model, m -> {
            Map<String, ModelPart> out = new HashMap<>();
            walk("", m.root(), out);

            return out;
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
