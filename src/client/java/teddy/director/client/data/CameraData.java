package teddy.director.client.data;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CameraData {
    public String name = "Unnamed camera";
    public UUID uuid = UUID.randomUUID();
    public Vec3 origPos = Vec3.ZERO;
    public UUID associatedTimeline = null;

    public CameraData() {}

    public CameraData(String name) {
        this.name = name;
    }
}
