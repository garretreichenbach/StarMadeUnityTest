package org.schema.game.common.controller.elements.cannon;

import com.bulletphysics.dynamics.RigidBody;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.CannonCombiSettings;
import org.schema.game.common.controller.elements.combination.CannonCombinationAddOn;
import org.schema.game.common.controller.elements.combination.modifier.CannonUnitModifier;
import org.schema.game.common.controller.elements.config.FloatReactorDualConfigElement;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.*;
import static org.schema.game.common.controller.elements.ammo.cannon.CannonCapacityElementManager.CANNON_CAPACITY_RELOAD_MODE;

public class CannonElementManager extends UsableCombinableControllableElementManager<CannonUnit, CannonCollectionManager, CannonElementManager, CannonCombiSettings> implements NTSenderInterface, NTReceiveInterface, BlockActivationListenerInterface, WeaponElementManagerInterface, IntegrityBasedInterface {

	static final double[] weightedLookupTable = new double[300];

	@ConfigurationElement(name = "AcidFormulaDefault")
	public static int ACID_FORMULA_DEFAULT = 0;

	@ConfigurationElement(name = "DamageLinear")
	public static FloatReactorDualConfigElement DAMAGE_LINEAR = new FloatReactorDualConfigElement();

	@ConfigurationElement(name = "DamageExponent")
	public static float DAMAGE_EXP = 0.33f;

	@ConfigurationElement(name = "DamageExpMultiplier")
	public static float DAMAGE_EXP_MULT = 100f;

	@ConfigurationElement(name = "Distance")
	public static float BASE_DISTANCE = 1000;

	@ConfigurationElement(name = "AdditiveDamage")
	public static float ADDITIVE_DAMAGE = 50;

	@ConfigurationElement(name = "Speed")
	public static float BASE_SPEED = 10;

	@ConfigurationElement(name = "ReloadMs")
	public static float BASE_RELOAD = 1000;

	@ConfigurationElement(name = "ImpactForce")
	public static float IMPACT_FORCE = 0.01f;

	@ConfigurationElement(name = "Recoil")
	public static float RECOIL = 0.1f;

	@ConfigurationElement(name = "CursorRecoilX")
	public static float CURSOR_RECOIL_X = 0.0001f;

	@ConfigurationElement(name = "CursorRecoilMinX")
	public static float CURSOR_RECOIL_MIN_X = 0.001f;

	@ConfigurationElement(name = "CursorRecoilMaxX")
	public static float CURSOR_RECOIL_MAX_X = 0.1f;

	@ConfigurationElement(name = "CursorRecoilDirX")
	public static float CURSOR_RECOIL_DIR_X = 0.0f;

	@ConfigurationElement(name = "CursorRecoilY")
	public static float CURSOR_RECOIL_Y = 0.0001f;

	@ConfigurationElement(name = "CursorRecoilMinY")
	public static float CURSOR_RECOIL_MIN_Y = 0.001f;

	@ConfigurationElement(name = "CursorRecoilMaxY")
	public static float CURSOR_RECOIL_MAX_Y = 0.1f;

	@ConfigurationElement(name = "CursorRecoilDirY")
	public static float CURSOR_RECOIL_DIR_Y = 0.0f;

	@ConfigurationElement(name = "CursorRecoilSpeedIn")
	public static float CURSOR_RECOIL_IN = 1.0f;

	@ConfigurationElement(name = "CursorRecoilSpeedInAddMod")
	public static float CURSOR_RECOIL_IN_ADD = 1.0f;

	@ConfigurationElement(name = "CursorRecoilSpeedInPowMult")
	public static float CURSOR_RECOIL_IN_POW_MULT = 1.0f;

	@ConfigurationElement(name = "CursorRecoilSpeedOut")
	public static float CURSOR_RECOIL_OUT = 5.0f;

	@ConfigurationElement(name = "CursorRecoilSpeedOutAddMod")
	public static float CURSOR_RECOIL_OUT_ADD = 1.0f;

	@ConfigurationElement(name = "CursorRecoilSpeedOutPowMult")
	public static float CURSOR_RECOIL_OUT_POW_MULT = 1.0f;

	@ConfigurationElement(name = "PowerConsumption")
	public static float BASE_POWER_CONSUMPTION = 10;

	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 10;

	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;

	@ConfigurationElement(name = "AdditionalPowerConsumptionPerUnitMult")
	public static float ADDITIONAL_POWER_CONSUMPTION_PER_UNIT_MULT = 0.1f;

	public static boolean debug = false;

	@ConfigurationElement(name = "EffectConfiguration")
	public static InterEffectSet basicEffectConfiguration = new InterEffectSet();
	@ConfigurationElement(name = "ProjectileWidth")
	public static float PROJECTILE_WIDTH_MULT = 1;

