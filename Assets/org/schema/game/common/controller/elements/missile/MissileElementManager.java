package org.schema.game.common.controller.elements.missile;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.MissileCombiSettings;
import org.schema.game.common.controller.elements.combination.MissileCombinationAddOn;
import org.schema.game.common.controller.elements.combination.modifier.MissileUnitModifier;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.GameServerController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.io.IOException;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.MISSILE;
import static org.schema.game.common.controller.elements.ammo.missile.MissileCapacityElementManager.MISSILE_CAPACITY_RELOAD_MODE;

public abstract class MissileElementManager<E extends MissileUnit<E, EC, EM>, EC extends MissileCollectionManager<E, EC, EM>, EM extends MissileElementManager<E, EC, EM>> extends UsableCombinableControllableElementManager<E, EC, EM, MissileCombiSettings> implements BlockActivationListenerInterface, WeaponElementManagerInterface, IntegrityBasedInterface {

	private static boolean debug = false;

	private final short controlling;

	private final WeaponStatisticsData tmpOutput = new WeaponStatisticsData();

	private final ShootContainer shootContainer = new ShootContainer();

	private final AmmoWeaponDrawReloadListener reloadListener = new AmmoWeaponDrawReloadListener(MISSILE,MISSILE_CAPACITY_RELOAD_MODE);

	public MissileElementManager(short controller, short controlling, SegmentController segmentController) {
		super(controller, controlling, segmentController);
		this.controlling = controlling;
	}

	private final MissileCombiSettings combiSettings = new MissileCombiSettings();

	public MissileCombiSettings getCombiSettings() {
		return combiSettings;
	}

