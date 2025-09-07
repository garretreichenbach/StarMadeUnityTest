package org.schema.game.common.controller.elements;

import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;

public abstract class SegmentControllerUsable implements PlayerUsableInterface{
	public final SegmentController segmentController;
	public final ManagerContainer<?> man;
	public SegmentControllerUsable(ManagerContainer<?> man){
		this(man, man.getSegmentController());
	}
	public SegmentControllerUsable(ManagerContainer<?> man, SegmentController self) {
		this.segmentController = self;
		this.man = man;
	}
	public SegmentController getSegmentController() {
		return segmentController;
	}
	public ManagerContainer<?> getContainer() {
		return man;
	}

	public StateInterface getState() {
		return segmentController.getState();
	}
	public abstract String getWeaponRowName();
	public abstract short getWeaponRowIcon();
	@Override
	public boolean isAddToPlayerUsable() {
		return true;
	}
	public int getMaxCharges() {
		return 0;
	}
	public int getCharges() {
		return 0;
	}
	@Override
	public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState,
			PlayerControllable newAttached){
		
	}
	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		
	}
	@Override
	public float getWeaponSpeed() {
		return 0;
	}
	@Override
	public float getWeaponDistance() {
		return 0;
	}
	@Override
	public void onSwitched(boolean on) {
		
	}
	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
	}
	
}
