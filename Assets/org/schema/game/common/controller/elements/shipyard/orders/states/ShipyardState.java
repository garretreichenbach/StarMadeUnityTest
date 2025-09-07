package org.schema.game.common.controller.elements.shipyard.orders.states;

import api.listener.events.state.ShipyardEnterStateEvent;
import api.mod.StarLoader;
import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

import java.util.logging.Level;

public abstract class ShipyardState extends State  {

	
	
	private int ticksDone;

	private long startTime;
	
	public long loadedStartTime = -1;
	public int loadedTicksDone = -1;
	
	private boolean loadedFromTag;
	
	public ShipyardState(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public ShipyardEntityState getEntityState() {
		return (ShipyardEntityState) super.getEntityState();
	}

	
	@Override
	public void stateTransition(Transition t) throws FSMException {
		if(ShipyardCollectionManager.DEBUG_MODE){
			try{
				throw new Exception("!!!!DEBUG MODE!!!! AUTO EXCEPTION :: TRANSITION :: "+this.getClass().getSimpleName()+" using Transition "+t.name());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		try{
			throw new Exception("!!!!DEBUG MODE!!!! AUTO EXCEPTION :: TRANSITION :: "+this.getClass().getSimpleName()+" using Transition "+t.name());
		}catch(Exception e){
			LogUtil.sy().log(Level.FINE, getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+" using Transition "+t.name(), e);
		}
		
		super.stateTransition(t);
	}

	public abstract boolean onEnterS();
	
	@Override
	public final boolean onEnter() {
		LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": ON_ENTER_STATE");
		if(loadedStartTime > 0){
			startTime = loadedStartTime;
			loadedStartTime = -1;
		}else{
			startTime = System.currentTimeMillis();
		}
		
		if(loadedTicksDone >= 0){
			
			ticksDone = loadedTicksDone;
			loadedTicksDone = -1;
		}else{
			ticksDone = 0;
		}
		getEntityState().setCompletionOrderPercentAndSendIfChanged(0.0f);
		boolean r = onEnterS();
		loadedFromTag = false;
		//INSERTED CODE @77
		StarLoader.fireEvent(new ShipyardEnterStateEvent(this), true);
		///
		return r;
	}


	private long getTickRate(){
		return 1000;
	}
	
	public double getTickCount(){
		long t = System.currentTimeMillis() - startTime;
		return t / getTickRate();
	}
	public int getTicksDone(){
		int t = (int) getTickCount();
		
		int ticks = 0;
		
		if(t > ticksDone){
			ticks = t - ticksDone;
			ticksDone = t;	
		}
		
		return ticks;
	}

	public boolean canCancel() {
		return false;
	}

	public boolean isLoadedFromTag() {
		return loadedFromTag;
	}

	public void setLoadedFromTag(boolean loadedFromTag) {
		this.loadedFromTag = loadedFromTag;
	}

	public String getClientShortDescription() {
		return getClass().getSimpleName();
	}

	public boolean canEdit() {
		return false;
	}

	public boolean hasBlockGoal() {
		return false;
	}
	
	public boolean canUndock() {
		return false;
	}

	public boolean isPullingResources() {
		return false;
	}

	public void pullResources() {
	}

	public void onShipyardRemoved(Vector3i shipyardControllerPos) {
	}

	public long getStartTime() {
		return startTime;
	}
}