	@ConfigurationElement(name = "BasicPenetrationDepth")
	public static int PROJECTILE_PENETRATION_DEPTH_BASIC = 1;

	@ConfigurationElement(name = "PenetrationDepthExp")
	public static float PROJECTILE_PENETRATION_DEPTH_EXP = 0.5f;

	@ConfigurationElement(name = "PenetrationDepthExpMult")
	public static float PROJECTILE_PENETRATION_DEPTH_EXP_MULT = 0.5f;

	@ConfigurationElement(name = "AcidDamageMaxPropagation")
	public static int ACID_DAMAGE_MAX_PROPAGATION = 50;

	@ConfigurationElement(name = "AcidDamageFormulaConeStartWideWeight")
	public static float ACID_DAMAGE_FORMULA_CONE_START_WIDE_WEIGHT = 1.8f;

	@ConfigurationElement(name = "AcidDamageFormulaConeEndWideWeight")
	public static float ACID_DAMAGE_FORMULA_CONE_END_WIDE_WEIGHT = 0.2f;

	@ConfigurationElement(name = "AcidDamageMinOverPenModifier")
	public static float ACID_DAMAGE_MIN_OVER_PEN_MOD = 1f;

	@ConfigurationElement(name = "AcidDamageMaxOverPenModifier")
	public static float ACID_DAMAGE_MAX_OVER_PEN_MOD = 10f;

	@ConfigurationElement(name = "AcidDamageMinOverArmorModifier")
	public static float ACID_DAMAGE_MIN_OVER_ARMOR_MOD = 1f;

	@ConfigurationElement(name = "AcidDamageMaxOverArmorModifier")
	public static float ACID_DAMAGE_MAX_OVER_ARMOR_MOD = 3f;

	@ConfigurationElement(name = "AcidDamageOverArmorBaseReference")
	public static float ACID_DAMAGE_OVER_ARMOR_BASE = 250f;

	@ConfigurationElement(name = "DamageChargeMax")
	public static float DAMAGE_CHARGE_MAX = 0.05f;

	@ConfigurationElement(name = "DamageChargeSpeed")
	public static float DAMAGE_CHARGE_SPEED = 0.05f;

	@ConfigurationElement(name = "PossibleZoom")
	public static float POSSIBLE_ZOOM = 0;

	@ConfigurationElement(name = "Aimable")
	public static int AIMABLE = 1;

	//Base capacity used per shot is always 1; it's a cannon round. No need for a base ammo modifier here.

	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamage")
	public static float ADDITIONAL_CAPACITY_USED_PER_DAMAGE = 10;

	private final WeaponStatisticsData tmpOutput = new WeaponStatisticsData();

	private CannonCombinationAddOn addOn;

	private final ShootContainer shootContainer = new ShootContainer();

