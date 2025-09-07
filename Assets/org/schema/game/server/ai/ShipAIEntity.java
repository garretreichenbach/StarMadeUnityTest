package org.schema.game.server.ai;

import api.common.GameCommon;
import api.listener.fastevents.CustomAddOnUseListener;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.ShipAIEntityAttemptToShootListener;
import api.utils.game.SegmentControllerUtils;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.camera.TransformableOldRestrictedAxisCameraLook;
import org.schema.game.client.view.camera.TransformableRestrictedAxisCameraLook;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.HittableAIEntityState;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.beam.harvest.SalvageElementManager;
import org.schema.game.common.controller.elements.beam.repair.RepairElementManager;
import org.schema.game.common.controller.elements.combination.Combinable;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.controller.elements.combination.modifier.MissileUnitModifier;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;
import org.schema.game.common.controller.elements.combination.modifier.CannonUnitModifier;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerCollectionManager;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SectorTransformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetBreaking;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.util.WorldToScreenConverterFixedAspect;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.TransformTimed;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.List;

public class ShipAIEntity extends SegmentControllerAIEntity<Ship> implements Transformable, HittableAIEntityState {

	public static final float EPSILON_RANGE = 0.23f;

	public static final float EPSILON_RANGE_TURRET = 0.08f;

	@ConfigurationElement(name = "NerfAIRangeVSJammingTargets")
	public static boolean NERF_AI_RANGE_VS_JAMMING_TARGETS = false;
	//Forcefully caps AI range if the target is jammed, on top of the accuracy penalty
	//This can break AI ship behavior vs jammed targets completely, so it's better to leave it off

	@ConfigurationElement(name = "AIScanCooldownMS")
	public static long AI_SCAN_COOLDOWN = 5000L;

	@ConfigurationElement(name = "AIInhibitorCooldownMS")
	public static long AI_INHIBITOR_COOLDOWN = 5000L;

	public static final float JAMMING_DIV = 1.7f;

	private static final float SPEED_MULT_PLAYER = 1.0f;

	/**
	 */
	// private float difficulty = 10.0f;
	protected final AIShipControllerStateUnit unit = new AIShipControllerStateUnit(this);

	private final TransformTimed camera;

	public float getForceMoveWhileShooting() {
		return Math.max(1, (4000 - getEntity().getMass()) * 0.05f);
	}

	private TransformableOldRestrictedAxisCameraLook algo;

	// public static final float FIND_TARGET_RANGE = 2500;
	private Vector3f linearVelocityTmp = new Vector3f();

	private Vector3i absPosTmp = new Vector3i();

	private float difficulty = (ServerConfig.AI_WEAPON_AIMING_ACCURACY.getInt());

	private Vector3f test = new Vector3f();

	private long lastAggroTest;

	private long lastShootingRangeCheck;

	private Range shootingRange = new Range();

	private Range salvageRange = new Range();

	private TransformableRestrictedAxisCameraLook algoAxis;

	private long stopOrient;

	private float antiMissileShootingSpeed;

	public List<SectorTransformation> fleetFormationPos = new ObjectArrayList<SectorTransformation>();

	private float timeTracker;

	public long lastAttackStateSetByFleet;

	private long evadingTime;

	private long evadingDuration;

	public Vector3i currentSectorLoadedMove;

	public long lastInSameSector;

	public long inSameSector;

	private long lastAlgoRefresh;
	private long lastScan = 0;
	private long lastInhibitor = 0;

	public ShipAIEntity(String name, Ship s) {
		super(name, s);
		camera = new TransformTimed();
		camera.setIdentity();
	}

	@Override
	public boolean canSalvage() {
		return salvageRange.canUse;
	}

