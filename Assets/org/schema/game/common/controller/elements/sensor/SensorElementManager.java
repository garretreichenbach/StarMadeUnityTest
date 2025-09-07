package org.schema.game.common.controller.elements.sensor;

import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class SensorElementManager extends UsableControllableElementManager<SensorUnit, SensorCollectionManager, SensorElementManager> {

	public static boolean debug = false;

	public SensorElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.SIGNAL_SENSOR, ElementKeyMap.ACTIVAION_BLOCK_ID, segmentController);
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(SensorUnit firingUnit,
	                                             SensorCollectionManager col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}

	@Override
	protected String getTag() {
		return "sensor";
	}

	@Override
	public SensorCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<SensorCollectionManager> clazz) {
		return new SensorCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Sensor System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}
}
