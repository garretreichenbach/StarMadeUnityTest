package org.schema.game.client.data;

import org.schema.game.common.controller.SegmentController;

public interface PowerChangeListener {
	public static enum PowerChangeType{
		REACTOR,
		STABILIZER,
		CHAMBER,
		STABILIZER_PATH
	}
	public void powerChanged(SegmentController c, PowerChangeType t);
}
