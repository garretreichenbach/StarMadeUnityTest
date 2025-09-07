package org.schema.game.common.controller.elements.thrust;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.ThrusterElementManagerListener;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.FastMath;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.controller.elements.config.DoubleReactorDualConfigElement;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.ControllerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.world.Sector;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

public class ThrusterElementManager extends UsableControllableSingleElementManager<ThrusterUnit, ThrusterCollectionManager, ThrusterElementManager> implements PowerConsumer {

	@ConfigurationElement(name = "PowPerThrustCollection")
	public static double THRUSTER_BONUS_POW_PER_UNIT = 1.125;

	@ConfigurationElement(name = "PowTotalThrust")
	public static DoubleReactorDualConfigElement POW_TOTAL = new DoubleReactorDualConfigElement();

	@ConfigurationElement(name = "MulTotalThrust")
	public static DoubleReactorDualConfigElement MUL_TOTAL = new DoubleReactorDualConfigElement();

	@ConfigurationElement(name = "LinearMulBonusThrust")
	public static DoubleReactorDualConfigElement LINEAR_MUL_TOTAL = new DoubleReactorDualConfigElement();
	// allows thruster blocks to have the same, normalized mass as other system blocks,
	// whilst being able to replicate the effect of arbitrarily large increases/decreases in thruster mass on TWR.
	// - Ithirahad

	@ConfigurationElement(name = "ReactorPowerPowerConsumptionPerBlockResting")
	public static double REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING = 1.125;

	@ConfigurationElement(name = "ReactorPowerPowerConsumptionPerBlockInUse")
	public static double REACTOR_POWER_CONSUMPTION_PER_BLOCK_IN_USE = 1.125;

	@ConfigurationElement(name = "ThrustPowerconsumptionPerBlock")
	public static double POWER_CONSUMPTION_PER_BLOCK = 1.125;

	@ConfigurationElement(name = "UnitCalcMult")
	public static DoubleReactorDualConfigElement UNIT_CALC_MULT = new DoubleReactorDualConfigElement();

	@ConfigurationElement(name = "UnitCalcStyle")
	public static UnitCalcStyle UNIT_CALC_STYLE = UnitCalcStyle.BOX_DIM_MULT;

	@ConfigurationElement(name = "MinThrustMassRatio")
	public static float MIN_THRUST_MASS_RATIO = 0.0f;

	@ConfigurationElement(name = "MaxThrustMassRatio")
	public static float MAX_THRUST_MASS_RATIO = 3;

	@ConfigurationElement(name = "MaxThrustToMassAcceleration")
	public static float MAX_THRUST_TO_MASS_ACC = 5f;

	@ConfigurationElement(name = "ThrustMassRatioMaxSpeedMultiplier")
	public static float THUST_MASS_RATIO_MAX_SPEED_MULTIPLIER = 1;

	@ConfigurationElement(name = "ThrustMassRatioMaxSpeedAddition")
	public static float THUST_MASS_RATIO_MAX_SPEED_ADD = 0.5f;

	@ConfigurationElement(name = "ThrusterMinReactorPower")
	public static float THRUSTER_MIN_REACTOR_POWER = 0.5f;

	@ConfigurationElement(name = "ThrustRotPercentMult")
	public static float THRUST_ROT_PERCENT_MULT = 1;

	@ConfigurationElement(name = "InertiaPow")
	public static float INTERTIA_POW = 0.5f;

	@ConfigurationElement(name = "MaxRotationalForceX")
	public static float MAX_ROTATIONAL_FORCE_X = 0;

	@ConfigurationElement(name = "MaxRotationalForceY")
	public static float MAX_ROTATIONAL_FORCE_Y = 0;

	@ConfigurationElement(name = "MaxRotationalForceZ")
	public static float MAX_ROTATIONAL_FORCE_Z = 0;

	@ConfigurationElement(name = "BaseRotationalForceX")
	public static float BASE_ROTATIONAL_FORCE_X = 0;

	@ConfigurationElement(name = "BaseRotationalForceY")
	public static float BASE_ROTATIONAL_FORCE_Y = 0;

	@ConfigurationElement(name = "BaseRotationalForceZ")
	public static float BASE_ROTATIONAL_FORCE_Z = 0;

