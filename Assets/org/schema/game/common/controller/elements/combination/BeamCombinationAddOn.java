package org.schema.game.common.controller.elements.combination;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.beam.BeamCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamCommand;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

public abstract class BeamCombinationAddOn<
		E extends BeamUnit<E, CM, EM>,
		CM extends BeamCollectionManager<E, CM, EM>,
		EM extends BeamElementManager<E, CM, EM>>
		extends CombinationAddOn<E, CM, EM, BeamCombiSettings> {

	public BeamCombinationAddOn(EM elementManager, GameStateInterface inferface) {
		super(elementManager, inferface);
	}
	
	private final Vector3f dir = new Vector3f();
	private final Vector3f to = new Vector3f();
	private final Vector3f shooringDirNorm = new Vector3f();
	private final Vector3f relativePos = new Vector3f();
	private final BeamCommand b = new BeamCommand();
	public ShootingRespose handle(BeamUnitModifier mod,
	                              CM fireringCollection,
	                              E firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                              ShootContainer shootContainer, SimpleTransformableSendableObject aquiredTarget, PlayerState playerState, Timer timer,
	                              float beamTimeout) {

		float ratio = getRatio(fireringCollection, combi);

		mod.handle(firingUnit, combi, ratio);

		float hitSpeed = mod.outputTickRate;
		float damage = mod.outputDamagePerHit;
		float distance = mod.outputDistance; //((GameStateInterface)fireringCollection.getState()).getGameState().getSectorSize()
		float coolDown = mod.outputCoolDown;
		float burstTime = mod.outputBurstTime;
		float initialTicks = mod.outputInitialTicks;

//		int split = mod.outputSplit;

		float extraConsumption = 0;

		
		if(mod.outputAimable) {
			dir.set(shootContainer.shootingDirTemp);
		}else {
			dir.set(shootContainer.shootingDirStraightTemp);
		}
		ShootingRespose response = null;

		boolean lastShot = true;
//		if (i > 1) {
//			dir.set(shootingDir);
//			FastMath.normalizeCarmack(dir);
//			FastMath.normalizeCarmack(shootingUp);
//			FastMath.normalizeCarmack(shootingRight);
//
//			shootingUp.scale((r.nextFloat() - 0.5f) * 0.4f);
//			shootingRight.scale((r.nextFloat() - 0.5f) * 0.4f);
//			dir.add(shootingUp);
//			dir.add(shootingRight);
//		}

		
		to.set(shootContainer.weapontOutputWorldPos);
		shooringDirNorm.set(dir);
		FastMath.normalizeCarmack(shooringDirNorm);
		shooringDirNorm.scale(distance);
		to.add(shooringDirNorm);

		//			System.err.println(elementManager.getSegmentController().getState()+" [COMBI-BEAMFIRE]: dmg: "+damage+"; dir: "+dir);

		long significator = firingUnit.getSignificator();

		/*
		 * use trick to generate unique identifiers for beams,
		 * by adding the type bits to the identifyer depending on split index
		 */
		long codedSig = significator; //ElementCollection.getIndex4(significator, (short) i);
		relativePos.set(firingUnit.getOutput().x - SegmentData.SEG_HALF, firingUnit.getOutput().y - SegmentData.SEG_HALF, firingUnit.getOutput().z - SegmentData.SEG_HALF);

		b.currentTime = timer.currentTime;
		b.lastShot = lastShot;
		b.identifier = codedSig;
		b.relativePos.set(relativePos);
		b.reloadCallback = firingUnit;
		b.from.set(shootContainer.weapontOutputWorldPos);
		b.to.set(to);
		b.playerState = playerState;
		if (playerState != null && playerState.getControllerState().isDown(KeyboardMappings.WALK)) {
			b.dontFade = true;
		}
		b.tickRate = hitSpeed;
		b.controllerPos = fireringCollection.getControllerPos();
		b.beamPower = damage;
		
		b.hitType = firingUnit.getHitType();
		b.beamTimeout = burstTime > 0 ? burstTime : beamTimeout;
		b.cooldownSec = coolDown;
		b.bursttime = burstTime;
		b.initialTicks = initialTicks;
		b.powerConsumedByTick = mod.outputPowerConsumption;
		b.powerConsumedExtraByTick = extraConsumption;
		b.weaponId = fireringCollection.getUsableId();
		b.latchOn = mod.outputLatchMode;
		b.checkLatchConnection = mod.outputCheckLatchConnection;
		b.firendlyFire = mod.outputFriendlyFire;
		b.penetrating = mod.outputPenetration;
		b.acidDamagePercent = mod.outputAcidPercentage;
		
		b.minEffectiveRange = mod.outputMinEffectiveRange;
		b.minEffectiveValue = mod.outputMinEffectiveValue;
		b.maxEffectiveRange = mod.outputMaxEffectiveRange;
		b.maxEffectiveValue = mod.outputMaxEffectiveValue;

		b.capacityPerTick = mod.baseCapacityUsedPerTick + (int)(mod.additionalCapacityUsedPerDamage*mod.outputDamagePerHit);
		
		response = ((BeamHandlerContainer<?>) fireringCollection).getHandler().addBeam(b);

			//			response = ((BeamHandlerContainer<?>)fireringCollection).getHandler().addBeam(codedSig, relativePos, firingUnit, new Vector3f(weaponOutputPos), to,
			//					playerState, hitSpeed, damage, beamTimeout, coolDown, burstTime, initialTicks, powerConsumption, effectType, effectRatio, effectSize);
		return response;
	}

	protected abstract BeamUnitModifier getBeamCannonUnitModifier();

	protected abstract BeamUnitModifier getBeamMissileUnitModifier();

	protected abstract BeamUnitModifier getBeamBeamUnitModifier();

	@Override
	public ShootingRespose handleCannonCombi(CM fireringCollection, E firingUnit,
	                                         CannonCollectionManager combi,
	                                         ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                                         ShootContainer shootContainer,
	                                         SimpleTransformableSendableObject lockOntarget,
	                                         PlayerState playerState, Timer timer, float beamTimeout) {
		return handle(getBeamCannonUnitModifier(), fireringCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget, playerState, timer, beamTimeout);
	}

	@Override
	public ShootingRespose handleBeamCombi(CM fireringCollection, E firingUnit,
	                                       DamageBeamCollectionManager combi,
	                                       ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                                       ShootContainer shootContainer,
	                                       SimpleTransformableSendableObject lockOntarget,
	                                       PlayerState playerState, Timer timer, float beamTimeout) {
		return handle(getBeamBeamUnitModifier(), fireringCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget, playerState, timer, beamTimeout);
	}

	@Override
	public ShootingRespose handleMissileCombi(CM fireringCollection, E firingUnit,
	                                          DumbMissileCollectionManager combi,
	                                          ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                                          ShootContainer shootContainer,
	                                          SimpleTransformableSendableObject lockOntarget,
	                                          PlayerState playerState, Timer timer, float beamTimeout) {
		return handle(getBeamMissileUnitModifier(), fireringCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget, playerState, timer, beamTimeout);
	}


	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.combination.CombinationAddOn#getGUI(org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.weapon.WeaponCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public Modifier<E, BeamCombiSettings> getGUI(
			CM fireringCollection,
			E firingUnit,
			CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamCannonUnitModifier();
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.combination.CombinationAddOn#getGUI(org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.damageBeam.DamageBeamCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public Modifier<E, BeamCombiSettings> getGUI(
			CM fireringCollection,
			E firingUnit,
			DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamBeamUnitModifier();
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.combination.CombinationAddOn#getGUI(org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public Modifier<E, BeamCombiSettings> getGUI(
			CM fireringCollection,
			E firingUnit,
			DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamMissileUnitModifier();
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}


	@Override
	public double calcCannonCombiPowerConsumption(double powerPerBlock, CM fireingCollection, E firingUnit,
	                                              CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamCannonUnitModifier();
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcBeamCombiPowerConsumption(double powerPerBlock, CM fireingCollection, E firingUnit,
			DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamBeamUnitModifier();
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcMissileCombiPowerConsumption(double powerPerBlock, CM fireingCollection, E firingUnit,
			DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamMissileUnitModifier();
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}
	
	
	@Override
	public double calcCannonCombiReload(CM fireingCollection, E firingUnit,
	                                    CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamCannonUnitModifier();
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}
	
	@Override
	public double calcBeamCombiReload(CM fireingCollection, E firingUnit,
			DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamBeamUnitModifier();
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}
	
	@Override
	public double calcMissileCombiReload(CM fireingCollection, E firingUnit,
			DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		BeamUnitModifier mod = getBeamMissileUnitModifier();
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}
	
	
	
	@Override
	public void calcCannonCombiSettings(BeamCombiSettings out, CM fireingCollection,
	                                    CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		getBeamCannonUnitModifier().calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcBeamCombiSettings(BeamCombiSettings out, CM fireingCollection,
			DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		getBeamBeamUnitModifier().calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));		
	}

	@Override
	public void calcMissileCombiPowerSettings(BeamCombiSettings out, CM fireingCollection,
			DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		getBeamMissileUnitModifier().calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));		
	}

}
