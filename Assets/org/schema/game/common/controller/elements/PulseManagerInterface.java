package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.pulse.push.PushPulseCollectionManager;
import org.schema.game.common.controller.elements.pulse.push.PushPulseElementManager;
import org.schema.game.common.controller.elements.pulse.push.PushPulseUnit;

public interface PulseManagerInterface {
	public ManagerModuleCollection<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> getPushPulse();

}
