package org.schema.game.common.controller.elements.effectblock;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.BlockEffectManager;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;
import org.schema.schine.common.language.Lng;

public abstract class EffectElementManager<E extends EffectUnit<E, EC, EM>, EC extends EffectCollectionManager<E, EC, EM>, EM extends EffectElementManager<E, EC, EM>> extends UsableControllableElementManager<E, EC, EM> {

	public EffectElementManager(short controller, short controlling,
	                            SegmentController segmentController) {
		super(controller, controlling, segmentController);
	}

	public BlockEffectManager getBlockEffectManager() {
		return ((SendableSegmentController) getSegmentController()).getBlockEffectManager();
	}

	protected boolean isActiveEffect(EC m) {
		BlockEffect effectByBlockIdentifyer = m.getCurrentBlockEffect();
		return effectByBlockIdentifyer != null && effectByBlockIdentifyer.isAlive();
	}

	protected void deactivateEffect(EC m) {
		BlockEffect currentBlockEffect = m.getCurrentBlockEffect();
		if (currentBlockEffect != null) {
			currentBlockEffect.end();
		}
	}
	public abstract InterEffectSet getInterEffect();
	
//	public BlockEffect getEffect(BlockEffectTypes blockEffectTypes, SendableSegmentController c, EC collect, float effectCap, float powerConsumption, float basicMultiplier) {
//		float size = collect.getTotalSize() * collect.getElementManager().getDefensiveBaseMultiplier();
//		long idPos = collect.getControllerElement().getAbsoluteIndexWithType4();
//		switch (blockEffectTypes) {
//			case CONTROLLESS:
//				return new ControllessEffect(c);
//			case STOP:
//				return new StopEffect(c, size);
//			case NULL_EFFECT:
//				return new NullEffect(c);
//			case PULL:
//				throw new IllegalArgumentException("cannot used this effect defensively " + blockEffectTypes);
//			case PUSH:
//				throw new IllegalArgumentException("cannot used this effect defensively " + blockEffectTypes);
//			case STATUS_ANTI_GRAVITY:
//				return new StatusAntiGravityEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_ARMOR_HARDEN:
//				return new StatusArmorHardenEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_GRAVITY_EFFECT_IGNORANCE:
//				return new StatusGravityEffectIgnoranceEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_PIERCING_PROTECTION:
//				return new StatusPiercingProtectionEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_POWER_SHIELD:
//				return new StatusPowerShieldEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_ARMOR_HP_ABSORPTION_BONUS:
//				return new StatusArmorHpAbsorptionBonusEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_ARMOR_HP_DEDUCTION_BONUS:
//				return new StatusArmorHpDeductionBonusEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_SHIELD_HARDEN:
//				return new StatusShieldHardenEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case STATUS_TOP_SPEED:
//				return new StatusTopSpeedEffect(c, (int) size, idPos, effectCap, powerConsumption, basicMultiplier);
//			case TAKE_OFF:
//				Vector3f f = GlUtil.getForwardVector(new Vector3f(), c.getWorldTransform());
//				return new TakeOffEffect(c, (int) size, f.x, f.y, f.z);
//			case EVADE:
//				return new EvadeEffect(c, (int) size);
//			default:
//				throw new IllegalArgumentException("unknown block effect: " + blockEffectTypes);
//
//		}
//
//	}
//
//	private final void activateEffect(EC m) {
//		m.addEffect(getEffect(getDefensiveEffectType(), (SendableSegmentController) getSegmentController(), m, getDefensiveEffectCapPercent(),
//				getDefensiveBaseBasePowerConsumption() * (1f / getDefensiveBaseMultiplier()), getDefensiveBaseMultiplier()));
//	}
//
//	public abstract float getDefensiveEffectCapPercent();
//
//	public abstract boolean isEffectIgnoreShields();
//
//	public abstract float getDefensiveBaseMultiplier();
//
//	public abstract BlockEffectTypes getDefensiveEffectType();
//
//	public abstract boolean isPiercing();
//
//	public abstract boolean isPunchThrough();
//
//	public abstract boolean isExplosive();
//
//	public abstract float getPierchingDamagePresevedOnImpact();
//
//	public abstract float getPunchThroughDamagePreserved();
//
//	public abstract float getCannonAdditionalPowerDamageBonusPercentage();
//
//	public abstract float getCannonShieldDamageBonus();
//
//	public abstract float getCannonBlockDamageBonus();
//
//	public abstract int getCannonExplosiveRadius();
//
//	public abstract float getCannonArmorEfficiency();
//
//	public abstract float getCannonPush();
//
//	public abstract float getCannonPull();
//
//	public abstract float getCannonGrab();
//
//	public abstract float getCannonBasePowerConsumption();
//
//	public abstract float getCannonSystemHPBonus();
//
//	public abstract float getCannonArmorHPBonus();
//
//	public abstract float getMissileAdditionalPowerDamageBonusPercentage();
//
//	public abstract float getMissileShieldDamageBonus();
//
//	public abstract float getMissileBlockDamageBonus();
//
//	public abstract float getMissileTotalDamageBonus();
//
//	public abstract int getMissileExplosiveRadius();
//
//	public abstract float getMissileArmorEfficiency();
//
//	public abstract float getMissilePush();
//
//	public abstract float getMissilePull();
//
//	public abstract float getMissileGrab();
//
//	public abstract float getMissileBasePowerConsumption();
//
//	public abstract float getMissileSystemHPBonus();
//
//	public abstract float getMissileArmorHPBonus();
//
//	public abstract float getBeamAdditionalPowerDamageBonusPercentage();
//
//	public abstract float getBeamShieldDamageBonus();
//
//	public abstract float getBeamBlockDamageBonus();
//
//	public abstract int getBeamExplosiveRadius();
//
//	public abstract float getBeamArmorEfficiency();
//
//	public abstract float getBeamPush();
//
//	public abstract float getBeamPull();
//
//	public abstract float getBeamGrab();
//
//	public abstract float getBeamBasePowerConsumption();
//
//	public abstract float getBeamSystemHPBonus();
//
//	public abstract float getBeamArmorHPBonus();
//
//	public abstract float getPulseAdditionalPowerDamageBonusPercentage();
//
//	public abstract float getPulseShieldDamageBonus();
//
//	public abstract float getPulseBlockDamageBonus();
//
//	public abstract float getPulseTotalDamageBonus();
//
//	public abstract int getPulseExplosiveRadius();
//
//	public abstract float getPulseArmorEfficiency();
//
//	public abstract float getPulsePush();
//
//	public abstract float getPulsePull();
//
//	public abstract float getPulseGrab();
//
//	public abstract float getPulseBasePowerConsumption();
//
//	public abstract float getPulseSystemHPBonus();
//
//	public abstract float getPulseArmorHPBonus();
//
//	public abstract float getDefensiveBaseBasePowerConsumption();
//
//	public double modifyShieldDamage(double inputDamage, DamageDealerType damageType, float effectRatio) {
//		double damageBefore = inputDamage;
//		double shieldDamageBonus = 0;
//		switch (damageType) {
//			case BEAM:
//				shieldDamageBonus = getBeamShieldDamageBonus();
//				break;
//			case MISSILE:
//				shieldDamageBonus = getMissileShieldDamageBonus();
//				break;
//			case PROJECTILE:
//				shieldDamageBonus = getCannonShieldDamageBonus();
//				break;
//			case PULSE:
//				shieldDamageBonus = getPulseShieldDamageBonus();
//				break;
//		}
//		inputDamage += inputDamage * (effectRatio * shieldDamageBonus);
//
////		System.err.println("Shield hit with effect '" + this.getTag() + "': " + damageBefore + " -> " + inputDamage + " R(" + effectRatio + "); S(" + shieldDamageBonus + ")");
//		return inputDamage;
//	}
//
//	public float getPowerDamage(float inputDamage, float effectRatio, FastSegmentControllerStatus status) {
//		return inputDamage * effectRatio * getCannonAdditionalPowerDamageBonusPercentage() * (1f - status.powerShield);
//	}
//
//	public float modifyBlockDamage(float inputDamage, DamageDealerType damageType, float effectRatio) {
//		float damageBefore = inputDamage;
//
//		float blockDamageBonus = 0;
//		switch (damageType) {
//			case BEAM:
//				blockDamageBonus = getBeamBlockDamageBonus();
//				break;
//			case MISSILE:
//				blockDamageBonus = getMissileBlockDamageBonus();
//				break;
//			case PROJECTILE:
//				blockDamageBonus = getCannonBlockDamageBonus();
//				break;
//			case PULSE:
//				blockDamageBonus = getPulseBlockDamageBonus();
//				break;
//		}
//
//		inputDamage += inputDamage * (effectRatio * blockDamageBonus);
//
//		//		System.err.println("Block hit with effect '"+this.getTag()+"': "+damageBefore+" -> "+inputDamage+" R("+effectRatio+"); S("+blockDamageBonus+")");
//		return inputDamage;
//	}
//
//	public float modifyTotalDamage(float inputDamage, DamageDealerType damageType, float effectRatio) {
//		float damageBefore = inputDamage;
//
//		float bonus = 0;
//		switch (damageType) {
//			case MISSILE:
//				bonus = getMissileTotalDamageBonus();
//				break;
//			case PULSE:
//				bonus = getPulseTotalDamageBonus();
//				break;
//		}
//
//		inputDamage += inputDamage * (effectRatio * bonus);
//
//		//		System.err.println("Block hit with effect '"+this.getTag()+"': "+damageBefore+" -> "+inputDamage+" R("+effectRatio+"); S("+blockDamageBonus+")");
//		return inputDamage;
//	}
//
//	public float modifyArmorHPDamage(float inputDamage, DamageDealerType damageType, float effectRatio) {
//		float damageBefore = inputDamage;
//
//		float blockDamageBonus = 0;
//		switch (damageType) {
//			case BEAM:
//				blockDamageBonus = getBeamArmorHPBonus();
//				break;
//			case MISSILE:
//				blockDamageBonus = getMissileArmorHPBonus();
//				break;
//			case PROJECTILE:
//				blockDamageBonus = getCannonArmorHPBonus();
//				break;
//			case PULSE:
//				blockDamageBonus = getPulseArmorHPBonus();
//				break;
//		}
//
//		inputDamage += inputDamage * (effectRatio * blockDamageBonus);
//
//		//		System.err.println("Block hit with effect '"+this.getTag()+"': "+damageBefore+" -> "+inputDamage+" R("+effectRatio+"); S("+blockDamageBonus+")");
//		return inputDamage;
//	}
//
//	public float modifySystemHPDamage(float inputDamage, DamageDealerType damageType, float effectRatio) {
//		float damageBefore = inputDamage;
//
//		float blockDamageBonus = 0;
//		switch (damageType) {
//			case BEAM:
//				blockDamageBonus = getBeamSystemHPBonus();
//				break;
//			case MISSILE:
//				blockDamageBonus = getMissileSystemHPBonus();
//				break;
//			case PROJECTILE:
//				blockDamageBonus = getCannonSystemHPBonus();
//				break;
//			case PULSE:
//				blockDamageBonus = getPulseSystemHPBonus();
//				break;
//		}
//
//		inputDamage += inputDamage * (effectRatio * blockDamageBonus);
//
//		//		System.err.println("Block hit with effect '"+this.getTag()+"': "+damageBefore+" -> "+inputDamage+" R("+effectRatio+"); S("+blockDamageBonus+")");
//		return inputDamage;
//	}
//
//	public float modifyArmorEfficiency(DamageDealerType damageType, float effectRatio) {
//		switch (damageType) {
//			case BEAM:
//				return getBeamArmorEfficiency() * effectRatio;
//			case MISSILE:
//				return getMissileArmorEfficiency() * effectRatio;
//			case PROJECTILE:
//				return getCannonArmorEfficiency() * effectRatio;
//			case PULSE:
//				return getPulseArmorEfficiency() * effectRatio;
//		}
//		return 0;
//	}
//
//	public float modifyRadius(float radius, DamageDealerType damageType, float effectRatio) {
//		float radiusBonus = 0;
//		switch (damageType) {
//			case BEAM:
//				radiusBonus = getBeamExplosiveRadius();
//				break;
//			case MISSILE:
//				radiusBonus = getMissileExplosiveRadius();
//				break;
//			case PROJECTILE:
//				radiusBonus = getCannonExplosiveRadius();
//				break;
//			case PULSE:
//				radiusBonus = getPulseExplosiveRadius();
//				break;
//		}
//		float weightedBonus = (effectRatio * radiusBonus);
//		assert (weightedBonus >= 0) : weightedBonus + "; " + effectRatio + " * " + radiusBonus;
//		radius += weightedBonus;
//		return radius;
//	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableElementManager#getGUIUnitValues(org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.ElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(E firingUnit, EC col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		float totalMass = (getSegmentController().getMassWithDocks() * VoidElementManager.DEVENSIVE_EFFECT_MAX_PERCENT_MASS_MULT);
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Effect System"), firingUnit,
				new ModuleValueEntry(Lng.str("Effect"), col.getName()),
				new ModuleValueEntry(Lng.str("Efficiency(%)"), StringTools.formatPointZero(col.getTotalSize() * 100.0f / totalMass)),
				new ModuleValueEntry(Lng.str("Size"), StringTools.formatPointZero(col.getTotalSize())),
				new ModuleValueEntry(Lng.str("Size/Ship"), StringTools.formatPointZeroZero(col.getTotalSize() / totalMass))
		);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Effect System Collective");
	}

