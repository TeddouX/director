package teddy.director.client.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import teddy.director.Director;

public class ClientEntities {
    public static final EntityType<DummyEntity> DUMMY = register(
            "dummy_entity",
            EntityType.Builder.of(DummyEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .noSave()
                    .noSummon()
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Director.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerAll() {
        // calculateEntityAnimation
    }
}
