package org.schema.game.common.data.fleet.missions.machines.states;

import api.common.GameCommon;
import api.utils.game.SegmentControllerUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.beam.repair.RepairElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetMovingToSector;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Repairing fleet state.
 * <p>When assigned to this mode, fleet ships will repair damaged allies if possible, prioritizing player controlled ships.</p>
 * <p>If the fleet is in combat or near enemies, will try to disengage and move to a safe distance first if possible.</p>
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class Repairing extends FleetState {
	private SegmentController repairTarget;
	private final PriorityQueue<RepairTarget> repairTargets = new PriorityQueue<>();

	public Repairing(Fleet fleet) {
		super(fleet);
	}

	@Override
	public boolean onEnterFleetState() {
		try {
			restartAllLoaded();
		} catch (FSMException e) {
			e.printStackTrace();
		}
		return super.onEnterFleetState();
	}

	@Override
	public boolean onUpdate() throws FSMException {
		if(isDisengaged()) {
			if(repairTargets.isEmpty()) {
				for(FleetMember member : getEntityState().getMembers()) {
					if(member.isLoaded() && !member.getLoaded().equals(repairTarget)) {
						if(member.getLoaded() instanceof Ship && ((Ship) member.getLoaded()).getReactorHp() < ((Ship) member.getLoaded()).getReactorHpMax()) {
							repairTargets.add(new RepairTarget(member.getLoaded(), 0));
							if(member.getLoaded().isConrolledByActivePlayer()) repairTargets.add(new RepairTarget(member.getLoaded(), 0));
							else repairTargets.add(new RepairTarget(member.getLoaded(), 1));
							Ship ship = (Ship) member.getLoaded();
							ship.getAiConfiguration().getAiEntityState().stop();
							ship.getAiConfiguration().getAiEntityState().getCurrentProgram().suspend(true);
						}
					}
				}
			}

			for(RepairTarget target : repairTargets) {
				if(!(target.getSegmentController() instanceof Ship) || ((Ship) target.getSegmentController()).getReactorHp() >= ((Ship) target.getSegmentController()).getReactorHpMax()) repairTargets.remove(target);
			}

			if(repairTarget == null || (repairTarget instanceof Ship && ((Ship) repairTarget).getReactorHp() >= ((Ship) repairTarget).getReactorHpMax())) {
				repairTarget = null;
				if(!repairTargets.isEmpty()) repairTarget = repairTargets.poll().getSegmentController();
			}

			if(repairTarget instanceof Ship) {
				((Ship) repairTarget).getAiConfiguration().getAiEntityState().stop();
				((TargetProgram<?>) ((Ship) repairTarget).getAiConfiguration().getAiEntityState().getCurrentProgram()).setTarget(null);
			} else {
				stateTransition(Transition.RESTART);
				getEntityState().onCommandPartFinished(this);
				getEntityState().sendFleetCommand(FleetCommandTypes.ESCORT); //Todo: Add a default command setting for fleets
				return false;
			}

			for(FleetMember member : getEntityState().getMembers()) {
				if(!(member.getLoaded() instanceof Ship)) continue;
				if(hasRepairFunctionality(member)) {
					if(repairTarget != null && repairTarget != member.getLoaded()) {
						Ship ship = (Ship) member.getLoaded();
						if((((TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram()).getTarget() instanceof Ship)) {
							Ship prevTarget = (Ship) ((TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram()).getTarget();
							//if(prevTarget.getReactorHp() < prevTarget.getReactorHpMax()) prevTarget.getManagerContainer().getPowerInterface().requestRecalibrate();
							try {
								prevTarget.getAiConfiguration().getAiEntityState().getCurrentProgram().suspend(false);
								prevTarget.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
							} catch(FSMException ignored) { }
						}
						ship.getNetworkObject().targetType.set(SimpleGameObject.MINABLE);
						((TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram()).setTarget(repairTarget);
						if(repairTarget instanceof Ship) {
							Ship targetShip = (Ship) repairTarget;
							targetShip.getAiConfiguration().getAiEntityState().stop();
							targetShip.getAiConfiguration().getAiEntityState().getCurrentProgram().suspend(true);
						}
						try {
							ship.getAiConfiguration().getAiEntityState().getCurrentProgram().suspend(false);
							ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
						} catch(FSMException ignored) {}
						//ship.getAiConfiguration().getAiEntityState().getCurrentProgram().suspend(false);
					}
				} else {
					Ship ship = (Ship) member.getLoaded();
					ship.getAiConfiguration().getAiEntityState().stop();
				}
			}
		} else {
			Vector3i retreatTo = new Vector3i(getEntityState().getFlagShip().getSector());
			//Add a random offset by -1 or 1 sectors
			retreatTo.x += (int) (Math.random() * 3) - 1;
			retreatTo.y += (int) (Math.random() * 3) - 1;
			retreatTo.z += (int) (Math.random() * 3) - 1;

			//If the retreat position is the same as the current position, add 1 to the x coordinate
			if(retreatTo.equals(getEntityState().getFlagShip().getSector())) retreatTo.x += 1;
			//If the retreat position is the same as the current position, add 1 to the y coordinate
			if(retreatTo.equals(getEntityState().getFlagShip().getSector())) retreatTo.y += 1;
			//If the retreat position is the same as the current position, add 1 to the z coordinate
			if(retreatTo.equals(getEntityState().getFlagShip().getSector())) retreatTo.z += 1;

			for(FleetMember member : getEntityState().getMembers()) {
				if(member.isLoaded()) {
					try {
						((TargetProgram<?>) ((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram()).setSectorTarget(retreatTo);
						member.moveTarget = retreatTo;
						if(!(((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetMovingToSector)) ((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.MOVE_TO_SECTOR);
					} catch(FSMException ignored) {}
				} else member.moveRequestUnloaded(getEntityState(), retreatTo);
			}
		}
		return true;
	}

	private boolean isDisengaged() {
		if(getEntityState().getFlagShip() != null) {
			Ship flagShip = (Ship) getEntityState().getFlagShip().getLoaded();
			for(Sendable sendable : flagShip.getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(sendable instanceof SegmentController) {
					SegmentController segmentController = (SegmentController) sendable;
					if(segmentController.getFactionId() != flagShip.getFactionId() && GameCommon.getGameState().getFactionManager().isEnemy(segmentController.getFactionId(), flagShip.getFactionId())) {
						if(Vector3i.getDisatance(flagShip.getSector(new Vector3i()), segmentController.getSector(new Vector3i())) < 1.5) return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	private boolean hasRepairFunctionality(FleetMember member) {
		return member.isLoaded() && member.getLoaded() instanceof ManagedSegmentController<?> && SegmentControllerUtils.getElementManager((ManagedUsableSegmentController<?>) member.getLoaded(), RepairElementManager.class) != null && Objects.requireNonNull(SegmentControllerUtils.getElementManager((ManagedUsableSegmentController<?>) member.getLoaded(), RepairElementManager.class)).totalSize > 0;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.REPAIRING;
	}

	public static class RepairTarget implements Comparable<RepairTarget> {

		private final SegmentController segmentController;
		private final long priority;

		public RepairTarget(SegmentController segmentController, long priority) {
			this.segmentController = segmentController;
			this.priority = priority;
		}

		public SegmentController getSegmentController() {
			return segmentController;
		}

		public long getPriority() {
			return priority;
		}

		@Override
		public int compareTo(RepairTarget o) {
			return Integer.compare((int) o.priority, (int) priority);
		}
	}
}