	protected int getMissileMode(EC m) {
		int mode = 0;
		if (getAddOn() != null && m.getSlaveConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getSlaveConnectedElement());
			switch(connectedType) {
				case (ElementKeyMap.WEAPON_CONTROLLER_ID) -> mode = (int) MissileCombinationAddOn.missileCannonUnitModifier.get(this).modeModifier.getOutput(0);
				case (ElementKeyMap.DAMAGE_BEAM_COMPUTER) -> mode = (int) MissileCombinationAddOn.missileBeamUnitModifier.get(this).modeModifier.getOutput(0);
				case (ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID) -> mode = (int) MissileCombinationAddOn.missileMissileUnitModifier.get(this).modeModifier.getOutput(0);
			}
		}
		return mode;
	}

	public void doShot(E c, EC m, ShootContainer shootContainer, float speed, SimpleTransformableSendableObject aquiredTarget, PlayerState playerState, Timer timer) {
		ManagerModuleCollection<?, ?, ?> effectModuleCollection = null;
		if (m.getEffectConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getEffectConnectedElement());
			effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
		}
		if (m.getEffectConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getEffectConnectedElement());
			effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
			ControlBlockElementCollectionManager<?, ?, ?> effect = CombinationAddOn.getEffect(m.getEffectConnectedElement(), effectModuleCollection, getSegmentController());
			if (effect != null) {
				m.setEffectTotal(effect.getTotalSize());
			}
		}
		if (getAddOn() != null && m.getSlaveConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getSlaveConnectedElement());
			// System.err.println(getState()+" [WEAPON] FIRING WITH COMBINED WEAPON: "+ElementKeyMap.getInfo(m.getControllerElement().getType()).getName()+" + "+ElementKeyMap.getInfo(connectedType).getName());
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
			ShootingRespose handled = handleAddOn(this, m, c, managerModuleCollection, effectModuleCollection, shootContainer, aquiredTarget, playerState, timer, -1);
			handleResponse(handled, c, shootContainer.weapontOutputWorldPos);
		} else {
			if (c.canUse(timer.currentTime, true)) {
				float totalPowerConsumption = c.getPowerConsumption();
				if (isUsingPowerReactors() || consumePower(totalPowerConsumption)) {
					Transform transform = new Transform();
					transform.setIdentity();
					transform.origin.set(shootContainer.weapontOutputWorldPos);
					c.setStandardShotReloading();
					long lightConnectedElement = m.getLightConnectedElement();
					short lightType = 0;
					if (lightConnectedElement != Long.MIN_VALUE) {
						lightType = (short) ElementCollection.getType(lightConnectedElement);
					}
					if (getSegmentController().isOnServer()) {
						assert (shootContainer.shootingDirTemp.lengthSquared() != 0);
						long weaponId = m.getUsableId();
						addMissile(getSegmentController(), transform, new Vector3f(shootContainer.shootingDirTemp), speed, c.getDamage(), c.getDistance(), weaponId, aquiredTarget, lightType);
					}
					/*AudioController.fireAudioEventRemote("MISSILE_FIRE", getSegmentController().getId(), new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.MISSILE, AudioTags.FIRE }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c))*/
					AudioController.fireAudioEventRemoteID(896, getSegmentController().getId(), AudioController.ent(getSegmentController(), c.getElementCollectionId(), c));
					handleResponse(ShootingRespose.FIRED, c, shootContainer.weapontOutputWorldPos);
				} else {
					handleResponse(ShootingRespose.NO_POWER, c, shootContainer.weapontOutputWorldPos);
				}
			} else {
				// if (c.isInitializing(System.currentTimeMillis())) {
				// handleResponse(ShootingRespose.INITIALIZING, c, weapontOutputWorldPos);
				// } else {
				handleResponse(ShootingRespose.RELOADING, c, shootContainer.weapontOutputWorldPos);
			// }
			}
		}
	}

	public boolean checkMissileCapacity() {
		if (getSegmentController() == null) return false;

		SegmentController root = getSegmentController().railController.getRoot();
		if (root instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> mc = ((ManagedSegmentController<?>) root).getManagerContainer();
			return mc.getAmmoCapacity(MISSILE) >= 1;
		}
		return false;
	}

	protected abstract void addMissile(SegmentController segmentController, Transform transform, Vector3f shootingDir, float speed, float damage, float distance, long weaponId, SimpleTransformableSendableObject target, short colorType);

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		long absPos = piece.getAbsoluteIndex();
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			for (E d : getCollectionManagers().get(i).getElementCollections()) {
				if (d.contains(absPos)) {
					d.setMainPiece(piece, active);
					return active ? 1 : 0;
				}
			}
		}
		return active ? 1 : 0;
	}

	// protected abstract  boolean handleAddOn(DumbMissileCollectionManager m, MissileUnit c,
	// ManagerModuleCollection<?, ?, ?> managerModuleCollection,
	// Vector3f weapontOutputWorldPos, Vector3f shootingDir,
	// Vector3f shootingUp, Vector3f shootingRight, SimpleTransformableSendableObject lockOntarget);
	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(controlling);
	}

	public MissileController getMissileController() {
		return ((GameServerController) getSegmentController().getState().getController()).getMissileController();
	}

	public abstract boolean isTargetLocking(EC collection);

	public abstract boolean isHeatSeeking(EC collection);

	public boolean isTargetLocking(SegmentPiece p) {
		if (p != null) {
			EC ec = getCollectionManagersMap().get(p.getAbsoluteIndex());
			if (ec != null) {
				return isTargetLocking(ec);
			}
		}
		return false;
	}

	public boolean isHeatSeeking(SegmentPiece p) {
		if (p != null) {
			EC ec = getCollectionManagersMap().get(p.getAbsoluteIndex());
			if (ec != null) {
				return isHeatSeeking(ec);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableElementManager#getGUIUnitValues(org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.ElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(E firingUnit, EC col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		getStatistics(firingUnit, col, supportCol, effectCol, tmpOutput);

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Missile Unit"), firingUnit,
				new ModuleValueEntry(Lng.str("Damage"), StringTools.formatPointZero(tmpOutput.damage)),
				new ModuleValueEntry(Lng.str("Missile Speed"), StringTools.formatPointZero(tmpOutput.speed)),
				new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(tmpOutput.distance)),
				new ModuleValueEntry(Lng.str("Reload(ms)"), StringTools.formatPointZero(tmpOutput.reload)),
				new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()),
				new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging())
		);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableElementManager#handleSingleActivation(org.schema.game.common.data.SegmentPiece)
	 */
	// @Override
	// public void handleSingleActivation(SegmentPiece controller) {
	// if(getCollectionManagers().isEmpty()){
	// //nothing to shoot with
	// return;
	// }
	// long time = System.currentTimeMillis();
	// int unpowered = 0;
	// for(int i = 0; i < getCollectionManagers().size(); i++){
	// ControlBlockElementCollectionManager<E, EC, EM> m = getCollectionManagers().get(i);
	// boolean selected = controller.equalsPos(m.getControllerPos());
	// if(selected){
	// System.err.println("MISSILE CONTROLLING "+ElementKeyMap.getInfo(controlling)+" ON "+m.getCollection().size()+" on "+getSegmentController().getState());
	// for(int u = 0; u < m.getCollection().size(); u++){
	// E c = m.getCollection().get(u);
	// if(c.canShoot(time)){
	// Vector3i v = c.getOutput();
	//
	// Vector3f weapontOutputWorldPos = new Vector3f(
	// v.x - SegmentData.SEG_HALF,
	// v.y - SegmentData.SEG_HALF,
	// v.z - SegmentData.SEG_HALF);
	//
	//
	// getWorldTransform().transform(weapontOutputWorldPos);
	//
	// Vector3i centeralizedControlledFromPos = new Vector3i(controlledFromOrig);
	// centeralizedControlledFromPos.sub(Ship.core);
	// Vector3f camPos = getSegmentController().getAbsoluteElementWorldPosition(centeralizedControlledFromPos, new Vector3f());
	//
	// Vector3f forward = getShooringDir(getSegmentController(),
	// GlUtil.getForwardVector(new Vector3f(), getWorldTransform()),
	// GlUtil.getUpVector(new Vector3f(), getWorldTransform()),
	// GlUtil.getRightVector(new Vector3f(), getWorldTransform()),
	// shootingForwardTemp,
	// shootingUpTemp, shootingRightTemp, true, m.getControllerPos());
	//
	// PhysicsExt physics = getSegmentController().getPhysics();
	// forward.scale(c.getDistance());
	// forward.add(camPos);
	//
	// ClosestRayResultCallback testRayCollisionPoint =
	// physics.testRayCollisionPoint(camPos, forward, false, getSegmentController(), null, false);
	//
	// if(testRayCollisionPoint.hasHit()){
	//
	// shootingDirTemp.sub(testRayCollisionPoint.hitPointWorld, weapontOutputWorldPos);
	// }else{
	// shootingDirTemp.set(getShooringDir(getSegmentController(),
	// GlUtil.getForwardVector(new Vector3f(), getWorldTransform()),
	// GlUtil.getUpVector(new Vector3f(), getWorldTransform()),
	// GlUtil.getRightVector(new Vector3f(), getWorldTransform()),
	// shootingForwardTemp,
	// shootingUpTemp, shootingRightTemp, true, m.getControllerPos()));
	// }
	// float speed = c.getSpeed() * ((GameStateInterface)getState()).getGameState().isRelativeProjectiles();
	// //							if(isOnServer()){
	// //								System.err.println("FIRE ON SERVER: "+weapontOutputWorldPos+": "+shootingDirTemp);
	// //							}
	// shootingDirTemp.normalize();
	// shootingDirTemp.scale(speed);
	// c.updateLastShoot();
	//
	// if(getSegmentController().isOnServer() ){
	// Transform transform = new Transform();
	// transform.setIdentity();
	// transform.origin.set(weapontOutputWorldPos);
	// System.err.println("ADD MISSILE: "+transform.origin);
	// short effectType = 0;
	// float effectRatio = 0;
	// float effectSize = 0;
	// addMissile(getSegmentController(), transform, new Vector3f(shootingDirTemp), speed, c.getBlastRadius(), c.getDamage(), c.getDistance(), effectType, effectRatio, effectSize, null);
	// }
	//
	// Transform t = new Transform();
	// t.setIdentity();
	// t.origin.set(weapontOutputWorldPos);
	//
	// if(!getSegmentController().isOnServer()){
	// Controller.queueTransformableAudio("0022_spaceship user - missile fire 1", t, 5);
	// notifyShooting(c);
	// }
	//
	// }else{
	// }
	// }
	// if(m.getCollection().isEmpty() && clientIsOwnShip()){
	// ((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nNo Weapons connected \nto entry point",0);
	// }
	// }
	// }
	// if(unpowered > 0 && clientIsOwnShip()){
	// ((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nWeapon Elements unpowered: "+unpowered,0);
	// }
	// if(getCollectionManagers().isEmpty() && clientIsOwnShip()){
	// ((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nNo weapon controllers",0);
	// }
	// }
	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		if (!unit.isFlightControllerActive()) {
			if (debug) {
				System.err.println("NO 1");
			}
			return;
		}
		if (getCollectionManagers().isEmpty()) {
			// nothing to shoot with
			if (debug) {
				System.err.println("NO 2 " + this.getClass().getSimpleName() + "; " + unit.getClass().getSimpleName());
			}
			return;
		}
		try {
			if (!convertDeligateControls(unit, shootContainer.controlledFromOrig, shootContainer.controlledFrom)) {
				if (debug) {
					System.err.println("NO 3");
				}
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int unpowered = 0;
		// System.err.println("FIREING CONTROLLERS: "+getCollectionManagers().size()+" FROM: "+controlledFrom+" on "+getSegmentController().getState());
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			EC m = getCollectionManagers().get(i);
			boolean selected = unit.isSelected(m.getControllerElement(), shootContainer.controlledFrom);
			boolean aiSelected = unit.isAISelected(m.getControllerElement(), shootContainer.controlledFrom, i, getCollectionManagers().size(), m);
			if (!selected && debug) {
				System.err.println("NO 4 " + this.getClass().getSimpleName() + "; " + unit.getClass().getSimpleName() + "; " + m.getControllerPos() + "; " + shootContainer.controlledFrom);
			}
			if (selected && aiSelected) {
				boolean controlling = shootContainer.controlledFromOrig.equals(shootContainer.controlledFrom);
				controlling |= getControlElementMap().isControlling(shootContainer.controlledFromOrig, m.getControllerPos(), controllerId);
				// System.err.println("CONTROLLING "+controlling+" ON "+getSegmentController().getState());
				if (!controlling && debug) {
					System.err.println("NO 4");
				}
				if (controlling) {
					if (!m.allowedOnServerLimit()) {
						continue;
					}
					if (shootContainer.controlledFromOrig.equals(Ship.core)) {
						unit.getControlledFrom(shootContainer.controlledFromOrig);
					}
					if (debug) {
						System.err.println("FIRE MISSILE " + unit.getClass());
					}
					m.handleControlShot(unit, timer);
					// System.err.println("CONTROLLING "+controlling+" ON "+m.getCollection().size()+" on "+getSegmentController().getState());
					// for (int u = 0; u < m.getElementCollections().size(); u++) {
					// E c = m.getElementCollections().get(u);
					//
					//
					//
					// if (c.canUse(timer.currentTime, true)) {
					// Vector3i v = c.getOutput();
					//
					// shootContainer.weapontOutputWorldPos.set(
					// v.x - SegmentData.SEG_HALF,
					// v.y - SegmentData.SEG_HALF,
					// v.z - SegmentData.SEG_HALF);
					//
					// if (getSegmentController().isOnServer()) {
					// getSegmentController().getWorldTransform().transform(shootContainer.weapontOutputWorldPos);
					// } else {
					// getSegmentController().getWorldTransformOnClient().transform(shootContainer.weapontOutputWorldPos);
					// }
					// shootContainer.centeralizedControlledFromPos.set(shootContainer.controlledFromOrig);
					// shootContainer.centeralizedControlledFromPos.sub(Ship.core);
					// shootContainer.camPos.set(getSegmentController().getAbsoluteElementWorldPosition(shootContainer.centeralizedControlledFromPos, shootContainer.tmpCampPos));
					// boolean focused = false;
					// boolean lead = true;
					// unit.getShootingDir(getSegmentController(), shootContainer, c.getDistanceFull(), c.getSpeed(), m.getControllerPos(), focused, lead);
					//
					// shootContainer.shootingDirTemp.normalize();
					// shootContainer.shootingDirTemp.scale(c.getSpeed());
					//
					//
					// assert(!Float.isNaN(shootContainer.shootingDirTemp.x) );
					// doShot(c, m, shootContainer, c.getSpeed(), unit.getAquiredTarget(), unit.getPlayerState(), timer);
					//
					// Transform t = new Transform();
					// t.setIdentity();
					// t.origin.set(shootContainer.weapontOutputWorldPos);
					//
					// if (!getSegmentController().isOnServer()) {
					// ((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship user - missile fire one", t, 1);
					// notifyShooting(c);
					// }
					//
					// } else {
					// }
					// }
					if (m.getElementCollections().isEmpty() && clientIsOwnShip()) {
						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Weapons connected \nto entry point"), 0);
					}
				}
			}
		}
		if (unpowered > 0 && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nWeapon Elements unpowered: %s", unpowered), 0);
		}
		if (getCollectionManagers().isEmpty() && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo weapon controllers."), 0);
		}
	}

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		handleReload(iconPos, iconSize, controllerPos, reloadListener);
	}

	public WeaponStatisticsData getStatistics(E firingUnit, EC col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol, WeaponStatisticsData output) {
		if (effectCol != null) {
			col.setEffectTotal(effectCol.getTotalSize());
		} else {
			col.setEffectTotal(0);
		}
		output.damage = firingUnit.getDamage();
		output.speed = firingUnit.getSpeed();
		output.distance = firingUnit.getDistance();
		output.reload = firingUnit.getReloadTimeMs();
		output.powerConsumption = (float) firingUnit.getPowerConsumedPerSecondCharging();
		output.split = 1;
		output.mode = 1;
		output.effectRatio = 0;
		if (supportCol != null) {
			MissileUnitModifier<?> gui = (MissileUnitModifier<?>) getAddOn().getGUI(col, firingUnit, supportCol, effectCol);

			output.mode = gui.outputMode;
			output.damage = gui.outputDamage;
			output.speed = gui.outputSpeed;
			output.distance = gui.outputDistance;
			output.reload = gui.outputReload;
			output.powerConsumption = gui.outputPowerConsumption;
			output.split = gui.outputSplit;
		}
		if (effectCol != null) {
			EffectElementManager<?, ?, ?> effect = (EffectElementManager<?, ?, ?>) effectCol.getElementManager();
			output.effectRatio = CombinationAddOn.getRatio(col, effectCol);
		}
		return output;
	}

	@Override
	public double calculateWeaponDamageIndex() {
		double dps = 0;
		for (EC a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (E w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				dps += tmpOutput.damage / (tmpOutput.reload / 1000d);
			}
		}
		return dps;
	}

	@Override
	public double calculateWeaponRangeIndex() {
		double range = 0;
		double c = 0;
		for (EC a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (E w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				range += tmpOutput.distance;
				c++;
			}
		}
		return c > 0 ? range / c : 0;
	}

	@Override
	public double calculateWeaponHitPropabilityIndex() {
		double range = 0;
		for (EC a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (E w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				// rough estimate of mo much "stuff" is in the air and how fast it hits
				range += (DumbMissileElementManager.BASE_SPEED * tmpOutput.split) / (tmpOutput.reload / 1000d);
			}
		}
		return range;
	}

	@Override
	public double calculateWeaponSpecialIndex() {
		double special = 0;
		for (EC a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (E w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
			// special += tmpOutput.stop +
			// tmpOutput.pull +
			// tmpOutput.push +
			// tmpOutput.bonusShields +
			// tmpOutput.bonusBlocks +
			// tmpOutput.explosiveRadius +
			// tmpOutput.powerConsumptionBonus +
			// tmpOutput.armorEfficiency +
			// tmpOutput.addPowerDamage;
			}
		}
		return special;
	}

	@Override
	public double calculateWeaponPowerConsumptionPerSecondIndex() {
		double powerConsumption = 0;
		for (EC a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (E w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				powerConsumption += w.getPowerConsumption();
			}
		}
		return powerConsumption;
	}
	// @Override
	// public boolean receiveDistribution(ReceivedDistribution d,
	// NetworkEntity networkObject) {
	// //client needs a dummy
	// return receiveDistribution(d);
	//
	// }
	//
	// @Override
	// public void updateFromNT(NetworkObject o) {
	// }
	//
	// @Override
	// public void updateToFullNT(NetworkObject networkObject) {
	// //		if(getSegmentController().isOnServer()){
	// //			for(int i = 0; i < getCollectionManagers().size(); i++){
	// //				((DistributionCollectionManager<?>)getCollectionManagers().get(i)).sendDistribution();
	// //			}
	// //		}
	// }
}
