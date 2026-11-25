package teddy.director.client.data.track;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import imgui.ImGui;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.DirectorState;
import teddy.director.client.screen.window.timeline.TimelineWindow;
import teddy.director.client.utils.Vec3Input;

public class CameraPosTrack extends AbstractTrack<CameraPosTrack.Keyframe> {
    public static final String TYPE_ID = "camera_pos";

    @Override
    public String getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void update(long ms) {
        KeyframePair keyframes = getKeyframesAround(ms);
        Vec3 lerpedPos;
        float lerpedYaw, lerpedPitch;

        Keyframe before = (Keyframe) keyframes.before();
        Keyframe after = (Keyframe) keyframes.after();
        if (before == null && after == null) {
            return;
        }
        else if (after == null) {
            lerpedPos = before.pos;
            lerpedYaw = before.yaw;
            lerpedPitch = before.pitch;
        }
        else if (before == null) {
            lerpedPos = after.pos;
            lerpedYaw = after.yaw;
            lerpedPitch = after.pitch;
        }
        else {
            long dBefAf = after.ms - before.ms;
            long dBefTick = ms - before.ms;

            float x = dBefAf == 0 ? 0f : (float) dBefTick / dBefAf;
            x = Math.clamp(x, 0f, 1f);

            lerpedPos = before.pos.lerp(after.pos, x);
            lerpedYaw = Mth.lerp(x, before.yaw, after.yaw);
            lerpedPitch = Mth.lerp(x, before.pitch, after.pitch);
        }

        DirectorState.renderCamera.pos = lerpedPos;
        DirectorState.renderCamera.yaw = lerpedYaw;
        DirectorState.renderCamera.pitch = lerpedPitch;
    }

    @Override
    public void save(JsonObject obj, Gson gson) {
        super.save(obj, gson);
    }

    @Override
    public void load(JsonObject obj, Gson gson) {
        super.load(obj, gson);
    }

    @Override
    public Keyframe createKeyframe(long ms) {
        Keyframe kf = new Keyframe(ms);

        kf.pos = DirectorState.renderCamera.pos;
        kf.yaw = DirectorState.renderCamera.yaw;
        kf.pitch = DirectorState.renderCamera.pitch;
        kf.updateInputs();

        addKeyframe(ms, kf);
        return kf;
    }

    @Override
    public void drawEditWindow() {

    }

    public static class Keyframe extends AbstractKeyframe {
        private final transient Vec3Input posInput = new Vec3Input("Position", 0.01f);
        private final transient float[] yawInputVal = new float[1];
        private final transient float[] pitchInputVal = new float[1];

        public Vec3 pos = Vec3.ZERO;
        public float yaw, pitch;

        public Keyframe(long ms) {
            super(ms);
        }

        public void updateInputs() {
            posInput.setValue(pos);
            yawInputVal[0] = yaw;
            pitchInputVal[0] = pitch;
        }

        @Override
        protected void drawImGuiWindowContents() {
            ImGui.separatorText("Edit camera position");

            posInput.draw(newPos -> pos = newPos);
            ImGui.dragFloat("Yaw", yawInputVal, 0.2f, -180f, 180f);
            ImGui.dragFloat("Pitch", pitchInputVal, 0.1f, -90f, 90f);

            yaw = yawInputVal[0];
            // -90 is up for some reason
            pitch = -pitchInputVal[0];

            track.update(TimelineWindow.playhead.getMs());
        }
    }
}
