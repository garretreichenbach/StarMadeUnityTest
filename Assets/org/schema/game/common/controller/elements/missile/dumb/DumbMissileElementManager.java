package org.schema.game.common.controller.elements.missile.dumb;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.MissileCombiSettings;
import org.schema.game.common.controller.elements.combination.MissileCombinationAddOn;
import org.schema.game.common.controller.elements.combination.modifier.MissileUnitModifier;
import org.schema.game.common.controller.elements.config.FloatReactorDualConfigElement;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.controller.elements.missile.MissileElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;

import javax.vecmath.Vector3f;

public class DumbMissileElementManager extends MissileElementManager<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> {

	@ConfigurationElement(name = "DamageLinear")
	public static FloatReactorDualConfigElement BASE_DAMAGE = new FloatReactorDualConfigElement();

	@ConfigurationElement(name = "DamageExponent")
	public static float DAMAGE_EXP = 0.33f;

	@ConfigurationElement(name = "DamageExpMultiplier")
	public static float DAMAGE_EXP_MULT = 100f;

	@ConfigurationElement(name = "Distance")
	public static float BASE_DISTANCE = 1000;

	@ConfigurationElement(name = "AdditiveDamage")
	public static float ADDITIVE_DAMAGE = 50;

	@ConfigurationElement(name = "Speed", description = "missile speed in percent of server max speed (1.0 = 100%)")
	public static float BASE_SPEED = 1.1f;

	@ConfigurationElement(name = "ReloadMs")
	public static float BASE_RELOAD = 10000;

	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 10;
	
	
	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamage")
	public static float ADDITIONAL_CAPACITY_USER_PER_DAMAGE = 10;
	
	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamageMult")
	public static float ADDITIONAL_CAPACITY_USER_PER_DAMAGE_MULT = 10;
	
	
	@ConfigurationElement(name = "PercentagePowerUsageCharging")
	public static float PERCENTAGE_POWER_USAGE_CHARGING = 10;
	
	@ConfigurationElement(name = "PercentagePowerUsageResting")
	public static float PERCENTAGE_POWER_USAGE_RESTING = 10;
	
	

	
	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;
	@ConfigurationElement(name = "PowerConsumption")
	public static float BASE_POWER_CONSUMPTION = 200;

	@ConfigurationElement(name = "AdditionalPowerConsumptionPerUnitMult")
	public static float ADDITIONAL_POWER_CONSUMPTION_PER_UNIT_MULT = 0.1f;


	@ConfigurationElement(name = "ChasingTurnSpeedWithTargetInFront")
	public static float CHASING_TURN_SPEED_WITH_TARGET_IN_FRONT = 2.03f;

	@ConfigurationElement(name = "ChasingTurnSpeedWithTargetInBack")
	public static float CHASING_TURN_SPEED_WITH_TARGET_IN_BACK = 1.1f;
	
	@ConfigurationElement(name = "BombActivationTimeSec")
	public static float BOMB_ACTIVATION_TIME_SEC = 1.1f;
	
	//Missile HP
	@ConfigurationElement(name = "MissileHPCalcStyle")
	public static UnitCalcStyle MISSILE_HP_CALC_STYLE = UnitCalcStyle.LINEAR;
	
	@ConfigurationElement(name = "MissileHPMin")
	public static float MISSILE_HP_MIN = 10f;
	
	@ConfigurationElement(name = "MissileHPPerDamage")
	public static float MISSILE_HP_PER_DAMAGE = 1f;

	@ConfigurationElement(name = "MissileHPExp")
	public static float MISSILE_HP_EXP = 1f;
	
	@ConfigurationElement(name = "MissileHPExpMult")
	public static float MISSILE_HP_EXP_MULT = 1f;

	
	@ConfigurationElement(name = "MissileHPLogOffset")
	public static float MISSILE_HP_LOG_OFFSET = 10f;

	@ConfigurationElement(name = "MissileHPLogFactor")
	public static float MISSILE_HP_LOG_FACTOR = 10f;

	@ConfigurationElement(name = "EffectConfiguration")
	public static InterEffectSet basicEffectConfiguration = new InterEffectSet();

	@ConfigurationElement(name = "LockOnTimeSec")
	public static float LOCK_ON_TIME_SEC;
	
	@ConfigurationElement(name = "LockedOnExpireTimeSec")
	public static float LOCKED_ON_EXPIRE_TIME_SEC;

	@ConfigurationElement(name = "PossibleZoom")
	public static float POSSIBLE_ZOOM = 0;
	

	private MissileCombinationAddOn addOn;

