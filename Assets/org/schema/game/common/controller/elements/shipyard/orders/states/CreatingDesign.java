package org.schema.game.common.controller.elements.shipyard.orders.states;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.LogUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;

import java.sql.SQLException;
import java.util.List;

public class CreatingDesign extends ShipyardState{

	


	public CreatingDesign(ShipyardEntityState gObj) {
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
		SegmentController currentDocked = getEntityState().getCurrentDocked();
		if(currentDocked != null){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Cannot create design: shipyard occupied");
			getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design! Shipyard occupied!"));
			stateTransition(Transition.SY_ERROR);
		}else if(!getEntityState().getInventory().hasFreeSlot()){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Cannot create design: no slot free in shipyard computer");
			getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design!\nNo slot free in shipyard computer."));
			stateTransition(Transition.SY_ERROR);
		}else{
			
			boolean exists = false;
			
			Sendable sendable = getEntityState().getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(EntityType.SHIP.dbPrefix+getEntityState().currentName);
			
			exists = sendable != null;
			
			if(!exists){
				try {
					List<DatabaseEntry> byUIDExact = ((GameServerState)getEntityState().getState()).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(getEntityState().currentName, 1);
					
					exists = !byUIDExact.isEmpty();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(exists){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Cannot create: Design name already exists");
				getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design!\nName already exists."));
				stateTransition(Transition.SY_ERROR);
				return false;
			}
			
			double l = getTickCount();
			if(l >= 1.0f){
				getEntityState().setCompletionOrderPercentAndSendIfChanged(1.0f);
				
				Transform tr = new Transform();
				tr.setIdentity();
				float[] mat = new float[16];
				tr.getOpenGLMatrix(mat);
				
				Ship newShip = EntityRequest.getNewShip(
						(ServerStateInterface) getEntityState().getState(), 
						EntityType.SHIP.dbPrefix+getEntityState().currentName, 
						getEntityState().getSegmentController().getSectorId(), 
						getEntityState().currentName, 
						mat, 
						-2, 
						-2, 
						-2, 
						2, 
						2, 
						2, 
						"SHIPYARD_"+getEntityState().getSegmentController().getUniqueIdentifier(), false);
				
				newShip.setTouched(true, false);
				
				RemoteSegment s = new RemoteSegment(newShip);
				s.setSegmentData(new SegmentData4Byte(newShip.getState() instanceof ClientStateInterface));
				s.getSegmentData().setSegment(s);
				try{
					s.getSegmentData().setInfoElementUnsynched((byte) Ship.core.x, (byte) Ship.core.y, (byte) Ship.core.z, ElementKeyMap.CORE_ID, true, s.getAbsoluteIndex((byte) 8, (byte) 8, (byte) 8), newShip.getState().getUpdateTime());
				} catch (SegmentDataWriteException e) {
					throw new RuntimeException("Should always be normal data", e);
				}
				s.setLastChanged(System.currentTimeMillis());
				newShip.getSegmentBuffer().addImmediate(s);
				newShip.getSegmentBuffer().updateBB(s);
				
				
				
				
				boolean b = getEntityState().getShipyardCollectionManager().createDockingRelation(newShip, true);
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Creating design: ship spawned: docked: "+newShip+"; DOCKING: "+b);
				
				if(b){
					
					((GameServerState)getEntityState().getState()).getMetaObjectManager();
					VirtualBlueprintMetaItem m = (VirtualBlueprintMetaItem) MetaObjectManager.instantiate(MetaObjectType.VIRTUAL_BLUEPRINT.type, (short) -1, true);
					
					int slot = -1;
					try {
						slot = getEntityState().getInventory().getFreeSlot();
					
						m.UID = newShip.getUniqueIdentifier();
						m.virtualName = new String(getEntityState().currentName);
						
						getEntityState().getInventory().put(slot, m);
						getEntityState().getInventory().sendInventoryModification(slot);
						
						getEntityState().getShipyardCollectionManager().setCurrentDesign(m.getId());
						
						newShip.initialize();
					
						getEntityState().getShipyardCollectionManager().sendShipyardStateToClient();
						
						((GameServerState)getEntityState().getState()).getController().getSynchController().addNewSynchronizedObjectQueued(newShip);
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Creating design: loaded and docked successfully "+newShip);
						stateTransition(Transition.SY_LOADING_DONE);
					
					} catch (NoSlotFreeException e) {
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Critical Server Error No slot while space reserved ");
						e.printStackTrace();
						getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nCritical Server Error\nNo slot while space reserved"));
						stateTransition(Transition.SY_ERROR);
					}
				}else{
					stateTransition(Transition.SY_ERROR);
				}
			}else{
				getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged((float)l);
				//wait;
			}
				
			
		}
		return false;
	}
	@Override
	public String getClientShortDescription() {
		return Lng.str("Creating Design");
	}
}
