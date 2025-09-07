package org.schema.game.client.data;

import org.schema.game.common.controller.SegmentController;

public interface RailDockingListener {
	public void dockingChanged(SegmentController c, boolean docked);
}
