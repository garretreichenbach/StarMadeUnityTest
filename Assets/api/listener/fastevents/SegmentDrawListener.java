package api.listener.fastevents;

import org.schema.game.common.data.world.DrawableRemoteSegment;

/**
 * Created by Jake on 11/13/2020.
 * <insert description here>
 */
public interface SegmentDrawListener {
    void preDrawSegment(DrawableRemoteSegment segment);//, int blended, Shader shader, boolean drawShields, boolean aabbOnly, int visMask
    void postDrawSegment(DrawableRemoteSegment segment);//, int blended, Shader shader, boolean drawShields, boolean aabbOnly, int visMask
}
