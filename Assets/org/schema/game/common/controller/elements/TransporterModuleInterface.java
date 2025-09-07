package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterElementManager;
import org.schema.game.common.controller.elements.transporter.TransporterUnit;

public interface TransporterModuleInterface {
	public ManagerModuleCollection<TransporterUnit, TransporterCollectionManager, TransporterElementManager> getTransporter();
}
