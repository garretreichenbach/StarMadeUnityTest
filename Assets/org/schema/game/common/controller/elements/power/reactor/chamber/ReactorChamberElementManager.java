package org.schema.game.common.controller.elements.power.reactor.chamber;

import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.IntegrityBasedInterface;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class ReactorChamberElementManager extends UsableControllableSingleElementManager<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> implements IntegrityBasedInterface{


	private short chamberType;


	public ReactorChamberElementManager(short type, SegmentController segmentController, Class<ReactorChamberCollectionManager> clazz) {
		super(segmentController, clazz);
		this.chamberType = type;
	}


	@Override
	public void onControllerChange() {
		
	}


	@Override
	protected String getTag() {
		return "chamber";
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}


	

	@Override
	public int getPriority() {
		return 15;
	}


	@Override
	public ControllerManagerGUI getGUIUnitValues(ReactorChamberUnit firingUnit, ReactorChamberCollectionManager col,
			ControlBlockElementCollectionManager<?, ?, ?> supportCol,
			ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}


	@Override
	public ReactorChamberCollectionManager getNewCollectionManager(SegmentPiece position, Class<ReactorChamberCollectionManager> clazz) {
		return new ReactorChamberCollectionManager(chamberType, getSegmentController(), this);
	}





}
