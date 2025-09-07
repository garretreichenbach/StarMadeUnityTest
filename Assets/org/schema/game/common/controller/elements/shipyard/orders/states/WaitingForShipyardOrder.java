package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

public class WaitingForShipyardOrder extends ShipyardState{

	private long startedWaitingLocal;
	public WaitingForShipyardOrder(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		getEntityState().getShipyardCollectionManager().setCompletionOrderPercent(-1);
		getEntityState().getShipyardCollectionManager().sendShipyardStateToClient();
		
		
		startedWaitingLocal = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		SegmentController currentDocked = getEntityState().getCurrentDocked();
		if( currentDocked != null){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Waiting for design but got an unexpected dock: "+currentDocked);
			if(!currentDocked.isMarkedForDeleteVolatile()){
				if(!currentDocked.isVirtualBlueprint() ){
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Waiting for design but got an unexpected dock: "+currentDocked+" -> normal dock");
					stateTransition(Transition.SY_LOAD_NORMAL);
				}else{
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Waiting for design but got an unexpected dock: "+currentDocked+" -> design");
					boolean found = false;
					//virtual design. look if we can find the design
					for(int slot : getEntityState().getInventory().getSlots()){
						int meta = getEntityState().getInventory().getMeta(slot);
						MetaObject o;
						if(meta >= 0 && (o = ((GameServerState)getEntityState().getState()).getMetaObjectManager().getObject(meta)) != null && o instanceof VirtualBlueprintMetaItem){
							VirtualBlueprintMetaItem m = (VirtualBlueprintMetaItem)o;
							
							if(m.UID.equals(currentDocked.getUniqueIdentifier())){
								getEntityState().designToLoad = o.getId();
								
								assert(getMachine().getFsm().getCurrentState() == this);
								LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Waiting for design but got an unexpected dock: "+currentDocked+" DESIGN DOCK AND DESIGN FOUND IN INVENTORY");
								System.err.println("[SERVER][SHIPYARD] there is a design docked but no design loaded. Design found in inventory: "+currentDocked);
								stateTransition(Transition.SY_LOAD_DESIGN);
								found = true;
								break;
							}
						}
					}
					if(!found){
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Waiting for design but got an unexpected dock: "+currentDocked+" -> DESIGN DOCKED BUT DESIGN NOT FOUND IN INV: unload design");
						getEntityState().unloadCurrentDockedVolatile();
					}
				}
			}
		}
		return false;
	}
	@Override
	public String getClientShortDescription() {
		return Lng.str("Waiting for Order");
	}
}
