package org.schema.game.common.data.fleet.missions.machines.states;

import com.bulletphysics.linearmath.Transform;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.FloatingRockManaged;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.formation.FleetFormationSpherical;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorTransformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetFormationingAbstract;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetMining;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

import java.util.List;
import java.util.Set;

public class MiningAsteroids extends Formatoning {

	private int currentId;
	private long miningTime;
	private long miningLastTime;

	public MiningAsteroids(Fleet gObj) {
		super(gObj);
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.MINING;
	}

	@Override
	public boolean onEnterFleetState() {
		miningTime = 0;
		miningLastTime = System.currentTimeMillis();
		return super.onEnterFleetState();
	}

	@Override
	public boolean onUpdate() throws FSMException {

		FleetMember flagShip = getEntityState().getFlagShip();

		miningTime += (System.currentTimeMillis() - miningLastTime);
		miningLastTime = System.currentTimeMillis();
		if(currentId > 0) {
			Sendable sendable = getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(currentId);
			if(!(sendable instanceof SegmentController) || ((SegmentController) sendable).getTotalElements() <= 0) {
				System.err.println("[FLEET][MINING] CURRENT ID RESET SINCE OBJECT NO LONGER LOADED: " + currentId + "; Fleet: " + getEntityState());
				currentId = -1;
			} else {
				super.onUpdate();
			}
		} else {
			currentId = -1;
			if(flagShip == null || !flagShip.isLoaded()) {
			} else {
				SegmentController flag = flagShip.getLoaded();

				Sector sector = ((GameServerState) flag.getState()).getUniverse().getSector(flag.getSectorId());
				if(sector != null) {
					Set<SimpleTransformableSendableObject<?>> entities = sector.getEntities();

					for(SimpleTransformableSendableObject<?> s : entities) {
						//						System.err.println("checking "+sector+" : "+s);
						if((s instanceof FloatingRock || s instanceof FloatingRockManaged) && ((SegmentController) s).getTotalElements() > 0) {
							//FIXME !!!!getTotalElements check filters out ghost asteroids that do not exist in the game state. There is a deeper issue here.
							// also the current algo is unable to successfully 'try again' if something goes wrong, because it'll always get the same asteroid again
							currentId = s.getId();
							break;
						}
					}
					if(currentId == -1) {
						stateTransition(Transition.RESTART);
						getEntityState().onCommandPartFinished(this);
					}
				}

			}

			if(getEntityState().isNPCFleet() && (currentId == -1 || miningTime > 60000 * 5)) {
				//reset on unloaded sectors for NPC fleets to reissue move
				stateTransition(Transition.RESTART);
				getEntityState().onCommandPartFinished(this);

			}
		}

		return false;
	}

	@Override
	public boolean needsFormationTransition(Ship s) {
		State currentState = s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState();
		return !(currentState instanceof FleetFormationingAbstract) && !(currentState instanceof FleetMining);
	}

	@Override
	public Transition getEntityFormationTransition() {
		return Transition.FLEET_GET_TO_MINING_POS;
	}

	@Override
	public void initFormation(Ship flagShip, List<Ship> loaded, List<Transform> formationPoses) {

		Sendable sendable = ((GameServerState) getEntityState().getState()).getLocalAndRemoteObjectContainer().getLocalObjects().get(currentId);
		if(sendable == null || !(sendable instanceof SegmentController)) {
			loaded.clear();
		}

		SegmentController seg = (SegmentController) sendable;

		FleetFormationSpherical sphere = new FleetFormationSpherical();
		sphere.getFormation(seg, loaded, formationPoses);

		for(Ship s : loaded) {
			if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)) {

			} else {
				((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram()).setTarget(seg);
			}
		}

	}

	@Override
	public void transformAndAddLocalFormation(FleetMember flagShip, Ship s, Transform transform) {
		Sendable sendable = ((GameServerState) getEntityState().getState()).getLocalAndRemoteObjectContainer().getLocalObjects().get(currentId);
		if(sendable == null || !(sendable instanceof SegmentController)) {
			return;
		}

		SegmentController seg = (SegmentController) sendable;

		seg.getWorldTransform().transform(transform.origin);
		SectorTransformation sectorTransformation = new SectorTransformation(transform, seg.getSectorId());
		s.getAiConfiguration().getAiEntityState().fleetFormationPos.add(sectorTransformation);
	}

}
