package org.schema.game.common.data;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;

public interface ManagedSegmentController<E extends SegmentController> {

	public ManagerContainer<E> getManagerContainer();

	public SegmentController getSegmentController();

	

	


}
