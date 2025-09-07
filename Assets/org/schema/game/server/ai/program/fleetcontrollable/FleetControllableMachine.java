package org.schema.game.server.ai.program.fleetcontrollable;

import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.game.server.ai.program.fleetcontrollable.states.*;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class FleetControllableMachine extends FiniteStateMachine<String> {


	public FleetControllableMachine(AiEntityStateInterface obj, FleetControllableProgram program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public void createFSM(String parameter) {

		AiEntityStateInterface gObj = getObj();
		FleetSeachingForTarget searchingForTarget = new FleetSeachingForTarget(gObj);
		FleetMovingToSector movingToSector = new FleetMovingToSector(gObj);
		FleetEngagingTarget engagingTarget = new FleetEngagingTarget(gObj);
		FleetFormationing formation = new FleetFormationing(gObj);
		FleetGettingToTarget gettingToTarget = new FleetGettingToTarget(gObj);
		FleetEvadingTarget evadingTarget = new FleetEvadingTarget(gObj);
		FleetRally rally = new FleetRally(gObj);
		FleetBreaking breaking = new FleetBreaking(gObj);
		FleetIdleWaiting idle = new FleetIdleWaiting(gObj);
		ShootAtTarget shootAtTarget = new FleetShootAtTarget(gObj);
		FleetFormationingMining formationMining = new FleetFormationingMining(gObj);
		FleetMining mining = new FleetMining(gObj);
		FleetEscorting escorting = new FleetEscorting(gObj);
		FleetRepairing repairing = new FleetRepairing(gObj);

		idle.addTransition(Transition.STOP, idle);
		idle.addTransition(Transition.RESTART, idle);
		idle.addTransition(Transition.FLEET_FORMATION, formation);
		idle.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		idle.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		idle.addTransition(Transition.HEALTH_LOW, rally);
		idle.addTransition(Transition.FLEET_BREAKING, breaking);
		idle.addTransition(Transition.FLEET_GET_TO_MINING_POS, formationMining);
		idle.addTransition(Transition.FLEET_ESCORT, escorting);
		idle.addTransition(Transition.FLEET_REPAIR, repairing);

		formation.addTransition(Transition.FLEET_MOVE_TO_FLAGSHIP_SECTOR, movingToSector);
		formation.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		formation.addTransition(Transition.RESTART, idle);
		formation.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		formation.addTransition(Transition.FLEET_BREAKING, breaking);
		
		formationMining.addTransition(Transition.FLEET_MOVE_TO_FLAGSHIP_SECTOR, movingToSector);
		formationMining.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		formationMining.addTransition(Transition.RESTART, idle);
		formationMining.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		formationMining.addTransition(Transition.FLEET_BREAKING, breaking);
		formationMining.addTransition(Transition.FLEET_MINE, mining);
		formationMining.addTransition(Transition.FLEET_FORMATION, formation);
		
		mining.addTransition(Transition.FLEET_MOVE_TO_FLAGSHIP_SECTOR, movingToSector);
		mining.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		mining.addTransition(Transition.RESTART, idle);
		mining.addTransition(Transition.FLEET_FORMATION, formation);
		mining.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		mining.addTransition(Transition.FLEET_BREAKING, breaking);
		mining.addTransition(Transition.FLEET_GET_TO_MINING_POS, formationMining);

		escorting.addTransition(Transition.FLEET_MOVE_TO_FLAGSHIP_SECTOR, movingToSector);
		escorting.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		escorting.addTransition(Transition.ENEMY_PROXIMITY, evadingTarget);
		escorting.addTransition(Transition.ENEMY_FIRE, searchingForTarget);
		escorting.addTransition(Transition.SHOOTING_COMPLETED, gettingToTarget);
		escorting.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		escorting.addTransition(Transition.HEALTH_LOW, rally);
		escorting.addTransition(Transition.STOP, idle);
		escorting.addTransition(Transition.RESTART, idle);
		escorting.addTransition(Transition.FLEET_FORMATION, formation);
		escorting.addTransition(Transition.FLEET_BREAKING, breaking);
		escorting.addTransition(Transition.TARGET_AQUIRED, gettingToTarget);
		escorting.addTransition(Transition.NO_TARGET_FOUND, idle);
		escorting.addTransition(Transition.FLEET_REPAIR, repairing);

		searchingForTarget.addTransition(Transition.TARGET_AQUIRED, gettingToTarget);
		searchingForTarget.addTransition(Transition.STOP, idle);
		searchingForTarget.addTransition(Transition.RESTART, idle);
		searchingForTarget.addTransition(Transition.HEALTH_LOW, rally);
		searchingForTarget.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		searchingForTarget.addTransition(Transition.NO_TARGET_FOUND, idle);
		searchingForTarget.addTransition(Transition.FLEET_FORMATION, formation);
		searchingForTarget.addTransition(Transition.FLEET_BREAKING, breaking);
		searchingForTarget.addTransition(Transition.FLEET_ESCORT, escorting);
		searchingForTarget.addTransition(Transition.FLEET_MOVE_TO_FLAGSHIP_SECTOR, movingToSector);
		searchingForTarget.addTransition(Transition.FLEET_REPAIR, repairing);

		movingToSector.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		movingToSector.addTransition(Transition.STOP, idle);
		movingToSector.addTransition(Transition.RESTART, idle);
		movingToSector.addTransition(Transition.HEALTH_LOW, rally);
		movingToSector.addTransition(Transition.FLEET_FORMATION, formation);
		movingToSector.addTransition(Transition.TARGET_SECTOR_REACHED, idle);
		movingToSector.addTransition(Transition.FLEET_BREAKING, breaking);
		movingToSector.addTransition(Transition.FLEET_GET_TO_MINING_POS, formationMining);
		movingToSector.addTransition(Transition.FLEET_ESCORT, escorting);
		movingToSector.addTransition(Transition.FLEET_REPAIR, repairing);

		gettingToTarget.addTransition(Transition.TARGET_IN_RANGE, engagingTarget);
		gettingToTarget.addTransition(Transition.ENEMY_PROXIMITY, evadingTarget);
		gettingToTarget.addTransition(Transition.IN_SHOOTING_POSITION, shootAtTarget);
		gettingToTarget.addTransition(Transition.STOP, idle);
		gettingToTarget.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		gettingToTarget.addTransition(Transition.RESTART, idle);
		gettingToTarget.addTransition(Transition.HEALTH_LOW, rally);
		gettingToTarget.addTransition(Transition.FLEET_FORMATION, formation);
		gettingToTarget.addTransition(Transition.FLEET_BREAKING, breaking);
		gettingToTarget.addTransition(Transition.FLEET_REPAIR, repairing);

		evadingTarget.addTransition(Transition.STOP, idle);
		evadingTarget.addTransition(Transition.RESTART, gettingToTarget);
		evadingTarget.addTransition(Transition.HEALTH_LOW, rally);
		evadingTarget.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		evadingTarget.addTransition(Transition.IN_SHOOTING_POSITION, shootAtTarget);
		evadingTarget.addTransition(Transition.FLEET_FORMATION, formation);
		evadingTarget.addTransition(Transition.FLEET_BREAKING, breaking);
		evadingTarget.addTransition(Transition.FLEET_REPAIR, repairing);

		engagingTarget.addTransition(Transition.TARGET_DESTROYED, searchingForTarget);
		engagingTarget.addTransition(Transition.ENEMY_PROXIMITY, evadingTarget);
		engagingTarget.addTransition(Transition.IN_SHOOTING_POSITION, shootAtTarget);
		engagingTarget.addTransition(Transition.TARGET_OUT_OF_RANGE, gettingToTarget);
		engagingTarget.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		engagingTarget.addTransition(Transition.ENEMY_FIRE, searchingForTarget);
		engagingTarget.addTransition(Transition.HEALTH_LOW, rally);
		engagingTarget.addTransition(Transition.STOP, idle);
		engagingTarget.addTransition(Transition.RESTART, idle);
		engagingTarget.addTransition(Transition.FLEET_FORMATION, formation);
		engagingTarget.addTransition(Transition.FLEET_BREAKING, breaking);
		engagingTarget.addTransition(Transition.FLEET_REPAIR, repairing);

		shootAtTarget.addTransition(Transition.ENEMY_PROXIMITY, evadingTarget);
		shootAtTarget.addTransition(Transition.ENEMY_FIRE, searchingForTarget);
		shootAtTarget.addTransition(Transition.SHOOTING_COMPLETED, gettingToTarget);
		shootAtTarget.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		shootAtTarget.addTransition(Transition.HEALTH_LOW, rally);
		shootAtTarget.addTransition(Transition.STOP, idle);
		shootAtTarget.addTransition(Transition.RESTART, idle);
		shootAtTarget.addTransition(Transition.FLEET_FORMATION, formation);
		shootAtTarget.addTransition(Transition.FLEET_BREAKING, breaking);
		shootAtTarget.addTransition(Transition.FLEET_REPAIR, repairing);

		rally.addTransition(Transition.STOP, idle);
		rally.addTransition(Transition.RESTART, idle);
		rally.addTransition(Transition.FLEET_FORMATION, formation);
		rally.addTransition(Transition.FLEET_BREAKING, breaking);
		rally.addTransition(Transition.FLEET_REPAIR, repairing);

		breaking.addTransition(Transition.STOP, idle);
		breaking.addTransition(Transition.RESTART, idle);
		breaking.addTransition(Transition.FLEET_FORMATION, formation);
		breaking.addTransition(Transition.MOVE_TO_SECTOR, movingToSector);
		breaking.addTransition(Transition.SEARCH_FOR_TARGET, searchingForTarget);
		breaking.addTransition(Transition.HEALTH_LOW, rally);
		breaking.addTransition(Transition.FLEET_GET_TO_MINING_POS, formationMining);

		setStartingState(idle);
	}

	@Override
	public void onMsg(Message message) {

	}
	
}
