package org.schema.game.common.controller.elements.shipyard.orders.states;

import java.io.IOException;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.CatalogState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.BluePrintWriteQueueElement;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;

public class CreateBlueprintFromDesign extends ShipyardState{

	public CreateBlueprintFromDesign(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		
		
		
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		Sendable sendable = getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(getEntityState().designToBlueprintOwner);
		if(sendable instanceof PlayerState){
			
			if (ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt() < 0 ||
							((PlayerState)sendable).getCatalog().getPersonalCatalog().size() < ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt()) {
		
					
				if(getTickCount() > 1.0){
					
					if(getEntityState().getCurrentDesign() != null && getEntityState().getCurrentDocked() != null){
						BluePrintWriteQueueElement e = new BluePrintWriteQueueElement(
								getEntityState().getCurrentDocked(), 
								getEntityState().currentName, 
								getEntityState().currentClassification,
								false);
						try {
							String pStateName = ((PlayerState)sendable).getName();
							((CatalogState) getEntityState().getState()).getCatalogManager().writeEntryServer(e, pStateName);
							System.err.println("[SERVER][PLAYER][BLUEPRINT] " + this + " SAVED BLUEPRINT " + e.segmentController+"; Owner: "+pStateName);
							LogUtil.log().fine("[BLUEPRINT][SAVE] " + "SHIPYARD_"+getEntityState().getSegmentController().getUniqueIdentifier() + " saved: \"" + e.name + "\"");
							getEntityState().sendShipyardErrorToClient(Lng.str("Blueprint saved!"));
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": bp from design successful: "+e.name);
							stateTransition(Transition.SY_CONVERSION_DONE);
						} catch (IOException ex) {
							ex.printStackTrace();
							stateTransition(Transition.SY_ERROR);
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": bp from design failed: "+ex.getClass().getSimpleName());
						}
					}else{
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": bp from design failed: NO DESIGN LAODED");
						getEntityState().sendShipyardErrorToClient(Lng.str("Cannot save blueprint from design!\nNo design loaded!"));
						stateTransition(Transition.SY_ERROR);
					}
				}else{
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": bp from design in progress: "+getTickCount());
					getEntityState().setCompletionOrderPercentAndSendIfChanged(getTickCount());
				}
			}else{
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": bp from design failed: blueprint limit");
				getEntityState().sendShipyardErrorToClient(Lng.str("Cannot save blueprint from design!\nYou cannot save more than %d blueprints on this server!", ServerConfig.CATALOG_SLOTS_PER_PLAYER.getInt()));
				stateTransition(Transition.SY_ERROR);
			}
		}else{
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": bp from design failed: no player");
			getEntityState().sendShipyardErrorToClient(Lng.str("Cannot save blueprint from design!\nNo player found!"));
			stateTransition(Transition.SY_ERROR);
		}
		
		
		
		return false;
	}

}
