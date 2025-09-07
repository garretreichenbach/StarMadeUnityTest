package org.schema.game.common.data;

import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

public interface SegmentDamageCallback {

	public void damage(byte x, byte y, byte z, Segment s, int damage, short type);

	public void registerRemoval(SegmentData segmentData, byte x, byte y, byte z);

}
