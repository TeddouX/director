package teddy.director.client.data.track.keyframe;

import org.jspecify.annotations.Nullable;

public record KeyframePair(
        @Nullable AbstractKeyframe before,
        @Nullable AbstractKeyframe after
) {}
