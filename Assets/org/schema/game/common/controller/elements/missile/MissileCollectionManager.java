package org.schema.game.common.controller.elements.missile;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FocusableUsableModule;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

public abstract class MissileCollectionManager<E extends MissileUnit<E, CM, EM>, CM extends MissileCollectionManager<E, CM, EM>, EM extends MissileElementManager<E, CM, EM>> extends ControlBlockElementCollectionManager<E, CM, EM> implements FocusableUsableModule{

	
	
	
	public MissileCollectionManager(SegmentPiece controllerElement, short clazz, SegmentController segController,
			EM em) {
		super(controllerElement, clazz, segController, em);
	}
	private FireMode mode = FireMode.getDefault(this.getClass());
	@Override
	public boolean isInFocusMode() {
		return mode == FireMode.FOCUSED;
	}

	@Override
	public void setFireMode(FireMode mode) {
		this.mode = mode; 		
	}
	@Override
	public FireMode getFireMode() {
		return mode;
	}
	public boolean isAllowedVolley() {
		return true;
	}
	public boolean isVolleyShot() {
		if(getSegmentController().isAIControlled() && getSegmentController() instanceof  SegmentControllerAIInterface &&
				((SegmentControllerAIInterface)getSegmentController()).getAiConfiguration().isActiveAI() && 
				((AIGameConfiguration<?, ?>)((SegmentControllerAIInterface)getSegmentController()).getAiConfiguration()).get(Types.FIRE_MODE).getCurrentState().equals("Volley")) {
			return true;
		}
		return mode == FireMode.VOLLEY;
	}


	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE && getSegmentController().isOnServer()) {
			handleControlShot(unit, timer);
		}
	}
}
