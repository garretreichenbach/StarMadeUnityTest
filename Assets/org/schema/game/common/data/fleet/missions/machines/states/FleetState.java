package org.schema.game.common.data.fleet.missions.machines.states;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.SegmentControllerGameState;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.*;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Random;

public abstract class FleetState extends State{

	public enum FleetStateType {
		IDLING(en -> {
			return Lng.str("IDLE");
		}),
		SENTRY_IDLE(en -> {
			return Lng.str("IDLE - SENTRY");
		}),
		FORMATION_IDLE(en -> {
			return Lng.str("IDLE - FORMATION");
		}),
		MOVING(en -> {
			return Lng.str("MOVING");
		}),
		REPAIRING(en -> {
			return Lng.str("REPAIRING");
		}),
		STANDOFF(en -> {
			return Lng.str("STANDOFF");
		}),
		ATTACKING(en -> {
			return Lng.str("ATTACKING");
		}),
		SENTRY(en -> {
			return Lng.str("SENTRY");
		}),
		DEFENDING(en -> {
			return Lng.str("DEFENDING");
		}),
		ESCORTING(en -> {
			return Lng.str("ESCORTING");
		}),
		FORMATION_SENTRY(en -> {
			return Lng.str("SENTRY - FORMATION");
		}),
		CALLBACK_TO_CARRIER(en -> {
			return Lng.str("CALLBACK TO CARRIER");
		}),
		MINING(en -> {
			return Lng.str("MINING");
		}),
		PATROLLING(en -> {
			return Lng.str("PATROLLING");
		}),
		TRADING(en -> {
			return Lng.str("TRADING");
		}),
		CLOAKING(en -> {
			return Lng.str("CLOAKING");
		}),
		UNCLOAKING(en -> {
			return Lng.str("UNCLOAKING");
		}),
		JAMMING(en -> {
			return Lng.str("JAMMING");
		}),
		UNJAMMING(en -> {
			return Lng.str("STOP JAMMING");
		}),
		INTERDICTING(en -> {
			return Lng.str("FTL INTERDICTING");
		}),
		STOPPING_INTERDICTION(en -> {
			return Lng.str("STOP FTL INTERDICTION");
		});
		
		private final Translatable name;
		private FleetStateType(Translatable name) {
			this.name = name;
		}
		
		public String getName(){
			return name.getName(this);
		}
		
	}
	
	public abstract FleetStateType getType();
	
