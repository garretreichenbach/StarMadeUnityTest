package org.schema.game.common.data;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Segment;

public class SegmentRetrieveCallback {
	public final Vector3i pos = new Vector3i();
	public final Vector3i abspos = new Vector3i();
	public int state;
	public Segment segment;
}
