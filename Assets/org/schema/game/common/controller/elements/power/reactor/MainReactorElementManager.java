package org.schema.game.common.controller.elements.power.reactor;

import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.IntegrityBasedInterface;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class MainReactorElementManager extends UsableControllableSingleElementManager<MainReactorUnit, MainReactorCollectionManager, MainReactorElementManager> implements IntegrityBasedInterface{

	public MainReactorElementManager(SegmentController segmentController, Class<MainReactorCollectionManager> clazz) {
		super(segmentController, clazz);
	}

	@Override
	public void onControllerChange() {
		
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(MainReactorUnit firingUnit, MainReactorCollectionManager col,
			ControlBlockElementCollectionManager<?, ?, ?> supportCol,
			ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		assert (false);
		throw new IllegalArgumentException();
	}

	@Override
	protected String getTag() {
		return "mainreactor";
	}

	@Override
	public MainReactorCollectionManager getNewCollectionManager(SegmentPiece position,
			Class<MainReactorCollectionManager> clazz) {
		return new MainReactorCollectionManager(getSegmentController(), this);
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

	@Override
	public int getPriority() {
		return 30;
	}
	
}
