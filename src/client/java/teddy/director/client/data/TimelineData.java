package teddy.director.client.data;

import teddy.director.client.data.keyframe.AbstractKeyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TimelineData {
    public String name;
    public int lengthTicks;
    public List<Track> tracks = new ArrayList<>();
    public List<UUID> controlledDummies = new ArrayList<>();

    public static class Track {
        public String name;

        // Which type of keyframes this track stores, each track can only store one type of keyframes
        public String type;

        // Sorted by tick
        public List<AbstractKeyframe> keyframes = new ArrayList<>();

        public Track(String name) {
            this.name = name;
        }

        /// @return The last keyframe index if no keyframes exist for the tick
        public int getKeyFrameIndex(int tick) {
            int keyframesSize = keyframes.size() - 1;

            if (tick >= keyframes.getLast().tick)
                return keyframesSize;
            else if (tick == keyframes.getFirst().tick)
                return 0;

            int lo = 0;
            int hi = keyframesSize;

            while (lo <= hi) {
                int mid = (lo + hi) >>> 1; // lower bound average
                int midTick = keyframes.get(mid).tick;

                if (midTick < tick)
                    lo = mid + 1;
                else if (midTick > tick)
                    hi = mid - 1;
                else
                    return mid;
            }

            return keyframesSize;
        }
    }
}
