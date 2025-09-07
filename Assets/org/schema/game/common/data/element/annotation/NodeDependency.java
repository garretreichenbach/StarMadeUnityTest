package org.schema.game.common.data.element.annotation;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.facedit.ElementInformationOption;

public interface NodeDependency {
	public void onSwitch(ElementInformationOption opt, ElementInformation info, Element elem);
}
