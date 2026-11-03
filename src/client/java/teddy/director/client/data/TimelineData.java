package teddy.director.client.data;

import teddy.director.client.data.keyframe.AbstractKeyframe;

import java.util.*;

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
        public TreeMap<Integer, AbstractKeyframe> keyframes = new TreeMap<>();

        public Track(String name) {
            this.name = name;
        }

        public void addKeyframe(AbstractKeyframe keyframe) {
            keyframes.put(keyframe.tick, keyframe);
        }

        public List<AbstractKeyframe> getKeyFramesAffectingTick(int tick) {
            Map.Entry<Integer, AbstractKeyframe> lower  = keyframes.floorEntry(tick);
            Map.Entry<Integer, AbstractKeyframe> higher = keyframes.ceilingEntry(tick);

            List<AbstractKeyframe> list = new ArrayList<>(2);
            if (lower == null && higher == null) {
                list.add(null);
                list.add(null);
            }
            else if (lower == null) {
                list.add(null);
                list.add(higher.getValue());
            }
            else if (higher == null) {
                list.add(lower.getValue());
                list.add(null);
            }
            else {
                list.add(lower.getValue());
                list.add(higher.getValue());
            }

            return list;
        }
    }
}
