package org.schema.game.common.controller.elements;

import java.util.List;

import org.schema.game.common.controller.SegmentController;

public interface InterControllerConnectionManager {

	public SegmentController getSegmentController();

	public List<? extends InterControllerCollectionManager> getCollectionManagers();

}