	@ConfigurationElement(name = "ThrustBalanceChangeApplyTimeInSecs")
	public static double THRUST_CHANGE_APPLY_TIME_IN_SEC = 1.125;

	private final Vector3f velocity = new Vector3f();

	private final Vector3f up = new Vector3f();

	private final Vector3f down = new Vector3f();

	private final Vector3f left = new Vector3f();

	private final Vector3f right = new Vector3f();

	private final Vector3f forward = new Vector3f();

	private final Vector3f backward = new Vector3f();

	private final Vector3f dir = new Vector3f();

	private final Vector3f joyDir = new Vector3f();

	private final Vector3f dirApplied = new Vector3f();

	private Vector3f linearVelocityTmp = new Vector3f();

	private short lastUpdate;

	private float timeTracker;

	private long lastSendLimitWarning;

	public final Vector3f thrustBalanceAxis = new Vector3f(0.3333333f, 0.3333333f, 0.3333333f);

	public float rotationBalance = 0.5f;

	private float sharedThrustCache;

	private long lastSharedThrust;

	private boolean usingThrust;

	private float powered;

	private Vector3f rawDir = new Vector3f();

	private long lastConsumeCalc;

	private float sharedRestingConsume;

	private float sharedChargingConsume;

	public float ruleModifierOnThrust = 1;