	private void check(UsableControllableFiringElementManager<? extends FiringUnit<?, ?, ?>, ? extends ControlBlockElementCollectionManager<? extends FiringUnit<?, ?, ?>, ?, ?>, ?> em, short controllerType, Range shootingRange) {
		shootingRange.canUse = true;
		if(em.getCollectionManagers().isEmpty()) {
			if (em instanceof SalvageElementManager) {
				shootingRange.canUse = false;
			}
		// nothing to shoot with
		} else {
			int unpowered = 0;
			for(int i = 0; i < em.getCollectionManagers().size(); i++) {
				ControlBlockElementCollectionManager<? extends FiringUnit<?, ?, ?>, ?, ?> m = em.getCollectionManagers().get(i);
				boolean controlling = em.getControlElementMap().isControlling(Ship.core, m.getControllerPos(), controllerType);
				if (controlling) {
					final ControlBlockElementCollectionManager<?, ?, ?> supportCol;
					if(m.getSlaveConnectedElement() != Long.MIN_VALUE) {
						ManagerModuleCollection<?, ?, ?> managerModuleCollection = ((ManagedSegmentController<?>) m.getSegmentController()).getManagerContainer().getModulesControllerMap().get((short) ElementCollection.getType(m.getSlaveConnectedElement()));
						ControlBlockElementCollectionManager<?, ?, ?> cb;
						if(managerModuleCollection != null && (cb = managerModuleCollection.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElement()))) != null) {
							float ratio = CombinationAddOn.getRatio(m, cb);
							supportCol = cb;
						} else {
							supportCol = null;
						}
					} else {
						supportCol = null;
					}
					if (em instanceof SalvageElementManager) {
						shootingRange.canUse = false;
						Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> short2ObjectOpenHashMap = getEntity().getControlElementMap().getControllingMap().get(m.getControllerElement().getAbsoluteIndex());
						LongOpenHashSet longOpenHashSet;
						if (short2ObjectOpenHashMap != null && (longOpenHashSet = short2ObjectOpenHashMap.get(ElementKeyMap.STASH_ELEMENT)) != null && longOpenHashSet.size() > 0) {
							LongIterator iterator = longOpenHashSet.iterator();
							while(iterator.hasNext()) {
								long chestPos = iterator.nextLong();
								Inventory inventory = getEntity().getManagerContainer().getInventory(ElementCollection.getPosIndexFrom4(chestPos));
								if(inventory != null && inventory.canPutIn(ElementKeyMap.TERRAIN_DIRT_ID, 20)) {
									shootingRange.canUse = true;
									break;
								}
							}
						}
					}
					for(FiringUnit<?, ?, ?> c : m.getElementCollections()) {
						if(supportCol != null && ((Combinable) em).getAddOn() != null) {
							Modifier mod = (((Combinable) em).getAddOn()).getGUI(m, c, supportCol, null);
							if(mod instanceof CannonUnitModifier) {
								CannonUnitModifier gui = (CannonUnitModifier) (((Combinable) em).getAddOn()).getGUI(m, c, supportCol, null);
								shootingRange.update(gui.outputDistance);
								antiMissileShootingSpeed = Math.max(antiMissileShootingSpeed, gui.outputSpeed);
							} else if(mod instanceof BeamUnitModifier) {
								BeamUnitModifier gui = (BeamUnitModifier) (((Combinable) em).getAddOn()).getGUI(m, c, supportCol, null);
								shootingRange.update(gui.outputDistance);
							} else if(mod instanceof MissileUnitModifier) {
								MissileUnitModifier gui = (MissileUnitModifier) (((Combinable) em).getAddOn()).getGUI(m, c, supportCol, null);
								shootingRange.update(gui.outputDistance);
							} else {
								assert (false);
							}
						} else {
							shootingRange.update(c.getDistanceFull());
						}
					}
				}
			}
		}
	}

	private static class Range {

		public enum Mode {

			MAX, MIN
		}

		public Mode mode = Mode.MAX;
		public boolean canUse = true;

		float range;

		public void reset() {
			canUse = true;
			if (mode == Mode.MAX) {
				range = 0;
			} else {
				range = Float.POSITIVE_INFINITY;
			}
		}

		public void update(float dist) {
			if (mode == Mode.MAX) {
				if (dist < Float.POSITIVE_INFINITY) {
					range = Math.max(dist, range);
				}
			} else {
				if (dist > 1) {
					range = Math.min(dist, range);
				}
			}
		}

		public void checkSanity() {
			if (range <= 0 ) range = 1500;

			range *=  ServerConfig.AI_ENGAGEMENT_RANGE_OF_MIN_WEAPON_RANGE.getFloat();
		}
	}

	private void checkShootingRange(Timer timer) {
		if(timer.currentTime - lastShootingRangeCheck > 5000) {
			shootingRange.mode = Range.Mode.MAX;//Range.Mode.MIN; Setting it to min really hampers the ai's ability to engage targets in general
			salvageRange.mode = Range.Mode.MIN;
			shootingRange.reset();
			salvageRange.reset();
			antiMissileShootingSpeed = 1;
			check(getEntity().getManagerContainer().getWeapon().getElementManager(), ElementKeyMap.WEAPON_CONTROLLER_ID, shootingRange);
			check(getEntity().getManagerContainer().getMissile().getElementManager(), ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID, shootingRange);
			check(getEntity().getManagerContainer().getBeam().getElementManager(), ElementKeyMap.DAMAGE_BEAM_COMPUTER, shootingRange);
			check(getEntity().getManagerContainer().getSalvage().getElementManager(), ElementKeyMap.SALVAGE_CONTROLLER_ID, salvageRange);
			lastShootingRangeCheck = timer.currentTime;
			shootingRange.checkSanity();
			salvageRange.checkSanity();
		}
		
	}

	@Override
	public Ship getEntity() {
		return super.getEntity();
	}

	/**
	 * @return the shootingRange
	 */
	@Override
	public float getShootingRange() {
		return shootingRange.range;
	}

	@Override
	public float getSalvageRange() {
		return salvageRange.range;
	}

	@Override
	public void updateAIServer(Timer timer) throws FSMException {
		if(getEntity().getDockingController().isDocked() && getEntity().getDockingController().isTurretDocking()) {
			String currentState = (String) getEntity().getAiConfiguration().get(Types.TYPE).getCurrentState();
			if(currentState.equals("Ship")) {
				((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).setCurrentState("Turret", true);
				getEntity().getAiConfiguration().applyServerSettings();
				System.err.println("[AI] Auto Set " + getEntity() + " from " + currentState + " mode to Turret mode because it's docked");
			}
		} else if(isTurretDockedLastAxis() && !getEntity().getAiConfiguration().get(Types.TYPE).getCurrentState().equals("Turret")) {
			((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).setCurrentState("Turret", true);
			getEntity().getAiConfiguration().applyServerSettings();
		} else if(getEntity().railController.isDockedAndExecuted() && getEntity().railController.isTurretDocked()) {
			// nothing to do for non-last turrets
		} else if(getEntity().getAiConfiguration().get(Types.TYPE).getCurrentState().equals("Turret")) {
			((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).setCurrentState("Ship", true);
			getEntity().getAiConfiguration().applyServerSettings();
		}

		checkShootingRange(timer);
		if(getCurrentProgram() != null && getCurrentProgram() instanceof TargetProgram && ((TargetProgram<?>) getCurrentProgram()).getTarget() != null) {
			SimpleGameObject target = ((TargetProgram<?>) getCurrentProgram()).getTarget();
			if(!target.existsInState()) {
				((TargetProgram<?>) getCurrentProgram()).setTarget(null);
				getStateCurrent().stateTransition(Transition.RESTART);
			} else if(target instanceof Ship && ((Ship) target).isCoreOverheating()) {
				((TargetProgram<?>) getCurrentProgram()).setTarget(null);
				getStateCurrent().stateTransition(Transition.RESTART);
			} else {
				// we have a valid target
				updateAIServerInCombat(timer);
			}
		}
		//ADDED CODE BY IRONSIGHT (quick and dirty fix for AI that scans:
		// make ship scan repeatedly
		if(!getEntity().isDocked() && !getEntity().isConrolledByActivePlayer()) {
			StructureScannerCollectionManager s = getEntity().getManagerContainer().getShortRangeScanner().getElementManager().getCollection();
			if(s != null && s.isCharged() && !s.isActive()) {
				if(timer.currentTime - lastScan > AI_SCAN_COOLDOWN) { //This needed a cooldown to prevent spam
					s.handleKeyPress(this.unit,timer);
					lastScan = timer.currentTime;
				}
			}

			for(CustomAddOnUseListener listener : FastListenerCommon.customAddOnUseListeners) listener.use(getEntity(), getEntity().getManagerContainer(), timer);
		}
	}

	private void updateAIServerInCombat(Timer timer) {
		StealthElementManager a = getEntity().getManagerContainer().getStealth().getElementManager();
		if (a.hasCollection() && a.getCollection().reloadingNeeded == 0 && !a.isActive()) {
			a.getCollection().handleKeyPress(unit, timer);
		}

		//ADDED CODE BY IRONSIGHT (quick and dirty fix for AI that uses jump inhibitor:
		//TODO: calls old interdiction system
		InterdictionAddOn i = getEntity().getManagerContainer().getInterdictionAddOn();
		if(i.isCharged() && !i.isActive() && i.canExecute()) {
			if(timer.currentTime - lastInhibitor > AI_INHIBITOR_COOLDOWN) { //This needed a cooldown to prevent spam
				i.executeModule();
				lastInhibitor = timer.currentTime;
			}
			i.executeModule();
		}
	}

	@Override
	public void updateAIClient(Timer timer) {
		// System.err.println("[CLIENT][AI] "+getEntity()+" AI UPDATING!");
		checkShootingRange(timer);
		Quat4f orientDir = new Quat4f();
		orientDir.set(getEntity().getNetworkObject().orientationDir.getVector());
		Vector3f moveDir = new Vector3f();
		moveDir.set(getEntity().getNetworkObject().moveDir.getVector());
		if (moveDir.equals(FleetBreaking.breakMoving)) {
			stop();
		} else {
			boolean orientateToMoveDir = Quat4fTools.isZero(orientDir);
			if(moveDir.lengthSquared() > 0 && orientateToMoveDir) {
				moveTo(timer, moveDir, true);
			} else if (!orientateToMoveDir) {
				// Vector3f up = new Vector3f(0,1,0);
				// up.cross(up, orientDir);
				// up.scale(MOVE_FORCE_WHILE_SHOOTING);
				if (moveDir.lengthSquared() > 0) {
					moveTo(timer, moveDir, false);
				}
				orientate(timer, orientDir);
			}
			Vector3f targetPosition = getEntity().getNetworkObject().targetPosition.getVector(new Vector3f());
			if (targetPosition.lengthSquared() > 0) {
				if (((GameClientState) getState()).getCurrentSectorEntities().containsKey(getEntity().getId())) {
					doShooting(unit, timer);
				}
			}
		}
	}

	public void stop() {
		if (!checkAIValid()) {
			return;
		}
		RigidBody body = (RigidBody) getEntity().getPhysicsDataContainer().getObject();
		Vector3f linearVelocity = body.getLinearVelocity(new Vector3f());
		linearVelocity.scale(0.3f);
		if (linearVelocity.length() < 1f) {
			linearVelocity.set(0, 0, 0);
		}
		body.setLinearVelocity(linearVelocity);
	}

	public boolean isTurretDockedLastAxis() {
		return (getEntity().railController.isDockedAndExecuted() && getEntity().railController.isTurretDocked() && getEntity().railController.isTurretDockLastAxis()) || getEntity().getDockingController().isDocked();
	}
	@Override
	public void afterUpdate(Timer timer) {
		if (!isBigEvading()) {
			getEntity().proximityVector.set(0, 0, 0);
			getEntity().getProximityObjects().clear();
		}
	}

	public boolean isBigEvading() {
		return System.currentTimeMillis() - evadingTime < evadingDuration;
	}

	public void setEvadingTime(long duration) {
		evadingTime = System.currentTimeMillis();
		evadingDuration = duration;
	}

	public long getEvadingDuration() {
		return evadingDuration;
	}

	@Override
	public void updateGeneral(Timer timer) {
		if (isOnServer()) {
			// System.err.println("[SERVER][AI] set to fleet?: "+getEntity()+"; "+getEntity().isInFleet()+"; Not-Turret: "+!isTurretDockedLastAxis()+"; ");
			if (!isTurretDockedLastAxis() && getEntity().isInFleet() && !((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).getCurrentState().equals("Fleet")) {
				// System.err.println("[SERVER][AI] set to 'fleet' AI mode: "+getEntity());
				((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).setCurrentState("Fleet", true);
				((AIConfiguationElements<Boolean>) (getEntity()).getAiConfiguration().get(Types.ACTIVE)).setCurrentState(true, true);
				getEntity().getAiConfiguration().applyServerSettings();
			}
		}
		super.updateGeneral(timer);
	}

	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		if (!isTurretDockedLastAxis() && !getEntity().isInFleet() && !((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).getCurrentState().equals("Ship")) {
			((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.TYPE)).setCurrentState("Ship", ((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.ACTIVE)).isOn());
			if (isOnServer()) {
				getEntity().getAiConfiguration().applyServerSettings();
			}
		}

		super.updateOnActive(timer);
	}

	@Override
	public float getAntiMissileShootingSpeed() {
		return antiMissileShootingSpeed;
	}

	public void setAntiMissileShootingSpeed(float antiMissileShootingSpeed) {
		this.antiMissileShootingSpeed = antiMissileShootingSpeed;
	}

	public float getShootingDifficulty(SimpleGameObject target) {
		return getEntity().getConfigManager().apply(StatusEffectType.AI_ACCURACY_DRONE, getShootingDifficultyRaw(target));
	}

	public float getShootingDifficultyRaw(SimpleGameObject target) {
		if(target.getOwnerState() != null && target.getOwnerState() instanceof PlayerState) {
			return difficulty;
		}
		if(target instanceof SegmentController) {
			SegmentController s = (SegmentController) target;
			if(s.railController.getRoot().getOwnerState() != null && s.railController.getRoot().getOwnerState() instanceof PlayerState) {
				return difficulty;
			}
		}
		if(getEntity().getFactionId() < 0 && target.getFactionId() > 0 && ((FactionState) getState()).getFactionManager().existsFaction(target.getFactionId())) {
			return difficulty;
		}
		// non player targets that are not a player have higher chance of hitting
		return 3000;
	// if(target instanceof SegmentControllerAIInterface){
	// AiEntityStateInterface aiEntityState = ((SegmentControllerAIInterface)target).getAiConfiguration().getAiEntityState();
	// if(aiEntityState.isActive()){
	// return 2000;
	// }
	// }
	//
	// return 5000;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Transformable#getWorldTransform()
	 */
	@Override
	public TransformTimed getWorldTransform() {
		return camera;
	}

	@Override
	public void handleHitBy(float actualDamage, Damager from) {
		if(getCurrentProgram() != null && getCurrentProgram() instanceof TargetProgram<?> && getState().getUpdateTime() - lastAggroTest > 25000) {
			AIConfiguationElements<String> a = ((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.AIM_AT));
			if((a.getCurrentState().equals("Any") || (a.getCurrentState().equals("Ships") && from instanceof Ship) || (a.getCurrentState().equals("Stations") && (from instanceof SpaceStation || from instanceof Planet || from instanceof PlanetIco)))) {
				if(from instanceof SimpleTransformableSendableObject) {
					FactionManager factionManager = ((GameServerState) getState()).getFactionManager();
					if(!factionManager.isFriend(getEntity(), ((SimpleTransformableSendableObject) from))) {
						// never aggro against allies
						if(((Sendable) from).getId() != ((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId()) {
							if(getEntity().getUniqueIdentifier().startsWith("ENTITY_SHIP_MOB_SIM")) {
								((GameServerState) getState()).getSimulationManager().aggressive(getEntity(), (SimpleTransformableSendableObject) from, actualDamage);
							} else {
								((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(((Sendable) from).getId());
							}
						}
					}
				}
			}
			lastAggroTest = getState().getUpdateTime();
		}
	}

	private boolean checkAIValid() {
		if (!getEntity().getAttachedPlayers().isEmpty()) {
			return false;
		}
		if(getEntity().getDockingController().isDocked() || getEntity().railController.isDockedOrDirty()) {
			getEntity().getManagerContainer().getThrusterElementManager().getVelocity().set(0, 0, 0);
			return false;
		}
		float thrusters = getEntity().getManagerContainer().getThrusterElementManager().getActualThrust();
		if(thrusters == 0 && getEntity().getTotalElements() == 0) {
			return false;
		}
		return true;
	}

	public void moveTo(Timer timer, final Vector3f toDir, boolean orientate) {
		if (!checkAIValid()) {
			return;
		}
		float thrusters = getEntity().getManagerContainer().getThrusterElementManager().getActualThrust();
		if(thrusters == 0 && toDir.lengthSquared() > 0) {
			thrusters = 0.1f;
		}
		// one udpate for every 30 ms
		float updateFrequency = ThrusterElementManager.getUpdateFrequency();
		float dist = toDir.length();
		Vector3f dir = new Vector3f(toDir);
		dir.normalize();
		RigidBody body = (RigidBody) getEntity().getPhysicsDataContainer().getObject();
		timeTracker += timer.getDelta();
		// make sure it's save against long lags
		timeTracker = Math.min(updateFrequency * 100f, timeTracker);
		while(timeTracker >= updateFrequency) {
			timeTracker -= updateFrequency;
			float nThrust = thrusters;
			if (dist > 0.05) {
				body.activate();
/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
//				AudioController.fireAudioEventID(961);
				body.getLinearVelocity(linearVelocityTmp);
				Vector3f linearVelocity = body.getLinearVelocity(new Vector3f());
				Vector3f a = new Vector3f(linearVelocityTmp);
				a.normalize();
				if (Vector3fTools.diffLength(a, dir) > 1.5f) {
					// break down current speed to avoid sidewards acceleration (flying in cycles around target)
					linearVelocity.scale(0.1f);
					body.setLinearVelocity(linearVelocity);
				// thrust is full since we want to correct course
				} else if (Vector3fTools.diffLength(a, dir) > 1.0f) {
					// break down current speed to avoid sidewards acceleration (flying in cycles around target)
					linearVelocity.scale(0.4f);
					body.setLinearVelocity(linearVelocity);
				// thrust is full since we want to correct course
				} else if (Vector3fTools.diffLength(a, dir) > 0.3f) {
					// break down current speed to avoid sidewards acceleration (flying in cycles around target)
					linearVelocity.scale(0.7f);
					body.setLinearVelocity(linearVelocity);
				} else if(dist < 2) {
					if (linearVelocity.length() > 3) {
						linearVelocity.scale(0.8f);
						body.setLinearVelocity(linearVelocity);
					}
					nThrust *= 0.5;
				} else if(dist < 1) {
					if (linearVelocity.length() > 2) {
						linearVelocity.scale(0.6f);
						body.setLinearVelocity(linearVelocity);
					}
					nThrust *= 0.3;
				} else if(dist < 0.5f) {
					linearVelocity.scale(0.1f);
					body.setLinearVelocity(linearVelocity);
					nThrust *= 0.1;
				} else {
					// AI slightly faster to keep up
					nThrust *= 1.01f;
				}
				Vector3f dirApplied = new Vector3f(dir);
				dirApplied.normalize();
				float speedMult = FactionManager.isNPCFaction(getEntity().getFactionId()) ? ((GameStateInterface) getState()).getGameState().getNPCFleetSpeedLoaded() : SPEED_MULT_PLAYER;
				ThrusterElementManager.applyThrust(dirApplied, nThrust, body, getEntity(), speedMult, linearVelocity);
			} else {
				// STOP
				stop();
			}
			if(orientate) {
				assert (!Float.isNaN(toDir.x)) : toDir;
				orientate(timer, Quat4fTools.getNewQuat(toDir.x, toDir.y, toDir.z, 0));
			}
			body.getLinearVelocity(getEntity().getManagerContainer().getThrusterElementManager().getVelocity());
		}
	}

	public void onProximity(SegmentController segmentController) {
		
	}

	private void iterateTurretOrientation(Vector3f oForce, Quat4f toDir, float timeStep) {
		if(algoAxis == null || (getState().getUpdateTime() - lastAlgoRefresh) > 5000) {
			algoAxis = new TransformableRestrictedAxisCameraLook(this, getEntity());
			algoAxis.setMaxIterations(2);
			algoAxis.setScaleDownPerIteration(0.1f);
			lastAlgoRefresh = getState().getUpdateTime();
		}
		camera.set(getEntity().getWorldTransform());
		WorldToScreenConverterFixedAspect w = null;
		Matrix4f proj = null;
		if(getEntity().isOnServer()) {
			w = GameServerController.worldToScreenConverter;
			proj = GameServerController.projectionMatrix;
		} else {
			w = GameClientController.worldToScreenConverter;
			proj = Controller.projectionMatrix;
		}
		// System.err.println("PROJ "+getSendable().isOnServer()+"\n"+proj);
		Transform worldTransform = new Transform(getEntity().getWorldTransform());
		// Vector3f fo = GlUtil.getForwardVector(new Vector3f(), worldTransform);
		// Vector3f ri = GlUtil.getRightVector(new Vector3f(), worldTransform);
		// GlUtil.setRightVector(ri, worldTransform);
		// GlUtil.setForwardVector(fo, worldTransform);
		worldTransform.origin.set(0, 0, 0);
		Vector3f tar = new Vector3f();
		Vector3f toDorScaled = new Vector3f(toDir.x, toDir.y, toDir.z);
		toDorScaled.normalize();
		// Transform inv = new Transform(worldTransform);
		// inv.inverse();
		// inv.transform(toDorScaled);
		toDorScaled.x = -toDorScaled.x;
		toDorScaled.z = -toDorScaled.z;
		toDorScaled.y = -toDorScaled.y;
		// toDorScaled.scale(10);
		tar.set(toDorScaled);
		Vector3f camForward = GlUtil.getForwardVector(new Vector3f(), worldTransform);
		Vector3f camPos = new Vector3f();
		Vector3f res = new Vector3f();
		w.convert(tar, res, camPos, camForward, true, proj, worldTransform);
		Vector3f resB = new Vector3f(res);
		res.normalize();
		assert (!Float.isNaN(res.x) && !Float.isNaN(res.y) && !Float.isNaN(res.y)) : toDir + "; " + tar + "; " + camPos + "; " + camForward + "; \n" + proj + "; \n\n" + getEntity().getWorldTransform().getMatrix(new javax.vecmath.Matrix4f());
		assert (!Float.isInfinite(res.x) && !Float.isInfinite(res.y) && !Float.isInfinite(res.y)) : toDir + "; " + tar + "; " + camPos + "; " + camForward + "; \n" + proj + "; \n\n" + getEntity().getWorldTransform().getMatrix(new javax.vecmath.Matrix4f());
		algoAxis.setCorrectingForNonCoreEntry(false);
		algoAxis.getFollowing().set(getEntity().getWorldTransform());
		algoAxis.setCorrecting2Transformable(getEntity().railController.previous.rail.getSegmentController());
		algoAxis.setOrientation(getEntity().railController.previous.rail.getOrientation());
		float turretOrientateSpeed = Math.max(VoidElementManager.AI_TURRET_ORIENTATION_SPEED_MIN, Math.min(VoidElementManager.AI_TURRET_ORIENTATION_SPEED_MAX, VoidElementManager.AI_TURRET_ORIENTATION_SPEED_DIV_BY_MASS / Math.max(0.01f, getEntity().getTotalPhysicalMass())));
		float f = turretOrientateSpeed * timeStep * getEntity().railController.getRailMassPercent();
		// 400 is screen width/2, 300 is screenheight/2. both values are fixed because of arbitrary calc
		float x = 0;
		Vector3f dN = new Vector3f(toDir.x, toDir.y, toDir.z);
		dN.normalize();
		if(dN.angle(camForward) < 0.5) {
			f *= Math.min(1f, dN.angle(camForward) * 5f);
		}
		// if(!w.overflowX){
		x = resB.x > 400 ? -f : f;
		float y = resB.y > 300 ? -f : f;
		// }else{
		// x = -f;
		// }
		algoAxis.mouseRotate(isOnServer(), x, y, 0, 1f, 1f, 0);
		if(algoAxis.lastCollision) {
			stopOrient = System.currentTimeMillis() + 500;
		}
		Vector3f currentForward = GlUtil.getForwardVector(new Vector3f(), camera);
		/*
		 * on 2-axis rotation the rotation of the base will be
		 * passed right in PhysicsExt.orientate() too
		 */
		// getEntity().getPhysics().onOrientateOnly(getEntity(), timer.getDelta());
		getEntity().getPhysics().orientate(getEntity(), GlUtil.getForwardVector(new Vector3f(), camera), GlUtil.getUpVector(new Vector3f(), camera), GlUtil.getRightVector(new Vector3f(), camera), oForce.x, oForce.y, oForce.z, timeStep);
	}

	public void orientate(Timer timer, Quat4f toDir) {
		if(System.currentTimeMillis() < stopOrient) {
			// System.err.println("LAG REDUC");
			// to reduce lag from continous collision
			return;
		}
		if (!getEntity().getAttachedPlayers().isEmpty()) {
			return;
		}
		assert (!Float.isNaN(toDir.x)) : toDir;
		// System.err.println("ORIENTATIONG "+getSendable().getState());
		if(Quat4fTools.isZero(toDir)) {
			// System.err.println("[AI][ORIENATION] WARNING: no to Dir given "+getSendable()+"; "+getSendable().getState());
			return;
		}
		// if(!isOnServer()){
		// getEntity().getPhysics().onOrientateOnly(getEntity(), timer.getDelta());
		// return;
		// }
		// float oForce = 3f / Math.max(1f, (getEntity().getMass()-8f)/8.7f);
		// oForce = Math.max(0.1f, oForce);
		Vector3f oForce = (getEntity()).getOrientationForce();
		if(getEntity().railController.isDockedOrDirty()) {
			if(getEntity().railController.isDockedAndExecuted() && getEntity().railController.isTurretDocked() && getEntity().railController.isTurretDockLastAxis()) {
				// float step = 0.005f;
				// float ts = timer.getDelta();
				// int iterations = 0;
				// for(float m = Math.min(ts, step); m < ts && iterations < 1000; m = Math.min(ts, m+step), iterations++ ){
				// iterateTurretOrientation(oForce, toDir, step);
				// }
				iterateTurretOrientation(oForce, toDir, timer.getDelta());
			} else {
				// not safe to do orientation in that state
				return;
			}
		} else if(getEntity().getDockingController().isDocked()) {
			// do rotation by mouse
			if(algo == null) {
				algo = new TransformableOldRestrictedAxisCameraLook(this, getEntity());
				
			}
			camera.set(getEntity().getWorldTransform());
			WorldToScreenConverterFixedAspect w = null;
			Matrix4f proj = null;
			if(getEntity().isOnServer()) {
				w = GameServerController.worldToScreenConverter;
				proj = GameServerController.projectionMatrix;
			} else {
				w = GameClientController.worldToScreenConverter;
				proj = Controller.projectionMatrix;
			}
			// System.err.println("PROJ "+getSendable().isOnServer()+"\n"+proj);
			Transform worldTransform = new Transform(getEntity().getWorldTransform());
			Vector3f fo = GlUtil.getForwardVector(new Vector3f(), worldTransform);
			Vector3f ri = GlUtil.getRightVector(new Vector3f(), worldTransform);
			GlUtil.setForwardVector(ri, worldTransform);
			GlUtil.setForwardVector(fo, worldTransform);
			worldTransform.origin.set(0, 0, 0);
			Vector3f tar = new Vector3f();
			Vector3f toDorScaled = new Vector3f(toDir.x, toDir.y, toDir.z);
			toDorScaled.normalize();
			// Transform inv = new Transform(worldTransform);
			// inv.inverse();
			// inv.transform(toDorScaled);
			toDorScaled.x = -toDorScaled.x;
			toDorScaled.z = -toDorScaled.z;
			toDorScaled.y = -toDorScaled.y;
			// toDorScaled.scale(10);
			tar.set(toDorScaled);
			Vector3f camForward = GlUtil.getForwardVector(new Vector3f(), worldTransform);
			Vector3f camPos = new Vector3f();
			Vector3f res = new Vector3f();
			w.convert(tar, res, camPos, camForward, true, proj, worldTransform);
			Vector3f resB = new Vector3f(res);
			res.normalize();
			assert (!Float.isNaN(res.x) && !Float.isNaN(res.y) && !Float.isNaN(res.y)) : toDir + "; " + tar + "; " + camPos + "; " + camForward + "; \n" + proj + "; \n\n" + getEntity().getWorldTransform().getMatrix(new javax.vecmath.Matrix4f());
			assert (!Float.isInfinite(res.x) && !Float.isInfinite(res.y) && !Float.isInfinite(res.y)) : toDir + "; " + tar + "; " + camPos + "; " + camForward + "; \n" + proj + "; \n\n" + getEntity().getWorldTransform().getMatrix(new javax.vecmath.Matrix4f());
			algo.setCorrectingForNonCoreEntry(false);
			algo.getFollowing().set(getEntity().getWorldTransform());
			algo.setCorrecting2Transformable(getEntity().getDockingController().getDockedOn().to.getSegment().getSegmentController());
			algo.setOrientation(getEntity().getDockingController().getDockedOn().to.getOrientation());
			float f = 0.5f * timer.getDelta();
			// 400 is screen width/2, 300 is screenheight/2. both values are fixed because of arbitrary calc
			float x = 0;
			// if(!w.overflowX){
			x = resB.x > 400 ? -f : f;
			// }else{
			// x = -f;
			// }
			algo.mouseRotate(isOnServer(), x, resB.y > 300 ? -f : f, 0, 1, 1, 0);
			Vector3f currentForward = GlUtil.getForwardVector(new Vector3f(), camera);
			// System.err.println(oForce+" ;;;; "+getWorldTransform().basis);
			getEntity().getPhysics().orientate(getEntity(), GlUtil.getForwardVector(new Vector3f(), camera), GlUtil.getUpVector(new Vector3f(), camera), GlUtil.getRightVector(new Vector3f(), camera), oForce.x, oForce.y, oForce.z, timer.getDelta());
		} else {
			if (toDir.w == 0) {
				// only forward dir provided
				Vector3f forward = new Vector3f(toDir.x, toDir.y, toDir.z);
				forward.normalize();
				Vector3f up = GlUtil.getUpVector(new Vector3f(), getEntity().getWorldTransform());
				Vector3f right = new Vector3f();
	
				right.cross(up, forward);
				right.normalize();
	
				up.cross(forward, right);
				up.normalize();
				getEntity().getPhysics().orientate(getEntity(), forward, up, right, oForce.x * 1.5f, oForce.y * 1.5f, oForce.z * 1.5f, timer.getDelta());
			} else {
				// full orientation provided (quaternion)
				Matrix3f m = new Matrix3f();
				m.set(toDir);
				// System.err.println(oForce+" ;;;; "+toDir+"\n"+m);
				getEntity().getPhysics().orientate(getEntity(), GlUtil.getForwardVector(new Vector3f(), m), GlUtil.getUpVector(new Vector3f(), m), GlUtil.getRightVector(new Vector3f(), m), oForce.x, oForce.y, oForce.z, timer.getDelta());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AiEntityState#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "; WEP-Range: " + getShootingRange() + " salv-Range: " + getSalvageRange() + "; " + (program != null && program.getMachine() != null && getStateCurrent() != null ? getStateCurrent().getDescString() : "");
	}

	public void doShooting(AIControllerStateUnit<?> unit, Timer timer) {
		if (!getEntity().getAttachedPlayers().isEmpty()) {
			return;
		}
		if (getEntity().getNetworkObject().targetType.getByte() == SimpleGameObject.MINABLE) {
			((AIShipControllerStateUnit) unit).timesBothMouseButtonsDown = 0;
			((AIShipControllerStateUnit) unit).useBothMouseButtonsDown = false;
			getEntity().getManagerContainer().getSalvage().getElementManager().handle(unit, timer);
		} else if((unit.getAquiredTarget() != null) &&
                ((((AIGameConfiguration<?,?>) getAIConfig()).get(Types.AIM_AT).getCurrentState().equals("Selected Target")) ||
                        GameCommon.getGameState().getFactionManager().isEnemy(getEntity().getFactionId(), unit.getAquiredTarget().getFactionId()))){
			((AIShipControllerStateUnit) unit).timesBothMouseButtonsDown = 0;
			((AIShipControllerStateUnit) unit).useBothMouseButtonsDown = false;
			if (getEntity().getDockingController().isDocked() || getEntity().getDockingController().isTurretDocking()) {
				getEntity().getManagerContainer().getWeapon().getElementManager().handle(unit, timer);
				getEntity().getManagerContainer().getBeam().getElementManager().handle(unit, timer);
				getEntity().getManagerContainer().getMissile().getElementManager().handle(unit, timer);
			} else {
				if (getEntity().isUsingPowerReactors() || getEntity().getManagerContainer().getPowerAddOn().getPercentOneOfLowestInChain() > 0.35f) {
					getEntity().getManagerContainer().getWeapon().getElementManager().handle(unit, timer);
				}
				if (getEntity().isUsingPowerReactors() || getEntity().getManagerContainer().getPowerAddOn().getPercentOneOfLowestInChain() > 0.35f) {
					getEntity().getManagerContainer().getBeam().getElementManager().handle(unit, timer);
				}
				if (getEntity().isUsingPowerReactors() || getEntity().getManagerContainer().getPowerAddOn().getPercentOneOfLowestInChain() > 0.35f) {
					getEntity().getManagerContainer().getMissile().getElementManager().handle(unit, timer);
				}
			}
		}

		if(unit.getAquiredTarget() != null && GameCommon.getGameState().getFactionManager().isFriend(unit.getAquiredTarget().getFactionId(), getEntity().getFactionId())) {
			if(unit.getAquiredTarget() instanceof Ship && ((Ship) unit.getAquiredTarget()).getReactorHp() < ((Ship) unit.getAquiredTarget()).getReactorHpMax()) {
				try {
					SegmentControllerUtils.getElementManager(getEntity(), RepairElementManager.class).handle(unit, timer);
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		//INSERTED CODE
		for(ShipAIEntityAttemptToShootListener listener : FastListenerCommon.shipAIEntityAttemptToShootListeners) {
			listener.doShooting(this, unit, timer);
		}
		///
	}

	

}
