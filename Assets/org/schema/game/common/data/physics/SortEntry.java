package org.schema.game.common.data.physics;

import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.world.SegmentData;

public class SortEntry implements Comparable<SortEntry> {
	byte x;
	byte y;
	byte z;

	BlockStyle blockStyle;
	byte orientation;
	float length;
	boolean active;
	byte slab;

	SegmentData segmentData;
	public ElementInformation info;

	@Override
	public int compareTo(SortEntry o) {
		return Float.compare(length, o.length);
	}

}
