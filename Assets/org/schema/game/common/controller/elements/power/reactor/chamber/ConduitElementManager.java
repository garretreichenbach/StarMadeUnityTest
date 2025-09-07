package org.schema.game.common.controller.elements.power.reactor.chamber;

import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.IntegrityBasedInterface;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class ConduitElementManager extends UsableControllableSingleElementManager<ConduitUnit, ConduitCollectionManager, ConduitElementManager> implements IntegrityBasedInterface{


	public ConduitElementManager(SegmentController segmentController, Class<ConduitCollectionManager> clazz) {
		super(segmentController, clazz);
	}


	@Override
	public void onControllerChange() {
		
	}


	@Override
	protected String getTag() {
		return "conduit";
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}


	

	@Override
	public int getPriority() {
		return 15;
	}


	@Override
	public ControllerManagerGUI getGUIUnitValues(ConduitUnit firingUnit, ConduitCollectionManager col,
			ControlBlockElementCollectionManager<?, ?, ?> supportCol,
			ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}


	@Override
	public ConduitCollectionManager getNewCollectionManager(SegmentPiece position,
			Class<ConduitCollectionManager> clazz) {
		return new ConduitCollectionManager(getSegmentController(), this);
	}


}