	public ThrusterElementManager(SegmentController segmentController) {
		super(segmentController, ThrusterCollectionManager.class);
		//INSERTED CODE
		ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!listeners.isEmpty()){
			for (ThrusterElementManagerListener listener : listeners) {
				listener.instantiate(this);
			}
		}
		///
	}

	public float getSingleThrust() {
		float thrusters = getCollection().getTotalThrust();
		if (thrusters == 0) {
			return 0;
		}
		return Math.max(0.5f, thrusters);
	}

	public float getSingleThrustRaw() {
		float thrusters = getCollection().getTotalThrustRaw();
		if (thrusters == 0) {
			return 0;
		}
		return Math.max(0.5f, thrusters);
	}

	public float getActualThrust() {
		// if(isUsingPowerReactors()){
		// return getSingleThrust();
		// }
		if (getSegmentController() instanceof Ship && (((Ship) getSegmentController())).getManagerContainer().thrustConfiguration.thrustSharing) {
			long t = System.currentTimeMillis();
			if (t - lastSharedThrust > 700) {
				sharedThrustCache = getSharedThrust();
				ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
				if(!listeners.isEmpty()){
					for (ThrusterElementManagerListener listener : listeners) {
						sharedThrustCache = listener.getSharedThrust(this, sharedThrustCache);
					}
				}
				///
				lastSharedThrust = t;
			}
			return sharedThrustCache;
		} else {
			float singleThrust = getSingleThrust();
			//INSERTED CODE
			ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
			if(!listeners.isEmpty()){
				for (ThrusterElementManagerListener listener : listeners) {
					singleThrust = listener.getSingleThrust(this, singleThrust);
				}
			}
			///
			return singleThrust;
		}
	}

	private float getSharedThrust() {
		float rawThrust = getSharedThrustRaw();
		float totalThrust = ((float) (Math.pow(rawThrust, ThrusterElementManager.POW_TOTAL.get(isUsingPowerReactors())) * ThrusterElementManager.MUL_TOTAL.get(isUsingPowerReactors())));
		return totalThrust;
	}

	float getSharedThrustRaw() {
		float rawThrust = getSingleThrustRaw();
		for (RailRelation s : getSegmentController().railController.next) {
			if (s.rail.getSegmentController() instanceof Ship) {
				rawThrust += ((Ship) s.docked.getSegmentController()).getManagerContainer().getThrusterElementManager().getSharedThrustRaw();
			}
		}
		return rawThrust;
	}

	/**
	 * @return the lastUpdate
	 */
	public short getLastUpdateNum() {
		return lastUpdate;
	}

	public float getThrustMassRatio() {
		float tmr = Math.max(MIN_THRUST_MASS_RATIO, Math.min(MAX_THRUST_MASS_RATIO, getActualThrust() / Math.max(0.00001f, getSegmentController().getMass())));//INSERTED CODE
		ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!listeners.isEmpty()){
			for (ThrusterElementManagerListener listener : listeners) {
				tmr = listener.getThrustMassRatio(this, tmr);
			}
		}
		///
		return tmr;
	}

	/**
	 * @return the maxVelocity
	 */
	public float getMaxVelocity(Vector3f curVelo) {
		if (((getSegmentController()) instanceof PlayerControllable && !((PlayerControllable) getSegmentController()).getAttachedPlayers().isEmpty() && Sector.isTutorialSector(((PlayerControllable) getSegmentController()).getAttachedPlayers().get(0).getCurrentSector()))) {
			if (getSegmentController().isClientOwnObject()) {
				((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Max speed in tutorial\narea is 15 m/s."), 0);
			}
			return 15;
		}
		float maxSpeed = getMaxSpeedAbsolute();
		maxSpeed = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_TOP_SPEED, maxSpeed);
		//INSERTED CODE
		ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!listeners.isEmpty()){
			for (ThrusterElementManagerListener listener : listeners) {
				maxSpeed = listener.getMaxSpeed(this, maxSpeed);
			}
		}
		///
		float curSpeed = curVelo.length();
		if (curSpeed > maxSpeed) {
			return curSpeed - 0.05f;
		}
		return maxSpeed;
	}

	public float getMaxSpeedAbsolute() {
		float topSpeedBonus = ((SendableSegmentController) getSegmentController()).getBlockEffectManager().status.topSpeed;
		float maxGalaxySpeed = ((GameStateInterface) getSegmentController().getState()).getGameState().getMaxGalaxySpeed();
		maxGalaxySpeed *= ((SendableSegmentController) getSegmentController()).getBlockEffectManager().status.thrustPercent;
		float ratio = getThrustMassRatio();
		ratio += THUST_MASS_RATIO_MAX_SPEED_ADD;
		ratio *= THUST_MASS_RATIO_MAX_SPEED_MULTIPLIER;
		maxGalaxySpeed *= ratio;
		float maxSpeed = maxGalaxySpeed + (topSpeedBonus * maxGalaxySpeed);

		//INSERTED CODE
		ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!listeners.isEmpty()){
			for (ThrusterElementManagerListener listener : listeners) {
				maxSpeed = listener.getMaxSpeedAbsolute(this, maxSpeed);
			}
		}
		///

		return maxSpeed;
	}

	/**
	 * @return the velocity
	 */
	public Vector3f getVelocity() {
		return velocity;
	}

	// @Override
	// public void handleSingleActivation(SegmentPiece controller)  {
	// if(((SendableSegmentController)getSegmentController()).getBlockEffectManager().hasEffect(BlockEffectTypes.CONTROLLESS)){
	// RigidBody body = (RigidBody)getPhysicsDataContainer().getObject();
	// body.getLinearVelocity(linearVelocityTmp);
	// if(linearVelocityTmp.length() > getCurrentMaxVelocity()){
	// linearVelocityTmp.normalize();
	// linearVelocityTmp.scale(getCurrentMaxVelocity());
	// body.setLinearVelocity(linearVelocityTmp);
	// }
	//
	// if(getSegmentController().isClientOwnObject()){
	//
	// BlockEffect effect = ((SendableSegmentController)getSegmentController()).getBlockEffectManager().getEffect(BlockEffectTypes.CONTROLLESS);
	//
	// long d = System.currentTimeMillis() - effect.getStart();
	// d = effect.getDuration() - d;
	//
	// ((GameClientState)getSegmentController().getState()).getController()
	// .popupAlertTextMessage("You've been hit by a pulse!\nShip systems not responding!\n("+d/1000+" sec)", 0);
	// }
	// return;
	// }
	// float thrusters = getActualThrust() ;
	//
	// RigidBody body = (RigidBody)getPhysicsDataContainer().getObject();
	//
	// if(lastMoveUpdate+30 < System.currentTimeMillis()){
	//
	// GlUtil.getForwardVector(dir, getSegmentController().getWorldTransform());
	//
	// if(dir.length() > 0){
	// float powerConsumed = thrustManager.getTotalThrust();
	// if(getPowerManager().getPower() < thrustManager.getTotalThrust()){
	// if(getPowerManager().getPower() <= 0){
	// return;
	// }else{
	// double power = getPowerManager().getPower();
	// getPowerManager().consumePowerInstantly(power );
	// thrusters = (float) power;
	// }
	// }else{
	// if(!getPowerManager().consumePowerInstantly(powerConsumed )){
	// if(clientIsOwnShip()){
	// ((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nThrusters have no power",0);
	// }
	// return;
	// }
	// }
	//
	// body.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	//
	// dir.scale( (thrusters * 0.5f));
	// body.applyCentralImpulse(dir);
	// body.getLinearVelocity(linearVelocityTmp);
	// if(linearVelocityTmp.length() > getCurrentMaxVelocity()){
	// linearVelocityTmp.normalize();
	// linearVelocityTmp.scale(getCurrentMaxVelocity());
	// body.setLinearVelocity(linearVelocityTmp);
	// }
	// this.lastUpdate = getState().getUpdateNumber();
	// body.getLinearVelocity(getVelocity());
	// }
	// }
	//
	// }
	public Transform getPlayerTrans(Transform out) {
		out.setIdentity();
		GlUtil.setForwardVector(forward, out);
		GlUtil.setUpVector(up, out);
		GlUtil.setRightVector(right, out);
		return out;
	}

	@Override
	public void onControllerChange() {
	}

	public void orientate(float fixedTimeStep) {
		Vector3f oForce = ((Ship) getSegmentController()).getOrientationForce();
//INSERTED CODE		oForce.scale(getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_TURN_RATE, 1f));ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!FastListenerCommon.thrusterElementManagerListeners.isEmpty()){
			for (ThrusterElementManagerListener listener : FastListenerCommon.thrusterElementManagerListeners) {
				oForce = listener.getOrientationPower(this, oForce);
			}
		}
		///
		
		if (!getAttachedPlayers().isEmpty()) {
			if (isOnServer() || ((GameClientState) getState()).getCurrentSectorEntities().containsKey(getSegmentController().getId())) {
				getSegmentController().getPhysics().orientate(getSegmentController(), forward, up, right, oForce.x, oForce.y, oForce.z, fixedTimeStep);
			}
		}
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(ThrusterUnit firingUnit, ThrusterCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Thruster Unit"), firingUnit, new ModuleValueEntry(Lng.str("Thrust"), StringTools.formatPointZero(firingUnit.thrust)), new ModuleValueEntry(Lng.str("PowerConsumption"), StringTools.formatPointZero(firingUnit.getPowerConsumption() * getUpdateFrequency())));
	}

	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		if (!getSegmentController().checkBlockMassServerLimitOk()) {
			if (!getSegmentController().isOnServer() && System.currentTimeMillis() - lastSendLimitWarning > 5000) {
				int blockLimit = ((GameStateInterface) getState()).getGameState().getBlockLimit(getSegmentController());
				double massLimit = FastMath.round(((GameStateInterface) getState()).getGameState().getMassLimit(getSegmentController()) * 100.0) / 100.0;
				String limit = blockLimit > 0 ? blockLimit + " " + Lng.str("blocks") : "";
				limit += limit.length() > 0 && massLimit > 0 ? " " + Lng.str("and") + " " : "";
				limit += massLimit > 0 ? massLimit + " " + Lng.str("mass") : "";
				getSegmentController().popupOwnClientMessage(Lng.str("WARNING! SERVER MASS/BLOCK REACHED!\nThrust deactivated!\nServer doesn't allow more than ", limit), ServerMessage.MESSAGE_TYPE_ERROR);
				lastSendLimitWarning = System.currentTimeMillis();
			}
			return false;
		}
		// turrets need orientation
		return !getSegmentController().railController.isDocked() || (getSegmentController().railController.isDockedAndExecuted() && getSegmentController().railController.isTurretDocked());
	}

	@Override
	protected String getTag() {
		return "thruster";
	}

	@Override
	public ThrusterCollectionManager getNewCollectionManager(SegmentPiece position, Class<ThrusterCollectionManager> clazz) {
		return new ThrusterCollectionManager(getSegmentController(), this);
	}

	// private long pressedForwardTime;
	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		if (getSegmentController().getMass() <= 0) {
			return;
		}
		if (!unit.isFlightControllerActive()) {
			return;
		}
		if (!unit.canFlyShip()) {
			return;
		}
		// Thrust for acceleration is limited to server config
		float thrusters = Math.min(getActualThrust(), Math.max(0.00001f, getSegmentController().getMass() * MAX_THRUST_TO_MASS_ACC));
		if (getSegmentController().railController.isDockedOrDirty()) {
			if (getSegmentController().railController.isDocked()) {
				unit.getUp(up);
				down.set(up);
				down.negate();
				unit.getRight(right);
				left.set(right);
				left.negate();
				unit.getForward(forward);
				backward.set(forward);
				backward.negate();
				boolean inTrans = unit.getPlayerState() != null && System.currentTimeMillis() - unit.getPlayerState().inControlTransition < ControllerState.DELAY_ORIENTATION_MS || !unit.getPlayerState().canRotate();
				if (inTrans) {
				} else {
					// System.err.println("RAIL::::: "+getSegmentController()+" ::: "+forward);
					getSegmentController().getPhysics().orientate(getSegmentController(), forward, up, right, 0, 0, 0, timer.getDelta());
				// System.err.println("WT :::: "+getSegmentController()+"   "+getSegmentController().getWorldTransform().basis);
				}
			}
			return;
		} else if (getSegmentController().getDockingController().isDocked()) {
			unit.getUp(up);
			down.set(up);
			down.negate();
			unit.getRight(right);
			left.set(right);
			left.negate();
			unit.getForward(forward);
			backward.set(forward);
			backward.negate();
			boolean inTrans = unit.getPlayerState() != null && System.currentTimeMillis() - unit.getPlayerState().inControlTransition < ControllerState.DELAY_ORIENTATION_MS || !unit.getPlayerState().canRotate();
			if (inTrans) {
			} else {
				getSegmentController().getPhysics().orientate(getSegmentController(), forward, up, right, 0, 0, 0, timer.getDelta());
			}
			return;
		}
		RigidBody body = (RigidBody) getPhysicsDataContainer().getObject();
		if (body == null) {
			return;
		}
		body.getLinearVelocity(linearVelocityTmp);
		float curSpeed = linearVelocityTmp.length();
		float maxSpeed = getMaxVelocity(linearVelocityTmp);
		if (((SendableSegmentController) getSegmentController()).getBlockEffectManager().hasEffect(BlockEffectTypes.CONTROLLESS)) {
			if (curSpeed > maxSpeed) {
				linearVelocityTmp.normalize();
				linearVelocityTmp.scale(maxSpeed);
				body.setLinearVelocity(linearVelocityTmp);
			}
			if (getSegmentController().isClientOwnObject()) {
				BlockEffect effect = ((SendableSegmentController) getSegmentController()).getBlockEffectManager().getEffect(BlockEffectTypes.CONTROLLESS);
				long d = System.currentTimeMillis() - effect.getStart();
				d = effect.getDuration() - d;
				((GameClientState) getSegmentController().getState()).getController().popupAlertTextMessage(Lng.str("Ship control systems\nnot responding!\n(%s sec)", d / 1000), "C", 0);
			}
			return;
		}
		unit.getUp(up);
		down.set(up);
		down.negate();
		unit.getRight(right);
		left.set(right);
		left.negate();
		unit.getForward(forward);
		backward.set(forward);
		backward.negate();
		float repulInv = 1.0f - ((Ship) getSegmentController()).getManagerContainer().getRepulseManager().getThrustToRepul();
		float rotInv = 1.0f - rotationBalance;
		float thrustScale = Math.max(0f, Math.min(1f, (repulInv + rotInv) / 2));
		left.scale(thrustScale * thrustBalanceAxis.x * 3f * 2f);
		right.scale(thrustScale * thrustBalanceAxis.x * 3f * 2f);
		up.scale(thrustScale * thrustBalanceAxis.y * 3f * 2f);
		down.scale(thrustScale * thrustBalanceAxis.y * 3f * 2f);
		forward.scale(thrustScale * thrustBalanceAxis.z * 3f * 2f);
		backward.scale(thrustScale * thrustBalanceAxis.z * 3f * 2f);
		dir.set(0, 0, 0);
		if (unit.isDown(KeyboardMappings.FORWARD_SHIP)) {
			rawDir.z += 1;
			dir.add(forward);
		}
		if (unit.isDown(KeyboardMappings.BACKWARDS_SHIP)) {
			rawDir.z -= 1;
			dir.add(backward);
		}
		if (unit.isDown(KeyboardMappings.STRAFE_LEFT_SHIP)) {
			rawDir.x -= 1;
			dir.add(right);
		}
		if (unit.isDown(KeyboardMappings.STRAFE_RIGHT_SHIP)) {
			rawDir.x += 1;
			dir.add(left);
		}
		if (unit.isDown(KeyboardMappings.UP_SHIP)) {
			rawDir.y += 1;
			dir.add(up);
		}
		if (unit.isDown(KeyboardMappings.DOWN_SHIP)) {
			rawDir.y -= 1;
			dir.add(down);
		}
		usingThrust = dir.length() > 0f || (unit.isDown(KeyboardMappings.BRAKE) && curSpeed > 0.01);
		// set to default for orientation arguments
		unit.getUp(up);
		down.set(up);
		down.negate();
		unit.getRight(right);
		left.set(right);
		left.negate();
		unit.getForward(forward);
		backward.set(forward);
		backward.negate();
		if (thrusters < 0.1) {
			if (clientIsOwnShip() && getSegmentController().getTotalElements() > 1) {
				if (dir.length() > 0) {
					if (((GameClientState) getState()).getWorldDrawer() != null) {
						((GameClientState) getState()).getWorldDrawer().getGuiDrawer().notifyEffectHit(this.getSegmentController(), OffensiveEffects.NO_THRUST);
					}
				}
			// ((GameClientState)getState()).getController().popupInfoTextMessage("WARNING!\n \nNo thrusters connected to core",0);
			}
			thrusters = 0.1f;
		} else if (thrusters <= 0.5f) {
			if (clientIsOwnShip()) {
				((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNot enough Thrusters for\n the mass of your ship\nIt will only move slowly"), 0);
			}
		}
		// FIXME left/right is switched
		joyDir.set(0, 0, 0);
		unit.handleJoystickDir(joyDir, new Vector3f(forward), new Vector3f(left), new Vector3f(up));
		if (joyDir.length() > 0) {
			// if someone presses both W and uses joystick button/axis
			dir.add(joyDir);
			dir.normalize();
		}
		// one udpate for every 30 ms
		float updateFrequency = getUpdateFrequency();
		timeTracker += timer.getDelta();
		// make sure it's save against long lags
		timeTracker = Math.min(updateFrequency * 100f, timeTracker);
		final float MIN_THRUST = 0.1f;
		while (timeTracker >= updateFrequency) {
			dirApplied.set(dir);
			timeTracker -= updateFrequency;
			if (dirApplied.length() > 0) {
				float nThrust = thrusters;
				if (isUsingPowerReactors()) {
					float pw = Math.max(THRUSTER_MIN_REACTOR_POWER, powered);
					nThrust = Math.max(MIN_THRUST, thrusters * pw);
				} else {
					float powerConsumed = getPowerConsumption();
					if (getPowerManager().getPower() < powerConsumed) {
						if (getPowerManager().getPower() <= 0) {
							nThrust = 0.001f;
						} else {
							double power = getPowerManager().getPower();
							getPowerManager().consumePowerInstantly(power);
							nThrust = thrusters * ((float) power / powerConsumed);
						}
					} else {
						if (!getPowerManager().consumePowerInstantly(powerConsumed)) {
							if (clientIsOwnShip()) {
								((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\nNo power left for thrusters!"), 0);
							}
							nThrust = 0.01f;
						}
					}
				}
				nThrust = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_ACCELERATION, nThrust);
				applyThrust(dirApplied, nThrust, body, getSegmentController(), 1f, linearVelocityTmp);
				this.lastUpdate = getState().getNumberOfUpdate();
				body.getLinearVelocity(velocity);
			} else {
				if (unit.isDown(KeyboardMappings.BRAKE)) {
					float bThrust = Math.max(0.1f, thrusters * rotInv);
					Vector3f linearVelocity = body.getLinearVelocity(new Vector3f());
					if (linearVelocity.length() > 1) {
						if (isUsingPowerReactors()) {
							bThrust = Math.max(MIN_THRUST, bThrust * Math.max(THRUSTER_MIN_REACTOR_POWER, powered));
						} else {
							float powerConsumed = getPowerConsumption();
							if (getPowerManager().getPower() < powerConsumed) {
								if (getPowerManager().getPower() <= 0) {
									bThrust = 0.1f;
								} else {
									double power = getPowerManager().getPower();
									getPowerManager().consumePowerInstantly(power);
									bThrust = (float) power;
								}
							} else {
								if (!getPowerManager().consumePowerInstantly(powerConsumed)) {
									if (clientIsOwnShip()) {
										((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nThrusters have no power!"), 0);
									}
									bThrust = 0.1f;
								}
							}
						}
						bThrust = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_BRAKING, bThrust);
						Vector3f norm = new Vector3f(linearVelocity);
						norm.normalize();
						norm.negate();
						norm.scale(bThrust * 0.5f);
						Vector3f normTest = new Vector3f(norm);
						normTest.scale(body.getInvMass());
						if (linearVelocity.length() < normTest.length()) {
							linearVelocity.set(0, 0, 0);
							body.setLinearVelocity(linearVelocity);
						} else {
							body.applyCentralImpulse(norm);
						}
					} else {
						linearVelocity.set(0, 0, 0);
						body.setLinearVelocity(linearVelocity);
					}
					velocity.x = 0;
					velocity.y = 0;
					velocity.z = 0;
				}
			}
			// Vector3f oForce = ((Ship)getSegmentController()).getOrientationForce();
			//
			// if(!getAttachedPlayers().isEmpty()){
			// getSegmentController().getPhysics().orientate(getSegmentController(),
			// forward, up, right, oForce.x, oForce.y, oForce.z, timer);
			// }
			if (unit.canRotateShip()) {
				orientate(timer.getDelta());
			}
		}
	}

	public static void applyThrust(Vector3f dirApplied, float nThrust, RigidBody body, SegmentController segmentController, float maxVeloMult, Vector3f linearVelocityTmp) {
		applyThrustForce(dirApplied, nThrust, body, segmentController, maxVeloMult, linearVelocityTmp, false);
	}

	public static void applyThrustForce(Vector3f dirApplied, float nThrust, RigidBody body, SegmentController segmentController, float maxVeloMult, Vector3f linearVelocityTmp, boolean forced) {
		body.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
//		if(!segmentController.isNPCFactionControlledAI()) AudioController.fireAudioEventID(928);
		//Don't play sounds for NPC ships as they call this method way too much which leads to audio spam.
		dirApplied.scale(nThrust * 0.3f);
		Vector3f velocityChangeVec = new Vector3f(dirApplied);
		// Convert impulse to velocity
		velocityChangeVec.scale(body.getInvMass());
		// linearVelocityTmp is the velocity before this calculation
		body.getLinearVelocity(linearVelocityTmp);
		Vector3f linearVelocityMod = new Vector3f(linearVelocityTmp);
		linearVelocityMod.add(velocityChangeVec);
		// linearVelocityMod is the velocity + impulse direction
		if (forced || !segmentController.getHpController().isRebooting()) {
			float maxVel;
			if (segmentController instanceof Ship) {
				maxVel = ((Ship) segmentController).getManagerContainer().getThrusterElementManager().getMaxVelocity(linearVelocityTmp) * maxVeloMult;
			} else {
				maxVel = ((GameStateInterface) segmentController.getState()).getGameState().getMaxGalaxySpeed();
			}
			float maxVelSqrd = maxVel * maxVel;
			// If the desired velocity is greater than the max velocity
			if (linearVelocityMod.lengthSquared() > maxVelSqrd) {
				// If the previous velocity is greater than the max velocity
				if (linearVelocityTmp.lengthSquared() > maxVelSqrd) {
					linearVelocityTmp.normalize();
					linearVelocityTmp.scale(maxVel);
				}
				// Calculate impulse, and scale back to
				linearVelocityMod.normalize();
				linearVelocityMod.scale(maxVel);
				linearVelocityMod.sub(linearVelocityTmp);
				// Convert from velocity to impulse...
				linearVelocityMod.scale(1.0F / body.getInvMass());
				body.applyCentralImpulse(linearVelocityMod);
			} else {
				// If the desired velocity is less than the max, just add impulse
				body.applyCentralImpulse(dirApplied);
			}
		}
	}

	public float getPowerConsumption() {
		float powerConsumed;
		if (POWER_CONSUMPTION_PER_BLOCK <= 0) {
			float actualThrust = getActualThrust();
			powerConsumed = actualThrust <= 0.5f ? 0 : actualThrust;
		} else {
			if (getSegmentController() instanceof Ship && (((Ship) getSegmentController())).getManagerContainer().thrustConfiguration.thrustSharing) {
				powerConsumed = getSharedConsume(POWER_CONSUMPTION_PER_BLOCK);
			} else {
				powerConsumed = (float) (POWER_CONSUMPTION_PER_BLOCK * getCollection().getTotalSize());
			}
		}

		return powerConsumed;
	}

	private float getSharedConsume(double consumptionPerBlock) {
		float t = (float) (consumptionPerBlock * totalSize);
		for (RailRelation s : getSegmentController().railController.next) {
			if (s.rail.getSegmentController() instanceof Ship) {
				t += ((Ship) s.docked.getSegmentController()).getManagerContainer().getThrusterElementManager().getSharedConsume(consumptionPerBlock);
			}
		}
		return t;
	}

	public static final float getUpdateFrequency() {
		return 0.03f;
	}

	public boolean isUsingThrust() {
		return usingThrust;
	}

	public void setUsingThrust(boolean usingThrust) {
		this.usingThrust = usingThrust;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		double powCons;
		if (getSegmentController() instanceof Ship && (((Ship) getSegmentController())).getManagerContainer().thrustConfiguration.thrustSharing) {
			if (getState().getUpdateTime() - lastConsumeCalc > 1000) {
				sharedRestingConsume = getSharedConsume(ThrusterElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING);
				sharedChargingConsume = getSharedConsume(ThrusterElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_IN_USE);
				lastConsumeCalc = getState().getUpdateTime();
			}
			powCons = sharedRestingConsume;
		} else {
			powCons = totalSize * ThrusterElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING;
		}
		powCons = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_POWER_CONSUMPTION, powCons);
		//INSERTED CODE
		ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!listeners.isEmpty()){
			for (ThrusterElementManagerListener listener : listeners) {
				powCons = listener.getPowerConsumptionResting(this, powCons);
			}
		}
		///
		return powCons;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		double powCons;
		if (getSegmentController() instanceof Ship && (((Ship) getSegmentController())).getManagerContainer().thrustConfiguration.thrustSharing) {
			if (getState().getUpdateTime() - lastConsumeCalc > 1000) {
				sharedRestingConsume = getSharedConsume(ThrusterElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING);
				sharedChargingConsume = getSharedConsume(ThrusterElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_IN_USE);
				lastConsumeCalc = getState().getUpdateTime();
			}
			powCons = sharedChargingConsume;
		} else {
			powCons = totalSize * ThrusterElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_IN_USE;
		}
		powCons = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_POWER_CONSUMPTION, powCons);
		//INSERTED CODE
		ArrayList<ThrusterElementManagerListener> listeners = FastListenerCommon.thrusterElementManagerListeners;
		if(!listeners.isEmpty()){
			for (ThrusterElementManagerListener listener : listeners) {
				powCons = listener.getPowerConsumptionCharging(this, powCons);
			}
		}
		///
		return powCons;
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return usingThrust;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.THRUST;
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public String getName() {
		return "ThrusterElementManager";
	}

	public Vector3f getInputVectorNormalize(Vector3f out) {
		out.set(dir);
		if (out.lengthSquared() > 0) {
			out.normalize();
		}
		return out;
	}

	@Override
	public void dischargeFully() {
	}
}
