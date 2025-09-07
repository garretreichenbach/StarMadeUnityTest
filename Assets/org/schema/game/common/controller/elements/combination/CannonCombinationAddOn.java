package org.schema.game.common.controller.elements.combination;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.combination.modifier.MultiConfigModifier;
import org.schema.game.common.controller.elements.combination.modifier.CannonUnitModifier;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.controller.elements.cannon.CannonUnit;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public class CannonCombinationAddOn
		extends
		CombinationAddOn<CannonUnit, CannonCollectionManager, CannonElementManager, CannonCombiSettings> {

	@ConfigurationElement(name = "cannon")
	private static final MultiConfigModifier<CannonUnitModifier, CannonUnit, CannonCombiSettings> cannonCannonUnitModifier = new MultiConfigModifier<CannonUnitModifier, CannonUnit, CannonCombiSettings>() {

		@Override
		public CannonUnitModifier instance() {
			return new CannonUnitModifier();
		}
	};
	
	@ConfigurationElement(name = "beam")
	private static final MultiConfigModifier<CannonUnitModifier, CannonUnit, CannonCombiSettings> cannonBeamUnitModifier = new MultiConfigModifier<CannonUnitModifier, CannonUnit, CannonCombiSettings>() {

		@Override
		public CannonUnitModifier instance() {
			return new CannonUnitModifier();
		}
	};
	
	@ConfigurationElement(name = "missile")
	private static final MultiConfigModifier<CannonUnitModifier, CannonUnit, CannonCombiSettings> cannonMissileUnitModifier = new MultiConfigModifier<CannonUnitModifier, CannonUnit, CannonCombiSettings>() {

		@Override
		public CannonUnitModifier instance() {
			return new CannonUnitModifier();
		}
	};

	public CannonCombinationAddOn(CannonElementManager elementManager, GameStateInterface gsinterface) {
		super(elementManager, gsinterface);
	}

	public ShootingRespose handle(CannonUnitModifier mod, CannonCollectionManager fireingCollection,
	                              CannonUnit firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi,
	                              ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                              ShootContainer shootContainer,
	                              SimpleTransformableSendableObject lockOntarget) {

		mod.handle(firingUnit, combi, getRatio(fireingCollection, combi));

		final long weaponId = fireingCollection.getUsableId();
		firingUnit.setShotReloading((long) mod.outputReload);

		Vector3f dir = new Vector3f(shootContainer.shootingDirTemp);

		if(mod.outputAimable) {
			dir.set(shootContainer.shootingDirTemp);
		} else {
			dir.set(shootContainer.shootingDirStraightTemp);
		}

		dir.normalize();

		dir.scale(mod.outputSpeed);

		float capacityUsed = mod.baseCapacityUsedPerShot + (mod.additionalCapacityUsedPerDamage * mod.outputDamage);

		//System.err.println("State:" + fireingCollection.getSegmentController().getState());
		this.elementManager.getParticleController()
				.addProjectile(this.elementManager.getSegmentController(),
						shootContainer.weapontOutputWorldPos, dir,
						mod.outputDamage, mod.outputDistance,
						mod.outputAcidType, mod.outputProjectileWidth,
						firingUnit.getPenetrationDepth(mod.outputDamage),
						mod.outputImpactForce, weaponId, fireingCollection.getColor(), capacityUsed);

		fireingCollection.damageProduced += mod.outputDamage;

		fireingCollection.getElementManager().handleRecoil(fireingCollection, firingUnit, shootContainer.weapontOutputWorldPos, shootContainer.shootingDirTemp, mod.outputRecoil, mod.outputDamage);
		return ShootingRespose.FIRED;

	}

	@Override
	protected String getTag() {
		return "cannon";
	}

	/**
	 * machine gun
	 */
	@Override
	public ShootingRespose handleCannonCombi(
			CannonCollectionManager fireingCollection,
			CannonUnit firingUnit, CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
			ShootContainer shootContainer,
			SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {

		return handle(cannonCannonUnitModifier.get(combi), fireingCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget);
	}

	/**
	 * sniper
	 */
	@Override
	public ShootingRespose handleBeamCombi(CannonCollectionManager fireingCollection,
	                                       CannonUnit firingUnit, DamageBeamCollectionManager combi,
	                                       ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                                       ShootContainer shootContainer,
	                                       SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {

		return handle(cannonBeamUnitModifier.get(combi), fireingCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget);
	}

	/**
	 * shotgun
	 */
	@Override
	public ShootingRespose handleMissileCombi(CannonCollectionManager fireingCollection,
	                                          CannonUnit firingUnit, DumbMissileCollectionManager combi,
	                                          ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                                          ShootContainer shootContainer,
	                                          SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {

		return handle(cannonMissileUnitModifier.get(combi), fireingCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget);
	}

	@Override
	public CannonUnitModifier getGUI(
			CannonCollectionManager fireringCollection,
			CannonUnit firingUnit,
			CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonCannonUnitModifier.get(combi);
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}

	@Override
	public CannonUnitModifier getGUI(
			CannonCollectionManager fireingCollection,
			CannonUnit firingUnit,
			DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonBeamUnitModifier.get(combi);
		mod.handle(firingUnit, combi, getRatio(fireingCollection, combi));
		return mod;
	}

	@Override
	public CannonUnitModifier getGUI(
			CannonCollectionManager fireringCollection,
			CannonUnit firingUnit,
			DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonMissileUnitModifier.get(combi);
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}

	@Override
	public double calcCannonCombiPowerConsumption(double powerPerBlock, CannonCollectionManager fireingCollection, CannonUnit firingUnit,
	                                              CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonCannonUnitModifier.get(combi);
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcBeamCombiPowerConsumption(double powerPerBlock, CannonCollectionManager fireingCollection, CannonUnit firingUnit,
	                                            DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonBeamUnitModifier.get(combi);
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcMissileCombiPowerConsumption(double powerPerBlock, CannonCollectionManager fireingCollection, CannonUnit firingUnit,
	                                               DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonMissileUnitModifier.get(combi);
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcCannonCombiReload(CannonCollectionManager fireingCollection, CannonUnit firingUnit,
	                                    CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonCannonUnitModifier.get(combi);
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcBeamCombiReload(CannonCollectionManager fireingCollection, CannonUnit firingUnit,
	                                  DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonBeamUnitModifier.get(combi);
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcMissileCombiReload(CannonCollectionManager fireingCollection, CannonUnit firingUnit,
	                                     DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonMissileUnitModifier.get(combi);
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcCannonCombiSettings(CannonCombiSettings out, CannonCollectionManager fireingCollection,
	                                    CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonCannonUnitModifier.get(combi);
		mod.calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcBeamCombiSettings(CannonCombiSettings out, CannonCollectionManager fireingCollection,
	                                  DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonBeamUnitModifier.get(combi);
		mod.calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcMissileCombiPowerSettings(CannonCombiSettings out, CannonCollectionManager fireingCollection,
	                                          DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		CannonUnitModifier mod = cannonMissileUnitModifier.get(combi);
		mod.calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

}
