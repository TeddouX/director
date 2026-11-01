package teddy.director.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import teddy.director.Director;

public class DirectorKeybinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Director.id("custom_category")
    );

    public static final KeyMapping openDirectorUIKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.director.open_director_ui",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    CATEGORY
            )
    );

    public static void registerKeyMappings() { }
}
