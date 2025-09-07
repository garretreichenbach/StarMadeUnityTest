package org.schema.game.common.controller.elements.power.reactor;

import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.IntegrityBasedInterface;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class StabilizerElementManager extends UsableControllableSingleElementManager<StabilizerUnit, StabilizerCollectionManager, StabilizerElementManager> implements IntegrityBasedInterface{


	public StabilizerElementManager(SegmentController segmentController, Class<StabilizerCollectionManager> clazz) {
		super(segmentController, clazz);
	}


	@Override
	public void onControllerChange() {
		
	}


	@Override
	protected String getTag() {
		return "stabilizer";
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}


	@Override
	public ControllerManagerGUI getGUIUnitValues(StabilizerUnit firingUnit, StabilizerCollectionManager col,
			ControlBlockElementCollectionManager<?, ?, ?> supportCol,
			ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}


	@Override
	public StabilizerCollectionManager getNewCollectionManager(SegmentPiece position,
			Class<StabilizerCollectionManager> clazz) {
		return new StabilizerCollectionManager(getSegmentController(), this);
	}




	@Override
	public int getPriority() {
		return 15;
	}

}
