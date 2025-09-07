package org.schema.game.server.controller;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;

public interface SegmentPathCallback {
	public void pathFinished(boolean success, List<Long> resultPathReference);

	public Vector3i getObjectSize();
}
