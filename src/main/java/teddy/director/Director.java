package teddy.director;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Director implements ModInitializer {
	public static final String MOD_ID = "director";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
	}

	public static EntityType<?> entityTypeFromStringId(String strId) {
		Identifier id = Identifier.tryParse(strId);
		if (id == null) {
			Director.LOGGER.error("Malformed identifier {}", strId);
			return null;
		}

		if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
			Director.LOGGER.error("Entity type {} doesn't exist in the registry", strId);
			return null;
		}

		return BuiltInRegistries.ENTITY_TYPE.getValue(id);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
