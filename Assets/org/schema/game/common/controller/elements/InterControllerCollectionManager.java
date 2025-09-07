package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.network.StateInterface;

public interface InterControllerCollectionManager {

	public SegmentPiece getControllerElement();

	public String getWarpDestinationUID();

	public StateInterface getState();

	public int getWarpType();

	public int getWarpPermission();

	public SegmentController getSegmentController();

	public Vector3i getControllerPos();

	public Vector3i getLocalDestination();

}
