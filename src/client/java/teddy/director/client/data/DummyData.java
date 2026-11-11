package teddy.director.client.data;

import net.minecraft.world.phys.Vec3;
import teddy.director.client.utils.Named;

import java.util.UUID;

public class DummyData extends Named {
    public UUID uuid;

    // The displayed entity
    public String displayEntityID;
    public Vec3 pos;

    public DummyData() {
        super("");
    }

    public DummyData(String name) {
        super(name);

        this.uuid = UUID.randomUUID();
    }
}