	public DumbMissileElementManager(SegmentController segmentController) {
		super(ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID, ElementKeyMap.MISSILE_DUMB_ID, segmentController);

		addOn = new MissileCombinationAddOn(this, (GameStateInterface) this.getState());
	}

	@Override
	public void addMissile(SegmentController segmentController,
	                       Transform transform, Vector3f shootingDir, float speed,
	                       float damage, float distance, long weaponId,
	                       SimpleTransformableSendableObject target, short colorType) {

		
//		if(segmentController.railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBodySegmentController) {
//			RigidBodySegmentController r = (RigidBodySegmentController)segmentController.railController.getRoot().getPhysicsDataContainer().getObject();
//			
//			Vector3f velo = r.getLinearVelocity(new Vector3f());
//			if(velo.lengthSquared() > 0) {
//				speed = velo.length();
//				velo.normalize();
//			}else {
//				velo.set(0,0,1);
//				speed = 0;
//			}
//			
//			float activation = DumbMissileElementManager.BOMB_ACTIVATION_TIME_SEC;
//			getMissileController().addBombMissile(
//					segmentController,
//					transform,
//					activation,
//					velo,
//					speed,
//					blastRadius,
//					damage,
//					distance,
//					weaponId,
//					colorType);
//		}
		
		
		
		getMissileController().addDumbMissile(segmentController, transform, shootingDir, speed, damage, distance, weaponId, colorType);
	}

	@Override
	public boolean isTargetLocking(DumbMissileCollectionManager collection) {
		return getMissileMode(collection) == 2;
	}

	@Override
	public boolean isHeatSeeking(DumbMissileCollectionManager collection) {
		return getMissileMode(collection) == 1;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(DumbMissileUnit firingUnit,
	                                             DumbMissileCollectionManager col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		if (effectCol != null) {
			col.setEffectTotal(effectCol.getTotalSize());
		} else {
			col.setEffectTotal(0);
		}
		float damage = firingUnit.getDamage();
		float speed = firingUnit.getSpeed();
		float distance = firingUnit.getDistance();
		float reload = firingUnit.getReloadTimeMs();
		float powerConsumption = firingUnit.getPowerConsumption();
		float split = 1;
		float mode = 0;
		float effectRatio = 0;
		if (supportCol != null) {
			MissileUnitModifier<?> gui = (MissileUnitModifier<?>) getAddOn().getGUI(col, firingUnit, supportCol, effectCol);

			damage = gui.outputDamage;
			speed = gui.outputSpeed;
			distance = gui.outputDistance;
			reload = gui.outputReload;
			powerConsumption = gui.outputPowerConsumption;
			split = gui.outputSplit;
			mode = gui.outputMode;
		}

		if (effectCol != null) {
			EffectElementManager<?, ?, ?> effect = (EffectElementManager<?, ?, ?>) effectCol.getElementManager();


			effectRatio = CombinationAddOn.getRatio(col, effectCol);

		}

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Missile Unit"), firingUnit,
				new ModuleValueEntry(Lng.str("Type"), mode == 0 ? Lng.str("Dumb Missile") : (mode == 1 ? Lng.str("Heat Seeking") : Lng.str("LockOn"))),
				new ModuleValueEntry(Lng.str("Damage/Missile"), StringTools.formatPointZero(damage / split)),
				new ModuleValueEntry(Lng.str("Explosion Shield Damage Bonus x"), StringTools.formatPointZero(VoidElementManager.EXPLOSION_SHIELD_DAMAGE_BONUS)),
				new ModuleValueEntry(Lng.str("Explosion Hull Damage Bonus x"), StringTools.formatPointZero(VoidElementManager.EXPLOSION_HULL_DAMAGE_BONUS)),
				new ModuleValueEntry(Lng.str("Missile Speed"), StringTools.formatPointZero(speed)),
				new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(distance)),
				new ModuleValueEntry(Lng.str("Shots"), StringTools.formatPointZero(split)),
				new ModuleValueEntry(Lng.str("Reload(ms)"), StringTools.formatPointZero(reload)),
				new ModuleValueEntry(Lng.str("Power Consumption"), StringTools.formatPointZero(powerConsumption)),
				new ModuleValueEntry(Lng.str("Effect Ratio(%):"), StringTools.formatPointZero(effectRatio))
		);
	}

	@Override
	public CombinationAddOn<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager, MissileCombiSettings> getAddOn() {
		return addOn;
	}

	@Override
	protected String getTag() {
		return "missile";
	}

	@Override
	public DumbMissileCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<DumbMissileCollectionManager> clazz) {
		return new DumbMissileCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Missile System Collective");
	}

	public boolean isUsingRegisteredActivation() {
		return true;
	}
}
