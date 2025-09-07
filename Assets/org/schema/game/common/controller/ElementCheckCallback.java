package org.schema.game.common.controller;

import org.schema.game.common.data.element.ElementInformation;

public interface ElementCheckCallback {
	boolean isCheckCriteriaSatisfied(ElementInformation info);

	boolean isCriteriaToCheckSatisfied(ElementInformation info);
}
