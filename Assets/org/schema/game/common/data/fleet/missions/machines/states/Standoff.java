package org.schema.game.common.data.fleet.missions.machines.states;

import api.common.GameCommon;
import api.common.GameServer;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

/**
 * Standoff Fleet State.
 * <p>When assigned to this mode, fleet ships will attempt to keep enemies at a distance and avoid close combat.</p>
 * <p>If a fleet ship has low shields, it will attempt to disengage.</p>
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class Standoff extends FleetState {

	private static final float MAX_DISTANCE_OFFSET = 3000f;
	private static final float BACKOFF_SHIELD_PERCENT = 0.35f; //Todo: Make this user configurable
	private static final ArrayList<Ship> backoffList = new ArrayList<>();

	public Standoff(Fleet fleet) {
		super(fleet);
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.STANDOFF;
	}

	@Override
	public boolean onEnterFleetState() {
		try {
			restartAllLoaded();
		} catch(FSMException exception) {
			exception.printStackTrace();
		}
		return super.onEnterFleetState();
	}

	@Override
	public boolean onUpdate() throws FSMException {
		SegmentController nearestEnemy = getNearestEnemy();
		if(nearestEnemy != null) {
			float distance = getDistance(nearestEnemy);
			if(distance == Float.MAX_VALUE) return false;
			else {
				for(FleetMember fleetMember : getEntityState().getMembers()) {
					if(fleetMember.isLoaded() && fleetMember.getLoaded() instanceof Ship) {
						Ship ship = (Ship) fleetMember.getLoaded();
						float memberDistance = Vector3fTools.distance(fleetMember.getLoaded().getWorldTransform().origin.x, fleetMember.getLoaded().getWorldTransform().origin.y, fleetMember.getLoaded().getWorldTransform().origin.z, nearestEnemy.getWorldTransform().origin.x, nearestEnemy.getWorldTransform().origin.y, nearestEnemy.getWorldTransform().origin.z);
						Vector3f moveDir = Vector3fTools.sub(nearestEnemy.getWorldTransform().origin, fleetMember.getLoaded().getWorldTransform().origin);
						moveDir.normalize();
						if(ship.getManagerContainer().getShieldAddOn().getPercentOne() <= BACKOFF_SHIELD_PERCENT) {
							if(!backoffList.contains(ship)) {
								moveDir.scale(memberDistance + MAX_DISTANCE_OFFSET);
								Vector3fTools.add(moveDir, fleetMember.getLoaded().getWorldTransform().origin);
								ship.getAiConfiguration().getAiEntityState().moveTo(GameServer.getServerState().getController().getTimer(), moveDir, true);
								System.err.println("[STANDOFF] " + ship + " is backing off due to low shields!");
								backoffList.add(ship);
							}
						} else {
							((TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram()).setTarget(nearestEnemy);
							backoffList.remove(ship);
						}
					}
				}
			}
		}
		return true;
	}

	private SegmentController getNearestEnemy() {
		if(getEntityState().getFlagShip().isLoaded()) {
			SegmentController nearestEnemy = null;
			float nearestDistance = Float.MAX_VALUE;
			for(Sendable sendable : getEntityState().getFlagShip().getLoaded().getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(sendable instanceof SegmentController) {
					SegmentController segmentController = (SegmentController) sendable;
					if(segmentController.getFactionId() != getEntityState().getFlagShip().getFactionId() && GameCommon.getGameState().getFactionManager().isEnemy(getEntityState().getFlagShip().getFactionId(), segmentController.getFactionId())) {
						float distance = getDistance(segmentController);
						if(distance < nearestDistance) {
							nearestDistance = distance;
							nearestEnemy = segmentController;
						}
					}
				}
			}
			return nearestEnemy;
		}
		return null;
	}

	private float getDistance(SegmentController segmentController) {
		if(getEntityState().getFlagShip().isLoaded()) return Vector3fTools.distance(getEntityState().getFlagShip().getLoaded().getWorldTransform().origin.x, getEntityState().getFlagShip().getLoaded().getWorldTransform().origin.y, getEntityState().getFlagShip().getLoaded().getWorldTransform().origin.z, segmentController.getWorldTransform().origin.x, segmentController.getWorldTransform().origin.y, segmentController.getWorldTransform().origin.z);
		else return Float.MAX_VALUE;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public Transition getMoveTrasition(){
		return Transition.FLEET_STANDOFF;
	}
}
