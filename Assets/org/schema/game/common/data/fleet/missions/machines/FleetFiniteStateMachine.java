package org.schema.game.common.data.fleet.missions.machines;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.states.*;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class FleetFiniteStateMachine extends FiniteStateMachine<FleetFiniteStateMachineFactory>{

	private Idle idle;
	private FormationingIdle formationingIdle;
	private FormationingSentry formatoningSentry;
	private Moving moving;
	private Attacking attacking;
	private Defending defending;
	private Escorting escorting;
	private Repairing repairing;
	private Standoff standoff;
	private SentryIdle sentry;
	private CallbackToCarrier recall;
	private MiningAsteroids mining;
	private Patrolling patrolling;
	private Trading trading;

	private Jamming jamming;
	private UnJamming unjamming;
	private Cloaking cloaking;
	private UnCloaking uncloaking;
	private Interdicting interdicting;
	private StoppingInterdiction stoppingInterdiction;

	@Override
	public Fleet getObj() {
		return (Fleet) super.getObj();
	}
	
	
	public FleetFiniteStateMachine(Fleet obj,
			MachineProgram<?> program, FleetFiniteStateMachineFactory parameter) {
		super(obj, program, parameter);
		
		Fleet gObj = getObj();
		
		
	}

	@Override
	public void createFSM(FleetFiniteStateMachineFactory parameter) {
		parameter.createMachine(this);
		
		Fleet gObj = getObj();
		idle = new Idle(gObj);
		formationingIdle = new FormationingIdle(gObj);
		formatoningSentry = new FormationingSentry(gObj);
		moving = new Moving(gObj);
		attacking = new Attacking(gObj);
		defending = new Defending(gObj);
		escorting = new Escorting(gObj);
		repairing = new Repairing(gObj);
		standoff = new Standoff(gObj);
		sentry = new SentryIdle(gObj);
		recall = new CallbackToCarrier(gObj);
		mining = new MiningAsteroids(gObj);
		patrolling = new Patrolling(gObj);
		trading = new Trading(gObj);
		
		cloaking = new Cloaking(gObj);
		uncloaking = new UnCloaking(gObj);
		jamming = new Jamming(gObj);
		unjamming = new UnJamming(gObj);
		interdicting = new Interdicting(gObj);
		stoppingInterdiction = new StoppingInterdiction(gObj);

		addState(idle);
		addState(formationingIdle);
		addState(formatoningSentry);
		addState(moving);
		addState(attacking);
		addState(defending);
		addState(escorting);
		addState(repairing);
		addState(standoff);
		addState(sentry);
		addState(recall);
		addState(mining);
		addState(patrolling);
		addState(trading);

		moving.addTransition(Transition.TARGET_SECTOR_REACHED, idle);
		patrolling.addTransition(Transition.TARGET_SECTOR_REACHED, idle);
		trading.addTransition(Transition.TARGET_SECTOR_REACHED, idle);

		setStartingState(idle);
	}
	private void addState(State s){
		s.addTransition(Transition.FLEET_IDLE_FORMATION, formationingIdle);
		s.addTransition(Transition.FLEET_SENTRY_FORMATION, formatoningSentry);
		s.addTransition(Transition.FLEET_ATTACK, attacking);
		s.addTransition(Transition.FLEET_DEFEND, defending);
		s.addTransition(Transition.FLEET_ESCORT, escorting);
		s.addTransition(Transition.RESTART, idle);
		s.addTransition(Transition.FLEET_EMPTY, idle);
		s.addTransition(Transition.MOVE_TO_SECTOR, moving);
		s.addTransition(Transition.FLEET_PATROL, patrolling);
		s.addTransition(Transition.FLEET_TRADE, trading);
		s.addTransition(Transition.FLEET_SENTRY, sentry);
		s.addTransition(Transition.FLEET_RECALL_CARRIER, recall);
		s.addTransition(Transition.FLEET_MINE, mining);
		s.addTransition(Transition.FLEET_REPAIR, repairing);
		s.addTransition(Transition.FLEET_STANDOFF, standoff);

		s.addTransition(Transition.FLEET_CLOAK, cloaking);
		s.addTransition(Transition.FLEET_UNCLOAK, uncloaking);
		s.addTransition(Transition.FLEET_JAM, jamming);
		s.addTransition(Transition.FLEET_UNJAM, unjamming);
		s.addTransition(Transition.FLEET_INTERDICT, interdicting);
		s.addTransition(Transition.FLEET_STOP_INTERDICT, stoppingInterdiction);

		cloaking.addTransition(Transition.FLEET_ACTION_DONE, s);
		uncloaking.addTransition(Transition.FLEET_ACTION_DONE, s);
		jamming.addTransition(Transition.FLEET_ACTION_DONE, s);
		unjamming.addTransition(Transition.FLEET_ACTION_DONE, s);
		repairing.addTransition(Transition.FLEET_ACTION_DONE, s);
		interdicting.addTransition(Transition.FLEET_ACTION_DONE, s);
		stoppingInterdiction.addTransition(Transition.FLEET_ACTION_DONE, s);
	}
	@Override
	public void onMsg(Message message) {
	}
}