	public FleetState(Fleet gObj) {
		super(gObj);
	}
	@Override
	public Fleet getEntityState(){
		return (Fleet) super.getEntityState();
	}
	public void restartAllLoaded() throws FSMException {
		for(FleetMember mem : getEntityState().getMembers()){
			if(mem.isLoaded()){
				Ship s = (Ship) mem.getLoaded();
				if(s.isCoreOverheating() || s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
					continue;
				}
				State st = s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState();
				if(!(st instanceof FleetIdleWaiting) && !(st instanceof FleetBreaking) ){
					System.err.println("[FLEET] breaking ship: "+s);
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.FLEET_BREAKING);
				}
			}
		}
	}
	@Override
	public final boolean onEnter() {
		if(!getEntityState().missionString.equals(getType().getName())){
			getEntityState().missionString = getType().getName();
			getEntityState().getFleetManager().submitMissionChangeToClients(getEntityState());
		}
		
		return onEnterFleetState();
	}
	public boolean onEnterFleetState(){
		return false;
	}
	public boolean isInAttackCycle(Ship s){
		if(s.getAiConfiguration() == null){
			return false;
		}
		State st = s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState();
		return st instanceof FleetAttackCycle;
	}
	private List<Ship> loaded = new ObjectArrayList<Ship>();
	private long attacked;
	private Vector3i attackedSector;
	private long lastSentry;
	public void allWaiting() throws FSMException{
		for(int i = 0; i < getEntityState().getMembers().size(); i++){
			FleetMember fleetMember = getEntityState().getMembers().get(i);
			
			if(fleetMember.isLoaded() ){
				Ship s = (Ship)fleetMember.getLoaded();
				if(!(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetIdleWaiting)){
					
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.RESTART);
				}
			}
		}
	}
	public boolean isState(Ship s, Class<? extends State> stateClazz){
		if(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState().getClass().isAssignableFrom(stateClazz)){
			return true;
		}
		return false;
	}
	public void fireTransitionIfNotState(Ship s, Transition t, Class<? extends State> stateClazz) throws FSMException{
		if(!isState(s, stateClazz)){
			s.getAiConfiguration().getAiEntityState().getCurrentProgram().
			getMachine().getFsm().stateTransition(t);
		}
	}
	
	public void moveToCurrentTarget() throws FSMException{
		FleetMember flagShip = getEntityState().getFlagShip();
		
		if(flagShip == null){
			stateTransition(Transition.FLEET_EMPTY);
			return;
		}
		if(getEntityState().getCurrentMoveTarget() == null){
			stateTransition(Transition.TARGET_SECTOR_REACHED);
			return;
		}
		loaded.clear();
		int atGoal = 0;
		
		
		
		
		for(int i = 0; i < getEntityState().getMembers().size(); i++){
			FleetMember fleetMember = getEntityState().getMembers().get(i);
			
			if(flagShip.getSector().equals(getEntityState().getCurrentMoveTarget()) && fleetMember.getSector().equals(flagShip.getSector())){
				atGoal++;
			}else if(fleetMember.isLoaded() ){
				Ship s = (Ship)fleetMember.getLoaded();
				
				loaded.add(s);
			}else{
				fleetMember.moveRequestUnloaded(getEntityState(), getEntityState().getCurrentMoveTarget());
			}
		}
		if(atGoal == getEntityState().getMembers().size()){
			getEntityState().removeCurrentMoveTarget();
			if(getEntityState().getCurrentMoveTarget() == null){
				stateTransition(Transition.TARGET_SECTOR_REACHED);
			}
			return;
		}
		handleLoadedMoving(flagShip, getEntityState().getCurrentMoveTarget());
		return;
	}
	
	public void onHitBy(Damager damager){
		if(damager != null && this.isMovingSentry()){
			if(damager instanceof SimpleTransformableSendableObject<?>){
				SimpleTransformableSendableObject<?> smp = (SimpleTransformableSendableObject<?>)damager;
				
				Sector sector = ((GameServerState)damager.getState()).getUniverse().getSector(smp.getSectorId());
				if(sector != null){
					this.attackedSector = new Vector3i(sector.pos);
					this.attacked = System.currentTimeMillis();
				}
			}
		}
	}
	
	public boolean isMovingSentry() {
		return false;
	}
	
	public boolean isMovingAttacking() {
		return false;
	}

	private void handleLoadedMoving(FleetMember flagShip, Vector3i goal) throws FSMException{
		
		
		for(int i = 0; i < loaded.size(); i++){
			final Ship s = loaded.get(i);
			
			if(s.isCoreOverheating() || s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
				continue;
			}
			assert(s.getAiConfiguration() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() != null):s;
			
			
			
			Sector sector = ((GameServerState)s.getState()).getUniverse().getSector(s.getSectorId());
			
			if(sector == null){
				continue;
			}
			
			
			boolean inAttackCycle = isInAttackCycle(s);
			ShipAIEntity aiState = s.getAiConfiguration().getAiEntityState();
			if(!inAttackCycle){
				if(aiState.currentSectorLoadedMove == null){
					aiState.currentSectorLoadedMove = new Vector3i(sector.pos);
					aiState.inSameSector = 0L;
					aiState.lastInSameSector = System.currentTimeMillis();
				}else{
					if(aiState.currentSectorLoadedMove.equals(sector.pos) && !aiState.currentSectorLoadedMove.equals(goal)){
						aiState.inSameSector += (System.currentTimeMillis() - aiState.lastInSameSector);
						aiState.lastInSameSector = System.currentTimeMillis();
						if(aiState.inSameSector > 60000*3){
							System.err.println("[SERVER][AI] WARNING: ship has been too long in this sector while wanting to move (stuck?). warping towards goal");
							Random r = new Random();
							Vector3i rand = new Vector3i(sector.pos);
							rand.x += r.nextInt(5)-2;
							rand.y += r.nextInt(5)-2;
							rand.z += r.nextInt(5)-2;
							SectorSwitch queueSectorSwitch = ((GameServerState) s.getState()).getController()
									.queueSectorSwitch(s, rand, SectorSwitch.TRANS_JUMP, false, true, true);
							if (queueSectorSwitch != null) {
								queueSectorSwitch.delay = System.currentTimeMillis() + 4000;
								queueSectorSwitch.jumpSpawnPos = new Vector3f(s.getWorldTransform().origin);
								queueSectorSwitch.executionGraphicsEffect = (byte) 2;
								queueSectorSwitch.keepJumpBasisWithJumpPos = true;
							}
							aiState.currentSectorLoadedMove = null;
						}
						
					}else{
						aiState.currentSectorLoadedMove = null;
					}
				}
			}else{
				aiState.currentSectorLoadedMove = null;
			}
			
			if(!inAttackCycle && isMovingSentry() && System.currentTimeMillis() - attacked < 5000 && attackedSector != null){
				//not attacking but we have been attacked and we react to that
				
				if(Sector.isNeighbor(sector.pos, attackedSector)){
					s.getAiConfiguration().getAiEntityState().engagingTimeoutQueued = new EngagingTimeout(attackedSector, 3){
						@Override
						public void onTimeout(
								SegmentControllerGameState<?> currentState) {
							
							((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram())
							.setSectorTarget(new Vector3i(attackedSector));
							try {
								currentState.stateTransition(Transition.MOVE_TO_SECTOR);
							} catch (FSMException e) {
								e.printStackTrace();
							}
						}
						@Override
						public void onShot(
								SegmentControllerGameState<?> currentState) {
							
							attacked = System.currentTimeMillis();
						}
						@Override
						public void onNoTargetFound(
								SegmentControllerGameState<?> currentState) {
							try {
								currentState.stateTransition(Transition.MOVE_TO_SECTOR);
							} catch (FSMException e) {
								e.printStackTrace();
							}
						}
					};
					
					
					//move to sector where the attack happened
					fireTransitionIfNotState(s, Transition.SEARCH_FOR_TARGET, FleetSeachingForTarget.class);
				}else{
					//move to sector where the attack happened
					((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram())
					.setSectorTarget(new Vector3i(attackedSector));
					fireTransitionIfNotState(s, Transition.MOVE_TO_SECTOR, FleetMovingToSector.class);
				}
				
			}else if(inAttackCycle && (isMovingSentry() || isMovingAttacking())){
				//nothing to do because we are currently attacking
			}else if(!inAttackCycle && isMovingAttacking() && System.currentTimeMillis() - lastSentry > 5000){
				//searches for target every 5 seconds
				
//				System.err.println("SEARCHING FOR TARGET !!!!!!!!!");
				s.getAiConfiguration().getAiEntityState().engagingTimeoutQueued = 
						new Timeout(){
					@Override
					public void onTimeout(
							SegmentControllerGameState<?> currentState) {
						
					}
					@Override
					public void onShot(
							SegmentControllerGameState<?> currentState) {
						
					}
					@Override
					public boolean checkTimeout(Vector3i sector) {
						return false;
					}
					@Override
					public void onNoTargetFound(SegmentControllerGameState<?> currentState) {
						try {
							currentState.stateTransition(Transition.MOVE_TO_SECTOR);
						} catch (FSMException e) {
							e.printStackTrace();
						}
					}
				};
				s.getAiConfiguration().getAiEntityState().getCurrentProgram().
				getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
				lastSentry = System.currentTimeMillis();
			}else{
				if(s.getAiConfiguration().getAiEntityState().engagingTimeoutQueued == null){
					moveLoadedToGoal(s, goal);
				}else{
				}
			}
			
		}
	}
	private void moveLoadedToGoal(Ship s, Vector3i goal) throws FSMException{
		((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram())
		.setSectorTarget(new Vector3i(goal));
		if(!(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetMovingToSector)){
			s.getAiConfiguration().getAiEntityState().getCurrentProgram().
			
			//use Transition.MOVE_TO_SECTOR here, since we are changing the 
			//state of a loaded ship. (if it's attacked, this is going to be overwritten anyways)
			getMachine().getFsm().stateTransition(Transition.MOVE_TO_SECTOR);
		}
	}
	public Transition getMoveTrasition(){
		//can be overridden
		return Transition.MOVE_TO_SECTOR;
	}
}
