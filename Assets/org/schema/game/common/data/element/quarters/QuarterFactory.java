package org.schema.game.common.data.element.quarters;

import org.schema.game.common.controller.SegmentController;

public interface QuarterFactory {
	public Quarter getQuarter(SegmentController c);
}
