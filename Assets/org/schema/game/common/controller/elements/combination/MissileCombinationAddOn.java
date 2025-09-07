package org.schema.game.common.controller.elements.combination;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.combination.modifier.MissileUnitModifier;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;
import org.schema.game.common.controller.elements.combination.modifier.MultiConfigModifier;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileUnit;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public class MissileCombinationAddOn extends CombinationAddOn<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager, MissileCombiSettings> {

	@ConfigurationElement(name = "cannon")
	public static final MultiConfigModifier<MissileUnitModifier<DumbMissileUnit>, DumbMissileUnit, MissileCombiSettings> missileCannonUnitModifier = new MultiConfigModifier<MissileUnitModifier<DumbMissileUnit>, DumbMissileUnit, MissileCombiSettings>() {
		
		@Override
		public MissileUnitModifier<DumbMissileUnit> instance() {
			return new MissileUnitModifier();
		}
	};
	@ConfigurationElement(name = "beam")
	public static final MultiConfigModifier<MissileUnitModifier<DumbMissileUnit>, DumbMissileUnit, MissileCombiSettings> missileBeamUnitModifier = new MultiConfigModifier<MissileUnitModifier<DumbMissileUnit>, DumbMissileUnit, MissileCombiSettings>() {
		
		@Override
		public MissileUnitModifier<DumbMissileUnit> instance() {
			return new MissileUnitModifier();
		}
	};
	@ConfigurationElement(name = "missile")
	public static final MultiConfigModifier<MissileUnitModifier<DumbMissileUnit>, DumbMissileUnit, MissileCombiSettings> missileMissileUnitModifier = new MultiConfigModifier<MissileUnitModifier<DumbMissileUnit>, DumbMissileUnit, MissileCombiSettings>() {
		
		@Override
		public MissileUnitModifier<DumbMissileUnit> instance() {
			return new MissileUnitModifier();
		}
	};

	public MissileCombinationAddOn(DumbMissileElementManager elementManager, GameStateInterface gsinterface) {
		super(elementManager, gsinterface);

	}

	public ShootingRespose handle(MissileUnitModifier mod, DumbMissileCollectionManager fireringCollection,
	                              DumbMissileUnit firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
	                              ShootContainer shootContainer, SimpleTransformableSendableObject lockOntarget) {

		float ratio = getRatio(fireringCollection, combi);

		mod.handle(firingUnit, combi, ratio);

		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(shootContainer.weapontOutputWorldPos);

		final long weaponId = fireringCollection.getUsableId();
		
		long lightConnectedElement = fireringCollection.getLightConnectedElement();
		short lightType = 0;
		if (lightConnectedElement != Long.MIN_VALUE) {
			lightType = (short) ElementCollection.getType(lightConnectedElement);
		}

		float speed = mod.outputSpeed;

		final float distance = mod.outputDistance;

		final long reload = (long) mod.outputReload;

		final float damage = mod.outputDamage;

		firingUnit.setShotReloading(reload);

		if (fireringCollection.getSegmentController().isOnServer()) {

			for (int i = 0; i < mod.outputSplit; i++) {
				final Vector3f dir = new Vector3f(shootContainer.shootingDirTemp);

				if (i > 1) {
					dir.normalize();
					shootContainer.shootingUpTemp.normalize();
					shootContainer.shootingRightTemp.normalize();

					shootContainer.shootingUpTemp.scale((r.nextFloat() - 0.5f) * 0.5f);
					shootContainer.shootingRightTemp.scale((r.nextFloat() - 0.5f) * 0.5f);
					dir.add(shootContainer.shootingUpTemp);
					dir.add(shootContainer.shootingRightTemp);
				}
				dir.normalize();
				dir.scale(speed);

				if (mod.outputMode == MissileType.DUMB.configIndex) {
					elementManager.getMissileController().addDumbMissile(
							fireringCollection.getSegmentController(),
							transform,
							dir,
							speed,
							damage / mod.outputSplit,
							distance,
							weaponId,
							lightType);
				} else if (mod.outputMode == MissileType.HEAT.configIndex) {
					elementManager.getMissileController().addHeatMissile(
							fireringCollection.getSegmentController(),
							transform,
							dir,
							speed,
							damage / mod.outputSplit,
							distance,
							weaponId,
							lightType);
				} else if(mod.outputMode == MissileType.FAFO.configIndex){
					elementManager.getMissileController().addFafoMissile(
							fireringCollection.getSegmentController(),
							transform,
							dir,
							speed,
							damage / mod.outputSplit,
							distance,
							weaponId,
							lockOntarget,
							lightType);
				}else if(mod.outputMode == MissileType.BOMB.configIndex){
					float activation = DumbMissileElementManager.BOMB_ACTIVATION_TIME_SEC;
					Vector3f velo = new Vector3f();
					
					if(fireringCollection.getSegmentController().railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBodySegmentController) {
						RigidBodySegmentController r = (RigidBodySegmentController)fireringCollection.getSegmentController().railController.getRoot().getPhysicsDataContainer().getObject();
						
						r.getLinearVelocity(velo);
						if(velo.lengthSquared() > 0) {
							speed = velo.length();
							velo.normalize();
						}else {
							velo.set(0,0,1);
							speed = 0;
						}
					}else {
						velo.set(0,0,1);
						speed = 0;
					}
					int capacityUsed = 1;
					capacityUsed += (int)(mod.outputAdditionalCapacityUsedPerDamage/mod.outputDamage) *(int)mod.outputAdditionalCapacityUsedPerDamageMult;
					elementManager.getMissileController().addBombMissile(
							fireringCollection.getSegmentController(),
							transform,
							activation,
							velo,
							speed,

							damage / mod.outputSplit,
							distance,
							weaponId,
							capacityUsed,
							lightType);
					
					
					
				}else{
					throw new RuntimeException("Unknown missile type: "+mod.outputMode);
				}
			}
		}
		return ShootingRespose.FIRED;
	}

	@Override
	protected String getTag() {
		return "missile";
	}

	/**
	 * rapid launch seeker
	 */
	@Override
	public ShootingRespose handleCannonCombi(
			DumbMissileCollectionManager fireringCollection,
			DumbMissileUnit firingUnit, CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
			ShootContainer shootContainer, SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {

		return handle(missileCannonUnitModifier.get(combi), fireringCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget);
	}

	/**
	 * long range, tracking
	 */
	@Override
	public ShootingRespose handleBeamCombi(
			DumbMissileCollectionManager fireringCollection,
			DumbMissileUnit firingUnit, DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
			ShootContainer shootContainer,
			SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {
		return handle(missileBeamUnitModifier.get(combi), fireringCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget);
	}

	/**
	 * bomb
	 */
	@Override
	public ShootingRespose handleMissileCombi(
			DumbMissileCollectionManager fireringCollection,
			DumbMissileUnit firingUnit, DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager,
			ShootContainer shootContainer,
			SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {

		return handle(missileMissileUnitModifier.get(combi), fireringCollection, firingUnit, combi, effectCollectionManager, shootContainer, lockOntarget);
	}


	@Override
	public Modifier<DumbMissileUnit, MissileCombiSettings> getGUI(
			DumbMissileCollectionManager fireringCollection,
			DumbMissileUnit firingUnit,
			CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileCannonUnitModifier.get(combi);
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}

	@Override
	public Modifier<DumbMissileUnit, MissileCombiSettings> getGUI(
			DumbMissileCollectionManager fireringCollection,
			DumbMissileUnit firingUnit,
			DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileBeamUnitModifier.get(combi);
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}

	@Override
	public Modifier<DumbMissileUnit, MissileCombiSettings> getGUI(
			DumbMissileCollectionManager fireringCollection,
			DumbMissileUnit firingUnit,
			DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileMissileUnitModifier.get(combi);
		mod.handle(firingUnit, combi, getRatio(fireringCollection, combi));
		return mod;
	}


	@Override
	public double calcCannonCombiPowerConsumption(double powerPerBlock, DumbMissileCollectionManager fireingCollection,
			DumbMissileUnit firingUnit, CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileCannonUnitModifier.get(combi);
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcBeamCombiPowerConsumption(double powerPerBlock, DumbMissileCollectionManager fireingCollection,
			DumbMissileUnit firingUnit, DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileBeamUnitModifier.get(combi);
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public double calcMissileCombiPowerConsumption(double powerPerBlock, DumbMissileCollectionManager fireingCollection,
			DumbMissileUnit firingUnit, DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileMissileUnitModifier.get(combi);
		return mod.calculatePowerConsumption(powerPerBlock, firingUnit, combi, getRatio(fireingCollection, combi));
	}
	@Override
	public double calcCannonCombiReload(DumbMissileCollectionManager fireingCollection,
			DumbMissileUnit firingUnit, CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileCannonUnitModifier.get(combi);
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}
	
	@Override
	public double calcBeamCombiReload(DumbMissileCollectionManager fireingCollection,
			DumbMissileUnit firingUnit, DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileBeamUnitModifier.get(combi);
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}
	
	@Override
	public double calcMissileCombiReload(DumbMissileCollectionManager fireingCollection,
			DumbMissileUnit firingUnit, DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileMissileUnitModifier.get(combi);
		return mod.calculateReload(firingUnit, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcCannonCombiSettings(MissileCombiSettings out, DumbMissileCollectionManager fireingCollection,
	                                    CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileCannonUnitModifier.get(combi);
		mod.calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcBeamCombiSettings(MissileCombiSettings out, DumbMissileCollectionManager fireingCollection,
			DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileBeamUnitModifier.get(combi);
		mod.calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

	@Override
	public void calcMissileCombiPowerSettings(MissileCombiSettings out, DumbMissileCollectionManager fireingCollection,
			DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		MissileUnitModifier mod = missileMissileUnitModifier.get(combi);
		mod.calcCombiSettings(out, fireingCollection, combi, getRatio(fireingCollection, combi));
	}

	
}