//	@Override
//	public void handle(ControllerStateInterface unit, Timer timer) {
//		if (getSegmentController().isOnServer()) {
//			if(isUsingPowerReactors()){
//				if (debug) {
//					System.err.println("NEW POWER");
//				}
//				return;
//			}
//			if (!unit.isFlightControllerActive()) {
//				if (debug) {
//					System.err.println("NOT ACTIVE");
//				}
//				return;
//			}
//			if (System.currentTimeMillis() - effectModTime < 500) {
//				if (debug) {
//					System.err.println("NOT RELOAD");
//				}
//				return;
//			}
//			if (getCollectionManagers().isEmpty()) {
//				//can only cloak the ship at core
//				if (debug) {
//					System.err.println("NOT NOT SHIPCORE");
//				}
//				return;
//			}
//			try {
//				if (!convertDeligateControls(unit, controlledFromOrig, controlledFrom)) {
//					if (debug) {
//						System.err.println("NOT DEL COD");
//					}
//					return;
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//				return;
//			}
//			int unpowered = 0;
//			getPowerManager().sendNoPowerHitEffectIfNeeded();
//			for (int i = 0; i < getCollectionManagers().size(); i++) {
//				EC m = getCollectionManagers().get(i);
//				if (unit.isSelected(m.getControllerElement(), controlledFrom)) {
//					boolean controlling = controlledFromOrig.equals(controlledFrom);
//					controlling |= getControlElementMap().isControlling(controlledFromOrig, m.getControllerPos(), controllerId);
//
//					if (controlling && getSegmentController().isOnServer()) {
//						if (!isActiveEffect(m)) {
//							activateEffect(m);
//							effectModTime = (System.currentTimeMillis());
//						} else {
//							deactivateEffect(m);
//							effectModTime = (System.currentTimeMillis());
//						}
//					} else {
//						if (debug) {
//							System.err.println("NOT CONTROL");
//						}
//					}
//				}
//			}
//
//		} else {
//		}
//	}
//
//	public abstract String getCombiDescription();
//
//	public abstract OffensiveEffects getOffensiveEffect();
//
//	public void onHit(
//			SimpleTransformableSendableObject obj) {
//		if (obj.isClientOwnObject() && ((GameClientState) getState()).getWorldDrawer() != null) {
//			((GameClientState) getState()).getWorldDrawer().getGuiDrawer().notifyEffectHit(obj, getOffensiveEffect());
//		}
//	}
//
	public enum OffensiveEffects {
		EMP(null),
		EXPLOSIVE(null),
		ION(null),
		OVERDRIVE(null),
		PIERCING(null),
		PUNCHTHROUGH(null),
		PULL(BlockEffectTypes.PULL),
		PUSH(BlockEffectTypes.PUSH),
		STOP(BlockEffectTypes.STOP),
		REPAIR(null),
		SHIELD_SUPPLY(null),
		SHIELD_DRAIN(null),
		POWER_SUPPLY(null),
		POWER_DRAIN(null),
		NO_THRUST(null),
		NO_POWER(null),
		SHIELD_DOWN(null),

		THRUSTER_OUTAGE(BlockEffectTypes.THRUSTER_OUTAGE),
		NO_POWER_RECHARGE(BlockEffectTypes.NO_POWER_RECHARGE),
		NO_SHIELD_RECHARGE(BlockEffectTypes.NO_SHIELD_RECHARGE),;

		private final BlockEffectTypes effectType;

		private OffensiveEffects(BlockEffectTypes t) {
			effectType = t;
		}

		public BlockEffectTypes getEffect() {
			return effectType;
		}
	}

}
