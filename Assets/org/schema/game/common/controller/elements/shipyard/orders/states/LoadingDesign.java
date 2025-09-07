package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

public class LoadingDesign extends ShipyardState{

	private long loadedDesignStart;
	private boolean requested;

	public LoadingDesign(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		requested = false;
		loadedDesignStart = 0;
		getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged(0.5f);
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loading design!");
		System.err.println("[SERVER][SHIPYARD] LOADING DESIGN onUpdate() : ID: "+getEntityState().designToLoad );
		if(loadedDesignStart > 0 && (System.currentTimeMillis() - loadedDesignStart > 5000 || getEntityState().getCurrentDocked() != null)){
			SegmentPiece p;
			if(getEntityState().getCurrentDocked() == null){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loading design timeout!");
				System.err.println("[SERVER][SHIPYARD][ERROR] timeout for loading design!");
				getEntityState().sendShipyardErrorToClient(Lng.str("Loading design timed out!"));
				stateTransition(Transition.SY_ERROR);
			}else if((( p = getEntityState().getCurrentDocked().getSegmentBuffer().getPointUnsave(Ship.core)) != null) && p.getType() == ElementKeyMap.CORE_ID){//autorequest true previously
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loading design DONE!");
				System.err.println("[SERVER][SHIPYARD] DONE LOADING!");
				getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged(1.0f);
				stateTransition(Transition.SY_LOADING_DONE);
			}else if(!requested) {
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loading design NO CORE -> FORCE REQUEST!");
				getEntityState().getCurrentDocked().getSegmentProvider().enqueueHightPrio(0, 0, 0, true);
				requested = true;
			}else if(System.currentTimeMillis() - loadedDesignStart > 20000){
				
				
				
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loading design NO CORE!");
				getEntityState().sendShipyardErrorToClient(Lng.str("No core on design while loading!"));
				System.err.println("[SERVER][SHIPYARD][ERROR] no core on design: "+getEntityState().getCurrentDocked());
			}
		}else{
			MetaObject o;
			if(getEntityState().designToLoad >= 0 && 
					(o = ((MetaObjectState)getEntityState().getState()).getMetaObjectManager().getObject(getEntityState().designToLoad)) != null &&
					o instanceof VirtualBlueprintMetaItem){
				double l = getTickCount();
				if(l >= 1.0f){
					SegmentController loadDesign = getEntityState().loadDesign((VirtualBlueprintMetaItem)o);
					
					getEntityState().designToLoad = -1;
					
					if(loadDesign != null){
						getEntityState().getShipyardCollectionManager().createDockingRelation(loadDesign, true);
						getEntityState().getShipyardCollectionManager().setCurrentDesign(o.getId());
						getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged(0.8f);
						loadedDesignStart = System.currentTimeMillis();
						System.err.println("[SERVER][SHIPYARD] Loading design started of "+loadDesign);
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Loading deisgn started: "+loadDesign);
					}else{
						System.err.println("[SERVER][SHIPYARD][ERROR] Loading design failed: meta: "+getEntityState().designToLoad+" -> "+((MetaObjectState)getEntityState().getState()).getMetaObjectManager().getObject(getEntityState().designToLoad));
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Loading deisgn failed: "+getEntityState().designToLoad);
						getEntityState().getShipyardCollectionManager().setCurrentDesign(-1);
						stateTransition(Transition.SY_ERROR);
					}
				}else{
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loading design: "+l);
					
					System.err.println("[SERVER][SHIPYARD] LOADING DESIGN ::: "+l);
					getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged((float)l);
				}
			}else{
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": CANNOT LOAD DESIGN META! "+getEntityState().designToLoad+((MetaObjectState)getEntityState().getState()).getMetaObjectManager().getObject(getEntityState().designToLoad));
				System.err.println("[SERVER][SHIPYARD][ERROR] Cannot load design: meta: "+getEntityState().designToLoad+" -> "+((MetaObjectState)getEntityState().getState()).getMetaObjectManager().getObject(getEntityState().designToLoad));
				getEntityState().getShipyardCollectionManager().setCurrentDesign(-1);
				getEntityState().designToLoad = -1;
				stateTransition(Transition.SY_ERROR);
			}
		}
		return false;
	}

}
