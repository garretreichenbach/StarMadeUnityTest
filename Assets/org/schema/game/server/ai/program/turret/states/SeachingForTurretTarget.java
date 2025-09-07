package org.schema.game.server.ai.program.turret.states;

import api.utils.ai.CustomAITargetUtil;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.rail.TurretShotPlayerUsable;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.missile.ServerMissileManager;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.SegmentControllerAIEntity;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.game.server.ai.program.turret.TurretShipAIEntity;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.ai.stateMachines.*;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class SeachingForTurretTarget extends ShipGameState {

	/**
	 *
	 */
	
	//
	//	public boolean checkTarget(SimpleTransformableSendableObject target) throws FSMException{
	//		Sector sector = ((GameServerState)getEntity().getState()).getUniverse().getSector(target.getSectorId());
	//		if(sector != null && (sector.isProtected()
	//				||sector.isPeace() )){
	//			//do not target objects in protected sectors
	//			return false;
	//		}
	//		if(target.isHidden()){
	//			return false;
	//		}
	//		if(target instanceof Ship && ((Ship)target).isCoreOverheating()){
	//			return false;
	//		}
	//		if(target instanceof FloatingRock){
	//			return false;
	//		}
	//		if(target instanceof Ship && ((Ship)target).isCloaked()){
	//			return false;
	//		}
	//		if(target instanceof Ship && ((Ship)target).isJamming() && dist.length() > getEntityState().getShootingRange()/2){
	//			return false;
	//		}
	//
	//		return true;
	//	}
	public Transform tmp = new Transform();
	public Vector3f dist = new Vector3f();
	public long lastCheck = 0;
	public ArrayList<SimpleGameObject> possibleEnemies = new ArrayList<SimpleGameObject>();
	private long startedSearching;
	private boolean resetTurret;
	private boolean aimManual;
	private final Vector3f rotDir = new Vector3f();
	private KeyboardMappings shoot;
	public SeachingForTurretTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	private void findMissileToTarget() throws FSMException {
		if (getEntity().getState().getUpdateTime() - lastCheck > 1000) {
			if (GameClientState.isDebugObject(getEntity())) {
				System.err.println("[AI][TURRET] FIND MISSILE TARGET: " + getEntity() + "; " + lastCheck);
			}
			possibleEnemies.clear();

			GameServerState state = (GameServerState) getEntityState().getState();

			ServerMissileManager missileManager = state.getController().getMissileController().getMissileManager();
			float minLen = 100000;
			if(getAIConfig().get(Types.PRIORIZATION).getCurrentState().toString().toLowerCase(Locale.ENGLISH).equals("highest")){
				Missile m = missileManager.getHighestDamage(getEntity(), getEntity().getWorldTransform().origin, getEntityState().getShootingRange() - 50);
				if(m != null) {
					possibleEnemies.add(m);
				}
			}else if(getAIConfig().get(Types.PRIORIZATION).getCurrentState().toString().toLowerCase(Locale.ENGLISH).equals("lowest")){
				Missile m = missileManager.getLowestDamage(getEntity(), getEntity().getWorldTransform().origin, getEntityState().getShootingRange() - 50);
				if(m != null) {
					possibleEnemies.add(m);
				}
			}else {
				//random missile
				for (Missile m : missileManager.getMissiles().values()) {

					boolean ownMissile = m.getOwner().isSegmentController() && ((SegmentController) m.getOwner()).railController.isInAnyRailRelationWith(getEntity());
					if (!ownMissile && (getEntity().getFactionId() == 0 || m.getOwner().getFactionId() != getEntity().getFactionId())) {
						Transform missileWorldTransform = m.getWorldTransformRelativeToSector(getEntity().getSectorId(), tmp);

						if (missileWorldTransform != null) {

							dist.sub(missileWorldTransform.origin, getEntity().getWorldTransform().origin);
							if (dist.length() < getEntityState().getShootingRange() - 100 /*&& dist.length() < minLen*/) {

								minLen = dist.length();
								possibleEnemies.add(m);
							}
						}
					}
				}
			}
			
			
			
			
			
			if (possibleEnemies.size() > 0) {
				//			System.err.println("POSSIBLE ENEMIES: "+possibleEnemies);
				((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(possibleEnemies.get(Universe.getRandom().nextInt(possibleEnemies.size())));
				possibleEnemies.clear();
			}

			lastCheck = getEntity().getState().getUpdateTime();

		}
		if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget() != null) {
//			System.err.println("TARGET MISSILE AQUIRED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			stateTransition(Transition.TARGET_AQUIRED);
		}
	}

	private void findAstronautTarget() throws FSMException {
		findAnyTarget(EntityType.ASTRONAUT);
	}
	private void findAsteroidTarget() throws FSMException {
		AbstractOwnerState s = getEntity().railController.getRoot().getOwnerState();
		if (s != null && s instanceof PlayerState) {
			PlayerState p = (PlayerState) s;
			int selectedEntityId = p.getNetworkObject().selectedAITargetId.get();

			Sendable sendable = getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedEntityId);
			if (sendable instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject<?> target = ((SimpleTransformableSendableObject<?>) sendable);
				Sector sector = ((GameServerState) getEntity().getState()).getUniverse().getSector(target.getSectorId());
				if (sector != null && (sector.isProtected()
						|| sector.isPeace())) {
					//do not target objects in protected sectors

				} else {

					if (!getEntity().railController.isInAnyRailRelationWith(target)) {
						((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(target);
						stateTransition(Transition.TARGET_AQUIRED);
					}
				}
			}
		}
	}
	private void findAnyTarget(EntityType... filter) throws FSMException {

		//		System.err.println("FIND ANY TARGET: "+getEntity()+"; "+lastCheck);
		if (getEntityState().getState().getUpdateTime() - lastCheck > 3000) {
			getEntityState().lastEngage = "";
			if (GameClientState.isDebugObject(getEntity())) {
				System.err.println("[AI][TURRET] FIND ANY TARGET: " + getEntity() + "; " + lastCheck);
			}

			
			
			lastCheck = getEntityState().getState().getUpdateTime();
//						System.err.println("FIND ANY TARGET --->: "+getEntity());
			possibleEnemies.clear();
			float minLen = 1000000;
			//				System.err.println("NOW SEARCHING TARGET FOR "+getGObj().getSendable());
			synchronized (getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {

				SimpleTransformableSendableObject own = (getEntityState().getEntity());
				for (Sendable s : getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof SimpleTransformableSendableObject) {

						if (filter != null && filter.length > 0) {
							boolean isInFilter = false;
							for (EntityType t : filter) {
								if (s instanceof SegmentController && ((SegmentController) s).railController.getRoot().getType() == t) {
									isInFilter = true;
									break;
								}else if (((SimpleTransformableSendableObject) s).getType() == t) {
									isInFilter = true;
									break;
								}
							}
							if (!isInFilter) {
								continue;
							}
						}
						if (s == getEntity()) {
							if (GameClientState.isDebugObject(getEntity())) {
								System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: target is self");
							}
							continue;
						}

						if (s instanceof ShopSpaceStation) {
							if (GameClientState.isDebugObject(getEntity())) {
								System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: its a shop");
							}
							continue;
						}
						if (!((FactionState) getEntityState().getState()).getFactionManager().isEnemy(own, (SimpleTransformableSendableObject) s)) {
							if (GameClientState.isDebugObject(getEntity())) {
								System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: its not an enemy");
							}
							continue;
						}
						if (!checkTarget((SimpleTransformableSendableObject) s, this)) {
							if (GameClientState.isDebugObject(getEntity())) {
								System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: check target failed");
							}
							continue;
						}
						if (((SimpleTransformableSendableObject) s).isInAdminInvisibility()) {
							if (GameClientState.isDebugObject(getEntity())) {
								System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: invisible");
							}
							continue;
						}
						if (s instanceof PlayerControllable && !(s instanceof AbstractCharacter<?>)) {
							
							if (((PlayerControllable) s).getAttachedPlayers().isEmpty() && s instanceof AiInterface && !((AiInterface) s).getAiConfiguration().isActiveAI() && ((SimpleTransformableSendableObject) s).getFactionId() != FactionManager.PIRATES_ID) {
								if (GameClientState.isDebugObject(getEntity())) {
									System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: no attached players and no active ai");
								}
								continue;
							}
						}
						((SimpleTransformableSendableObject) s).calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);

						dist.sub(((SimpleTransformableSendableObject) s).getClientTransform().origin, getEntity().getWorldTransform().origin);

//						System.err.println("DISTANCE TO "+s+": "+dist);

						if (dist.length() < getEntityState().getShootingRange() - 10 /*&& dist.length() < minLen*/) {
							if (GameClientState.isDebugObject((SimpleTransformableSendableObject) s)) {
								System.err.println("[AI][DEBUG] " + getEntity() + " added as possible enemy " + s + ". " + dist.length() + " / range: " + (getEntityState().getShootingRange() - 100));
							}
							minLen = dist.length();
							possibleEnemies.add(((SimpleTransformableSendableObject) s));
						} else {
							if (GameClientState.isDebugObject((SimpleTransformableSendableObject) s)) {
								System.err.println("[AI][DEBUG] " + getEntity() + " dismissed " + s + " as target: distance too far: dist: " + dist.length() + " / range: " + (getEntityState().getShootingRange() - 100));
							}
						}
					}

				}
			}
		}
		if (!possibleEnemies.isEmpty()) {
			//			System.err.println("POSSIBLE ENEMIES: "+possibleEnemies);
			if(getAIConfig().get(Types.PRIORIZATION).getCurrentState().toString().toLowerCase(Locale.ENGLISH).equals("highest")){
				SegmentController largest = null;
                for (Iterator<SimpleGameObject> iterator = possibleEnemies.iterator(); iterator.hasNext(); ) {
                    if (iterator.next() instanceof SegmentController entity) {
                        if(entity.hasActiveReactors() && (largest == null || entity.getMassWithDocks() > largest.getMassWithDocks())){
							largest = entity;
						} else iterator.remove();
                    }
                }
			} else if(getAIConfig().get(Types.PRIORIZATION).getCurrentState().toString().toLowerCase(Locale.ENGLISH).equals("lowest")){
				SegmentController smallest = null;
				for (Iterator<SimpleGameObject> iterator = possibleEnemies.iterator(); iterator.hasNext(); ) {
					if (iterator.next() instanceof SegmentController entity) {
						if(entity.hasActiveReactors() && (smallest == null || entity.getMassWithDocks() < smallest.getMassWithDocks())){
							smallest = entity;
						} else iterator.remove();
					}
				}
			}

			((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(possibleEnemies.get(Universe.getRandom().nextInt(possibleEnemies.size())));
			possibleEnemies.clear();
		}

		if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget() != null) {
			stateTransition(Transition.TARGET_AQUIRED);
		}
	}

	private void findSelected() throws FSMException {
		SegmentController root = getEntity().railController.getRoot();
		AbstractOwnerState s = root.getOwnerState();
		Integer selectedEntityId = null;
		if (s instanceof PlayerState) {
			PlayerState p = (PlayerState) s;
			selectedEntityId = p.getNetworkObject().selectedAITargetId.get();
		} else {
			if(root instanceof AiInterface){
				AiInterface a = (AiInterface)root;
				if(a.getAiConfiguration().isActiveAI()) {
					State aiState = a.getAiConfiguration().getAiEntityState().getStateCurrent();
					if(aiState instanceof ShootAtTarget){
						selectedEntityId = ((ShootAtTarget)aiState).getTargetId();
					}
				}
			}
		}

		if(selectedEntityId != null) {
			Sendable sendable = getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedEntityId);
			if (sendable instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject<?> target = ((SimpleTransformableSendableObject<?>) sendable);
				Sector sector = ((GameServerState) getEntity().getState()).getUniverse().getSector(target.getSectorId());
				if (sector != null && (sector.isProtected()
						|| sector.isPeace())) {
					//do not target objects in protected sectors

				} else {

					if (!getEntity().railController.isInAnyRailRelationWith(target)) {
						((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(target);
						stateTransition(Transition.TARGET_AQUIRED);
					}
				}
			}
		}
	}

	@Override
	public boolean onEnter() {
		((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
		startedSearching = getEntity().getState().getUpdateTime();
		resetTurret = false;
		aimManual = false;
		lastCheck = 0;
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}
	@Override
	public boolean onUpdate() throws FSMException {
		if (!resetTurret && getEntity().railController.isDockedAndExecuted() && getEntity().railController.isTurretDocked() && getEntityState().getState().getUpdateTime() - startedSearching > 30000) {
			getEntity().railController.flagResetTurretServer();
			resetTurret = true;
		}
		aimManual = false;
		shoot = null;
		
		
//		ManualUsable manualPlayerUsable = getEntityState().getManualPlayerUsable();
//		if(manualPlayerUsable != null) {
//			((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
//			manualPlayerUsable.u.getForward(rotDir);
//			
//			aimManual = true;
//			
//			TurretShotPlayerUsable t = (TurretShotPlayerUsable)manualPlayerUsable.p;
//			if(t.shootFlag != null){
//				shoot = t.shootFlag;
//			}
//			return false;
//		}
		if(getAIConfig().get(Types.MANUAL).isOn()){
			((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
			if(getEntity().railController.isDockedAndExecuted()){
				final SegmentController root = getEntity().railController.getRoot();
				ManagerContainer<?> o = ((ManagedSegmentController<?>)root).getManagerContainer();
				
				if(root instanceof PlayerControllable && !(((PlayerControllable)root).getAttachedPlayers()).isEmpty()){
					for(ControllerStateUnit u : ((PlayerControllable)root).getAttachedPlayers().get(0).getControllerState().getUnits()){
						PlayerUsableInterface playerUsable = o.getPlayerUsable(PlayerUsableInterface.USABLE_ID_SHOOT_TURRETS);
						
						if(playerUsable instanceof TurretShotPlayerUsable && u.isSelected(playerUsable, ((ManagedSegmentController<?>)root).getManagerContainer()) && u.playerControllable == root){
							
							u.getForward(rotDir);
							
							aimManual = true;
						
							TurretShotPlayerUsable t = (TurretShotPlayerUsable)playerUsable;
							if(t.shootFlag != null){
								shoot = t.shootFlag;
							}
							return false;
						}
					}
				}
			}
			
			return false;
		}
		if (getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Selected Target")) {
			findSelected();
		} else if (getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Any")) {
			findAnyTarget();
		} else if (getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Stations")) {
			findAnyTarget(EntityType.SHIP);
		} else if (getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Ships")) {
			findAnyTarget(EntityType.SPACE_STATION, EntityType.PLANET_SEGMENT);
		} else if (getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Missiles")) {
//			System.err.println("FINDING MISSILES TO SHOOT");
			findMissileToTarget();
		} else if (getAIConfig().get(Types.AIM_AT).getCurrentState().equals("Astronauts")) {
			findAstronautTarget();
		}
		//INSERTED CODE
		String currentTargetProgram = (String) getAIConfig().get(Types.AIM_AT).getCurrentState();
		CustomAITargetUtil.FindTargetProgram customTargetProgram = CustomAITargetUtil.getCustomPrograms().get(currentTargetProgram);
		if(customTargetProgram != null){
			SimpleTransformableSendableObject<?> target = customTargetProgram.findTarget(this);
			if(target != null){
				((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(target);
				stateTransition(Transition.TARGET_AQUIRED);
			}
		}
		///
		return false;
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().moveDir.set(new Vector3f(0, 0, 0));
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));
		getEntity().getNetworkObject().targetVelocity.set(new Vector3f(0, 0, 0));		
		
		if(aimManual){
//			System.err.println("ROTDIR: "+rotDir);
			((TurretShipAIEntity)s).orientateDir.set(rotDir);
			getEntity().getNetworkObject().orientationDir.set(((TurretShipAIEntity)s).orientateDir, 0);
			s.orientate(timer, Quat4fTools.getNewQuat(((TurretShipAIEntity)s).orientateDir.x, ((TurretShipAIEntity)s).orientateDir.y, ((TurretShipAIEntity)s).orientateDir.z, 0));	
			
			if(shoot != null){
				Vector3f tar = new Vector3f();
				if(shoot == KeyboardMappings.SHIP_ZOOM){
					tar.set(rotDir);
					tar.scale(5000);
					tar.add(getEntity().getWorldTransform().origin);
				}else{
					assert(shoot == KeyboardMappings.SHIP_PRIMARY_FIRE);
					Vector3f tmpCampPos = new Vector3f();
					Vector3f centeralizedControlledFromPos = new Vector3f();
					
					Vector3f camPos = getEntity().railController.getRoot().getAbsoluteElementWorldPositionShifted(
							new Vector3i(Segment.HALF_DIM, Segment.HALF_DIM, Segment.HALF_DIM), tmpCampPos);
					
					tar.set(rotDir);
					tar.scale(5000);
					tar.add(camPos);
					assert(!Vector3fTools.isNan(rotDir)):rotDir;
					assert(!Vector3fTools.isNan(camPos)):camPos;
					assert(!Vector3fTools.isNan(tar)):tar;
					assert(!Vector3fTools.isNan(getEntity().getWorldTransform().origin)):getEntity().getWorldTransform().origin;
					ClosestRayResultCallback testRayCollisionPoint =
							getEntity().railController.getRoot().getPhysics().testRayCollisionPoint(camPos, tar, false, getEntity(), null, false, true, true);
					
					
					if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit()){
						tar.set(testRayCollisionPoint.hitPointWorld);
						assert(!testRayCollisionPoint.hitNormalWorld.equals(camPos));
					}else {
					}
				}
				getEntity().getNetworkObject().targetPosition.set(tar);
				getEntity().getNetworkObject().targetVelocity.set(0,0,0);
				getEntity().getNetworkObject().targetId.set(-1);
				getEntity().getNetworkObject().targetType.set(SimpleGameObject.SIMPLE_TRANSFORMABLE_SENSABLE_OBJECT);
				s.doShooting(unit, timer);
				
				((SegmentControllerAIEntity<?>) getEntityState()).onShot(this);
			
			}
		}else{
			getEntity().getNetworkObject().orientationDir.set(0,0,0,0);
		}
	}

}
