package org.schema.game.server.ai;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.fleet.missions.machines.states.Timeout;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.server.ai.program.common.states.SegmentControllerGameState;
import org.schema.schine.ai.stateMachines.AIGameEntityState;

public abstract class SegmentControllerAIEntity<E extends SegmentController> extends AIGameEntityState<E> {

	/**
	 *
	 */
	public Timeout engagingTimeoutQueued;
	private Timeout engagingTimeoutEngaged;

	public SegmentControllerAIEntity(String name, E s) {
		super(name, s);
	}
	@Override
	public boolean isActive() {
		return super.isActive() && !getEntity().isVirtualBlueprint();
	}
	public static class ManualUsable{
		public final PlayerUsableInterface p;
		public final ControllerStateUnit u;
		public ManualUsable(PlayerUsableInterface p, ControllerStateUnit u) {
			super();
			this.p = p;
			this.u = u;
		}
		
	}
	public ManualUsable getManualPlayerUsable() {
		if(((AIGameConfiguration)getAIConfig()).get(Types.MANUAL).isOn()){
			if(getEntity().railController.isDockedAndExecuted()){
				final SegmentController root = getEntity().railController.getRoot();
				ManagerContainer<?> o = ((ManagedSegmentController<?>)root).getManagerContainer();
				
				if(root instanceof PlayerControllable && !(((PlayerControllable)root).getAttachedPlayers()).isEmpty()){
					for(ControllerStateUnit u : ((PlayerControllable)root).getAttachedPlayers().get(0).getControllerState().getUnits()){
						PlayerUsableInterface playerUsable = o.getPlayerUsable(PlayerUsableInterface.USABLE_ID_SHOOT_TURRETS);
						
						if(playerUsable != null && u.isSelected(playerUsable, ((ManagedSegmentController<?>)root).getManagerContainer()) && u.playerControllable == root){
							return new ManualUsable(playerUsable, u);
						}
					}
				}
			}
		}
		return null;					
	}
	public boolean isTimeout(){
		if(engagingTimeoutEngaged != null){
			Vector3i sector = getEntity().getSector(new Vector3i());
			
			return engagingTimeoutEngaged.isTimeout(sector);
			
			
		}
		return false;
	}
	
	public void engageTimeout(){
		engagingTimeoutEngaged = engagingTimeoutQueued; //will set to null on second attempt
		engagingTimeoutQueued = null;
	}
	public boolean hadTimeout() {
		return engagingTimeoutEngaged != null && engagingTimeoutEngaged.wasTimedOut;
	}
	public void onShot(SegmentControllerGameState<?> currentState) {
		if(engagingTimeoutEngaged != null){
			engagingTimeoutEngaged.onShot(currentState);
		}
		
	}
	public void onTimeout(SegmentControllerGameState<?> currentState) {
		if(engagingTimeoutEngaged != null){
			engagingTimeoutEngaged.onTimeout(currentState);
		}
		
	}
	public void onNoTargetFound(SegmentControllerGameState<?> currentState) {
		if(engagingTimeoutEngaged != null){
			engagingTimeoutEngaged.onNoTargetFound(currentState);
		}		
	}
}
