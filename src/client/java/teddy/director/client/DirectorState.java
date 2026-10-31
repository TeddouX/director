package teddy.director.client;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import teddy.director.client.data.MovieData;

public class DirectorState {
    public static final EntityType<?> DEFAULT_DUMMY_ENTITY_TYPE = EntityTypes.ARMOR_STAND;

    public static MovieData CURRENT_MOVIE = null;
    public static boolean IS_ACTIVE = true;
}
