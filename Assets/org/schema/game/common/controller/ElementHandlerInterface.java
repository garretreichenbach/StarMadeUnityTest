package org.schema.game.common.controller;

import java.util.Collection;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Segment;

public interface ElementHandlerInterface {
	public void readjustControllers(Collection<Element> elems, SegmentController sc, Segment segment);
}