	public CannonElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.WEAPON_CONTROLLER_ID, ElementKeyMap.WEAPON_ID, segmentController);
		addOn = new CannonCombinationAddOn(this, (GameStateInterface) this.getState());
	}

	private final CannonCombiSettings combiSettings = new CannonCombiSettings();

	public CannonCombiSettings getCombiSettings() {
		return combiSettings;
	}

	void doShot(CannonUnit c, CannonCollectionManager m, ShootContainer shootContainer, PlayerState playerState, Timer timer) {
		ManagerModuleCollection<?, ?, ?> effectModuleCollection = null;
		m.setEffectTotal(0);
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
		if (m.getSlaveConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getSlaveConnectedElement());
			// System.err.println(getState()+" [WEAPON] FIRING WITH COMBINED WEAPON: "+ElementKeyMap.getInfo(m.getControllerElement().getType()).getName()+" + "+ElementKeyMap.getInfo(connectedType).getName());
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
			ShootingRespose handled = handleAddOn(this, m, c, managerModuleCollection, effectModuleCollection, shootContainer, null, playerState, timer, -1);
			handleResponse(handled, c, shootContainer.weapontOutputWorldPos);
		} else {
			boolean canUse = c.canUse(timer.currentTime, false);
			// try {
			// throw new Exception("SHOT "+c.isUsingPowerReactors()+"; "+isUsingPowerReactors()+"; "+(c.getReactorReloadNeeded() <= 0.000000001d));
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			if (canUse) {
				final long weaponId = m.getUsableId();
				if (isUsingPowerReactors() || consumePower(c.getPowerConsumption())) {
					/*AudioController.fireAudioEventRemote("CANNON_FIRE", getSegmentController().getId(), new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.CANNON, AudioTags.FIRE }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c))*/
					AudioController.fireAudioEventRemoteID(933, getSegmentController().getId(), AudioController.ent(getSegmentController(), c.getElementCollectionId(), c));
					//Todo: Use different sounds for different weapon sizes and types
					c.setStandardShotReloading();
					float capacityUsed = c.getBaseCapacityUsedPerShot() + (c.getAdditionalCapacityUsedPerDamage() * c.getDamage());
					getParticleController().addProjectile(getSegmentController(), shootContainer.weapontOutputWorldPos, shootContainer.shootingDirTemp, c.getDamage(), c.getDistance(), m.getAcidFormula().ordinal(), c.getProjectileWidth(), c.getPenetrationDepth(c.getDamage()), c.getImpactForce(), weaponId, m.getColor(), capacityUsed);
					handleRecoil(m, c, shootContainer.weapontOutputWorldPos, shootContainer.shootingDirTemp, c.getRecoil(), c.getDamage());
					m.damageProduced += c.getDamage();
///
					handleResponse(ShootingRespose.FIRED, c, shootContainer.weapontOutputWorldPos);
				} else {
					handleResponse(ShootingRespose.NO_POWER, c, shootContainer.weapontOutputWorldPos);
				}
			} else {
				handleResponse(ShootingRespose.RELOADING, c, shootContainer.weapontOutputWorldPos);
			}
		}
	}

	public void handleRecoil(CannonCollectionManager fireingCollection, CannonUnit firingUnit, Vector3f weaponOutputWorldPos, Vector3f shootingDir, float outputRecoil, float damage) {
		if (getSegmentController().railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBody) {
			RigidBody r = (RigidBody) getSegmentController().railController.getRoot().getPhysicsDataContainer().getObject();
			if (outputRecoil * damage == 0) {
				return;
			}
			Vector3f dir = new Vector3f(shootingDir);
			dir.negate();
			boolean negateTorque = true;
			// if(!isOnServer()) {
			// System.err.println("DAMAGE:: "+outputRecoil+"; "+damage+"; "+outputRecoil * damage);
			// }
			getSegmentController().railController.getRoot().hitWithPhysicalRecoil(weaponOutputWorldPos, dir, outputRecoil * damage, negateTorque);
		}
	}

	public void handleCursorRecoil(CannonCollectionManager fireingCollection, float damage, CannonCombiSettings settings) {
		if (getSegmentController().isClientOwnObject()) {
			if (Controller.getCamera() instanceof InShipCamera) {
				((InShipCamera) Controller.getCamera()).addRecoil(Math.min(settings.cursorRecoilMaxX, Math.max(settings.cursorRecoilMinX, damage * settings.cursorRecoilX)), Math.min(settings.cursorRecoilMaxY, Math.max(settings.cursorRecoilMinY, damage * settings.cursorRecoilY)), settings.cursorRecoilDirX, settings.cursorRecoilDirY, CURSOR_RECOIL_IN, CURSOR_RECOIL_IN_ADD, CURSOR_RECOIL_IN_POW_MULT, CURSOR_RECOIL_OUT, CURSOR_RECOIL_OUT_ADD, CURSOR_RECOIL_OUT_POW_MULT);
			}
		}
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		// System.err.println("WEAPON CONTROLLER ON ACTIVATE ON "+getSegmentController()+"; ON "+getSegmentController().getState());
		long absPos = piece.getAbsoluteIndex();
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			for (CannonUnit d : getCollectionManagers().get(i).getElementCollections()) {
				if (d.contains(absPos)) {
					d.setMainPiece(piece, active);
					return active ? 1 : 0;
				}
			}
		}
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.WEAPON_ID);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}

	@Override
	public void updateToFullNT(NetworkObject networkObject) {
	}

	@Override
	public CombinationAddOn<CannonUnit, CannonCollectionManager, CannonElementManager, CannonCombiSettings> getAddOn() {
		return addOn;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(CannonUnit firingUnit, CannonCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		getStatistics(firingUnit, col, supportCol, effectCol, tmpOutput);
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Weapon Unit"), firingUnit, new ModuleValueEntry(Lng.str("Total Damage/Projectile"), StringTools.formatPointZero(tmpOutput.damage / tmpOutput.split)), new ModuleValueEntry(Lng.str("Penetration (Blocks)"), firingUnit.getPenetrationDepth(tmpOutput.damage)), new ModuleValueEntry(Lng.str("Cannon Speed"), StringTools.formatPointZero(tmpOutput.speed)), new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(tmpOutput.distance)), new ModuleValueEntry(Lng.str("Shots"), StringTools.formatPointZero(tmpOutput.split)), new ModuleValueEntry(Lng.str("Reload(ms)"), StringTools.formatPointZero(tmpOutput.reload)), new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()), new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging()), new ModuleValueEntry(Lng.str("Effect Ratio(%):"), StringTools.formatPointZero(tmpOutput.effectRatio)));
	}

	@Override
	protected String getTag() {
		return "cannon";
	}

	@Override
	public CannonCollectionManager getNewCollectionManager(SegmentPiece position, Class<CannonCollectionManager> clazz) {
		return new CannonCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Cannon System Collective");
	}

	private static GUITextOverlay chargesText;

	public static final Vector4f chargeColor = new Vector4f(0.8F, 0.5F, 0.3F, 0.4F);

	public boolean checkCapacity() {
		if (getSegmentController() == null) return false;

		SegmentController root = getSegmentController().railController.getRoot();
		if (root instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> mc = ((ManagedSegmentController<?>) root).getManagerContainer();
			return mc.getAmmoCapacity(MISSILE) >= 1;
		}
		return false;
	}

    public class DrawReloadListener implements ReloadListener {

		@Override
		public String onDischarged(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}

		@Override
		public String onReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}

		@Override
		public String onFull(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent, long controllerPos) {
			return null;
		}

		@Override
		public void drawForElementCollectionManager(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadcolor, long controllerPos) {
			CannonCollectionManager wep = getCollectionManagersMap().get(controllerPos);
			if (wep != null) {
				CannonCombiSettings cp = wep.getWeaponChargeParams();
				if (cp.damageChargeMax > 0 && wep.damageCharge > 0) {
					if (chargesText == null) {
						chargesText = new GUITextOverlay(FontSize.MEDIUM_15, (InputState) getState());
						chargesText.onInit();
					}
					float p = Math.min(wep.damageCharge / cp.damageChargeMax, 0.99999f);
					drawReload(state, iconPos, iconSize, chargeColor, false, p, true, wep.damageCharge, (int) cp.damageChargeMax, -1, chargesText);
				}
			}
		}
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		long curTime = System.currentTimeMillis();
		if (!unit.isFlightControllerActive()) {
			if (debug) {
				System.err.println("NOT ACTIVE");
			}
			return;
		}
		if (getCollectionManagers().isEmpty()) {
			if (debug) {
				System.err.println("NO WEAPONS");
			}
			// nothing to shoot with
			return;
		}
		try {
			if (!convertDeligateControls(unit, shootContainer.controlledFromOrig, shootContainer.controlledFrom)) {
				if (debug) {
					System.err.println("NO SLOT");
				}
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int unpowered = 0;
		getPowerManager().sendNoPowerHitEffectIfNeeded();
		if (debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + shootContainer.controlledFrom);
		}
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			CannonCollectionManager m = getCollectionManagers().get(i);
			boolean selected = unit.isSelected(m.getControllerElement(), shootContainer.controlledFrom);
			boolean aiSelected = unit.isAISelected(m.getControllerElement(), shootContainer.controlledFrom, i, getCollectionManagers().size(), m);
			if (selected && aiSelected) {
				boolean controlling = shootContainer.controlledFromOrig.equals(shootContainer.controlledFrom);
				controlling |= getControlElementMap().isControlling(shootContainer.controlledFromOrig, m.getControllerPos(), controllerId);
				if (debug) {
					System.err.println("Controlling " + controlling + " " + getState());
				}
				if (controlling) {
					if (!m.allowedOnServerLimit()) {
						continue;
					}
					if (shootContainer.controlledFromOrig.equals(Ship.core)) {
						unit.getControlledFrom(shootContainer.controlledFromOrig);
					}
					if (debug) {
						System.err.println("Controlling " + controlling + " " + getState() + ": " + m.getElementCollections().size());
					}
					m.handleControlShot(unit, timer);
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

	public boolean isUsingRegisteredActivation() {
		return true;
	}

	public WeaponStatisticsData getStatistics(CannonUnit firingUnit, CannonCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol, WeaponStatisticsData output) {
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
			CannonUnitModifier gui = (CannonUnitModifier) getAddOn().getGUI(col, firingUnit, supportCol, effectCol);
			output.damage = gui.outputDamage;
			output.speed = gui.outputSpeed;
			output.distance = gui.outputDistance;
			output.reload = gui.outputReload;
			output.powerConsumption = gui.outputPowerConsumption;
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
		for (CannonCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (CannonUnit w : a.getElementCollections()) {
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
		for (CannonCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (CannonUnit w : a.getElementCollections()) {
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
		for (CannonCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (CannonUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				// rough estimate of mo much "stuff" is in the air and how fast it hits
				range += (CannonElementManager.BASE_SPEED * tmpOutput.split) / (tmpOutput.reload / 1000d);
			}
		}
		return range;
	}

	@Override
	public double calculateWeaponSpecialIndex() {
		double special = 0;
		for (CannonCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (CannonUnit w : a.getElementCollections()) {
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
		for (CannonCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (CannonUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				powerConsumption += w.getPowerConsumption();
			}
		}
		return powerConsumption;
	}

	private final AmmoWeaponDrawReloadListener reloadListener = new AmmoWeaponDrawReloadListener(CANNON,CANNON_CAPACITY_RELOAD_MODE);

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		handleReload(iconPos, iconSize, controllerPos, reloadListener);
	}
}
