package org.schema.game.common.controller.elements.trigger;

import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class TriggerElementManager extends UsableControllableElementManager<TriggerUnit, TriggerCollectionManager, TriggerElementManager> {

	public static boolean debug = false;

	public TriggerElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.SIGNAL_TRIGGER_AREA_CONTROLLER, ElementKeyMap.SIGNAL_TRIGGER_AREA, segmentController);
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(TriggerUnit firingUnit,
	                                             TriggerCollectionManager col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
				return null;
	}

	@Override
	protected String getTag() {
		return "trigger";
	}

	//
	//	@Override
	//	public void handleSingleActivation(SegmentPiece controller) {
	//
	//	}
	//

	@Override
	public TriggerCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<TriggerCollectionManager> clazz) {
		return new TriggerCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Trigger System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}
}
