package org.schema.game.common.controller.elements.rail.pickup;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetFormationingAbstract;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

public class RailPickupUnit extends ElementCollection<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> {

	
	private Long2LongOpenHashMap lastUsed = new Long2LongOpenHashMap();

	public void activate(SegmentPiece piece, boolean active) {
		//only on server
		//this is still in synchronize(getBlockActivationBuffer())
		//since its called from within
		long index = piece.getAbsoluteIndex();
		long d;
		if (active) {
			d = ElementCollection.getActivation(index, false, false);
			assert (ElementCollection.getType(d) < 10);
		} else {
			d = ElementCollection.getDeactivation(index, false, false);
			assert (ElementCollection.getType(d) < 10);
		}
		((SendableSegmentController) getSegmentController()).getBlockActivationBuffer().enqueue(d);

	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Rail Pickup Module"), this);
	}

	public boolean isActive() {
		if (getElementCollectionId() != null) {
			getElementCollectionId().refresh();
			return getElementCollectionId().isActive();
		}
		return false;
	}
	public void update(Timer timer) {
		if(!getSegmentController().isOnServer()){
			ObjectCollection<SimpleTransformableSendableObject<?>> values = ((GameClientState)getSegmentController().getState()).getCurrentSectorEntities().values();
			
			if(((GameClientState)getSegmentController().getState()).getShip() != null){
				Ship s = ((GameClientState)getSegmentController().getState()).getShip();
				handle(s);
			}
		}else{
			Sector sec = ((GameServerState)getSegmentController().getState()).getUniverse().getSector(getSegmentController().getSectorId());
			if(sec != null){
				for(SimpleTransformableSendableObject<?> s : sec.getEntities()){
					if(s instanceof SegmentController && ((SegmentController)s).railController.isRoot() && (!(s instanceof PlayerControllable) || ((PlayerControllable)s).getAttachedPlayers().isEmpty())){
						handle((SegmentController) s);
					}
				}
			}
		}
	}
	private void handle(SegmentController s){
		if(!getSegmentController().isVirtualBlueprint() && s != getSegmentController() && s instanceof Ship  && ((Ship)s).railController.isRoot() ){
			boolean docked = checkRaildockingProximity((Ship)s);
			if(docked){
				
//				System.err.println("["+getSegmentController().getState()+"][PICKUPAREA] SUCCESS PICKUP "+s+" on "+getSegmentController());
				return;
			}else{
//				System.err.println("[CLIENT][PICKUPAREA] FAILED PICKUP "+s+" on "+getSegmentController());
			}
		}
	}
	Vector3f dockerPos = new Vector3f();
	Vector3f railAreaPos = new Vector3f();
	private boolean checkRaildockingProximity(Ship s) {
		LongSet railDockers = s.getManagerContainer().getRailBeam().getRailDockers();
		for(long railPickId : getNeighboringCollection()){
		
			if(isActive(railPickId)){
			
				ElementCollection.getPosFromIndex(railPickId, railAreaPos);
				railAreaPos.x -= SegmentData.SEG_HALF;
				railAreaPos.y -= SegmentData.SEG_HALF;
				railAreaPos.z -= SegmentData.SEG_HALF;
				
				getSegmentController().getWorldTransform().transform(railAreaPos);
				
				for(long dockerId : railDockers){
					
					ElementCollection.getPosFromIndex(dockerId, dockerPos);
					dockerPos.x -= SegmentData.SEG_HALF;
					dockerPos.y -= SegmentData.SEG_HALF;
					dockerPos.z -= SegmentData.SEG_HALF;
					
					s.getWorldTransform().transform(dockerPos);
					
					
					
					if(isInPickupProximity(dockerPos, railAreaPos)){
						dock(s, dockerId, railPickId);
						return true;
					}
					
				}
			}
		}
		
		return false;
	}
	private void dock(Ship s, long dockerId, long railTo) {
		SegmentPiece docker = s.getSegmentBuffer().getPointUnsave(dockerId, new SegmentPiece());
		
		SegmentPiece pickupArea = getSegmentController().getSegmentBuffer().getPointUnsave(railTo, new SegmentPiece());
		
		if(docker != null && pickupArea != null && System.currentTimeMillis() - lastUsed.get(railTo) > 3000){
			for(int i = 0; i < 6; i++){
				Vector3i side = Element.DIRECTIONSi[i];
				Vector3i p = new Vector3i();
				pickupArea.getAbsolutePos(p);
				
				p.add(side);
				
				
				SegmentPiece toRail = getSegmentController().getSegmentBuffer().getPointUnsave(p, new SegmentPiece());
				if(ElementKeyMap.isValidType(toRail.getType()) && ElementKeyMap.getInfoFast(toRail.getType()).isRailTrack()){
					
					if(getSegmentController().isOnServer()){
						if(pickupArea.getAbsoluteIndex() != s.lastPickupAreaUsed && hasFleetFormatingState((s))){
							//dont pick up in formation if this is not our pickup point
						}else{
							s.lastPickupAreaUsed = pickupArea.getAbsoluteIndex();
							s.getNetworkObject().lastPickupAreaUsed.add(s.lastPickupAreaUsed);
							s.railController.connectServer(docker, toRail);
							lastUsed.put(railTo, System.currentTimeMillis());
						}
					}else{
						s.lastPickupAreaUsed = pickupArea.getAbsoluteIndex();
						s.getNetworkObject().lastPickupAreaUsed.add(s.lastPickupAreaUsed);
						s.railController.connectClient(docker, toRail);
						lastUsed.put(railTo, System.currentTimeMillis());
					}
					
					
					return;
				}
			}
		
			
		}
	}
	private boolean hasFleetFormatingState(Ship ship) {
		return ship.getAiConfiguration() != null &&
				ship.getAiConfiguration().getAiEntityState() != null &&
				ship.getAiConfiguration().getAiEntityState().getCurrentProgram() != null &&
				ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine() != null &&
				ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm() != null &&
				ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() != null &&
				ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetFormationingAbstract;
	}
	private boolean isInPickupProximity(Vector3f a,
			Vector3f b) {
		float dist = Vector3fTools.diffLength(a, b);
//		System.err.println("DIST ::: "+dist);
		return  dist < 3;
	}
	SegmentPiece test = new SegmentPiece();
	public boolean isActive(long railPickId) {
		SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(railPickId, test);
		return pointUnsave != null && pointUnsave.isActive();
	}

}