package org.schema.game.common.controller.elements.combination;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Random;

import org.schema.common.config.ConfigParserException;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;
import org.schema.game.common.controller.elements.combination.modifier.MultiConfigModifier;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class CombinationAddOn<E extends ElementCollection<E, CM, EM>, CM extends ControlBlockElementCollectionManager<E, CM, EM>, EM extends UsableControllableElementManager<E, CM, EM>, S extends CombinationSettings> {

	protected final EM elementManager;
	Random r = new Random(123422342322423L);

	public CombinationAddOn(EM elementManager, GameStateInterface inferface) {
		this.elementManager = elementManager;
	}

	public static ControlBlockElementCollectionManager<?, ?, ?> getEffect(long slaveConnectedElement, ManagerModuleCollection<?, ?, ?> effectModuleCollection, SegmentController segmentController) {
		ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = null;

		if (effectModuleCollection != null) {
			ControlBlockElementCollectionManager<?, ?, ?> we = effectModuleCollection.getElementManager().getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(slaveConnectedElement));

			if (we != null) {
				effectCollectionManager = we;
//				if(segmentController.isClientOwnObject()){
//					((GameClientState)segmentController.getState()).getController().popupInfoTextMessage("Firing with effect:\n"+ElementKeyMap.toString(effectCollectionManager.getControllerElement().getType()), 0);
//				}
			}
		}
		return effectCollectionManager;
	}
	/**
	 * Will return the slave's ratio to the master, or 1 if the slave if bigger then the master
	 *
	 * @param master
	 * @param slave
	 * @return
	 */
	public static float getRatio(ControlBlockElementCollectionManager<?, ?, ?> master, ControlBlockElementCollectionManager<?, ?, ?> slave) {
		return getRatio(master, slave, true);
	}
	/**
	 * Will return the slave's ratio to the master,
	 *
	 * @param master
	 * @param slave
	 * @return
	 */
	public static float getRatio(ControlBlockElementCollectionManager<?, ?, ?> master, ControlBlockElementCollectionManager<?, ?, ?> slave, boolean capped) {
		if (slave == null || master == null) {
			System.err.println("WARNING: RATIO CANNOT BE CALCULATED: " + master + " " + slave+"; ");
			return 0;
		}
		final float sizeA = master.getTotalSize();
		final float sizeB = slave.getTotalSize();
		if (sizeA == 0 || sizeB == 0) {
			return 0;
		}
		if (!capped || sizeA > sizeB) {
			return sizeB / sizeA;
		} else {
			return 1;
		}
	}

	public static float getEffectSize(ControlBlockElementCollectionManager<?, ?, ?> col, ControlBlockElementCollectionManager<?, ?, ?> effect) {
		return Math.min(col.getTotalSize(), effect.getTotalSize());
	}

	public void parse(Node itemIn) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {

		Field[] fields = getClass().getDeclaredFields();

		NodeList childNodes = itemIn.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				for (Field f : fields) {

					f.setAccessible(true);
					ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
//					System.err.println("CHECKING: "+item.getNodeName()+" -> "+f.getName()+"; "+(annotation != null ? annotation.name() : "NoAnnotation(null)"));

					if (annotation != null) {
						Object o = f.get(this);
						if (annotation.name().equals(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
							if(o instanceof Modifier<?, ?>){
								((Modifier<?, ?>) o).load(item, 0);
							}else if(o instanceof MultiConfigModifier<?, ?, ?>){
								((MultiConfigModifier<?,?, ?>) o).load(item);
								((MultiConfigModifier<?,?, ?>) o).setInitialized(true, false);
							}
						}
					}
				}
			}

		}

		Annotation[] annotations = getClass().getAnnotations();
		for (Field f : fields) {
			f.setAccessible(true);
			ConfigurationElement annotation = f.getAnnotation(ConfigurationElement.class);
			if (annotation != null) {
				Object o = f.get(this);
				if ((o instanceof Modifier<?, ?> && !((Modifier<?, ?>) o).initialized) || (o instanceof MultiConfigModifier<?, ?, ?> && !((MultiConfigModifier<?, ?, ?>)o).checkInitialized())) {
					throw new ConfigParserException(o+" -> "+this.getClass().getSimpleName() + ": not parsed " + annotation.name() + " (field: " + f.getName() + ")");
				}
				if(o instanceof MultiConfigModifier<?, ?, ?>){
					//uninitilize in case for next parsing
					((MultiConfigModifier<?, ?, ?>) o).setInitialized(false, true);
				}else if(o instanceof Modifier<?, ?>){
					//uninitilize in case for next parsing
					((Modifier<?, ?>) o).initialized = false;
				}
				
			}
		}

	}

	protected abstract String getTag();

	public Modifier<E, S> getGUI(CM fireingCollection, E firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {

		if (combi instanceof CannonCollectionManager) {
			return getGUI(fireingCollection, firingUnit, (CannonCollectionManager) combi, effectCollectionManager);
		} else if (combi instanceof DamageBeamCollectionManager) {
			return getGUI(fireingCollection, firingUnit, (DamageBeamCollectionManager) combi, effectCollectionManager);
		} else if (combi instanceof DumbMissileCollectionManager) {
			return getGUI(fireingCollection, firingUnit, (DumbMissileCollectionManager) combi, effectCollectionManager);
		} else {
			return null;
		}
	}
	public double calculatePowerConsumptionCombi(double powerPerBlock, CM fireingCollection, E firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi, 
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		if (combi instanceof CannonCollectionManager) {
			return calcCannonCombiPowerConsumption(powerPerBlock, fireingCollection, firingUnit, (CannonCollectionManager) combi, effectCollectionManager);
		} else if (combi instanceof DamageBeamCollectionManager) {
			return calcBeamCombiPowerConsumption(powerPerBlock, fireingCollection, firingUnit, (DamageBeamCollectionManager) combi, effectCollectionManager);
		} else if (combi instanceof DumbMissileCollectionManager) {
			return calcMissileCombiPowerConsumption(powerPerBlock, fireingCollection, firingUnit, (DumbMissileCollectionManager) combi, effectCollectionManager);
		} 
		throw new RuntimeException("Invalid Combination "+fireingCollection+"; "+firingUnit+"; "+combi+"; "+effectCollectionManager);
	}
	public double calculateReloadCombi(CM fireingCollection, E firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi, 
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		if (combi instanceof CannonCollectionManager) {
			return calcCannonCombiReload(fireingCollection, firingUnit, (CannonCollectionManager) combi, effectCollectionManager);
		} else if (combi instanceof DamageBeamCollectionManager) {
			return calcBeamCombiReload(fireingCollection, firingUnit, (DamageBeamCollectionManager) combi, effectCollectionManager);
		} else if (combi instanceof DumbMissileCollectionManager) {
			return calcMissileCombiReload(fireingCollection, firingUnit, (DumbMissileCollectionManager) combi, effectCollectionManager);
		} 
		throw new RuntimeException("Invalid Combination "+fireingCollection+"; "+firingUnit+"; "+combi+"; "+effectCollectionManager);
	}
	public abstract double calcCannonCombiPowerConsumption(double powerPerBlock, CM fireingCollection, E firingUnit, CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	public abstract double calcBeamCombiPowerConsumption(double powerPerBlock, CM fireingCollection, E firingUnit, DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	public abstract double calcMissileCombiPowerConsumption(double powerPerBlock, CM fireingCollection, E firingUnit, DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	
	public abstract double calcCannonCombiReload(CM fireingCollection, E firingUnit, CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	public abstract double calcBeamCombiReload(CM fireingCollection, E firingUnit, DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	public abstract double calcMissileCombiReload(CM fireingCollection, E firingUnit, DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);

	
	public void calcCombiSettings(S out, CM fireingCollection, ControlBlockElementCollectionManager<?, ?, ?> combi, 
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
		if (combi instanceof CannonCollectionManager) {
			calcCannonCombiSettings(out, fireingCollection, (CannonCollectionManager) combi, effectCollectionManager);
			return;
		} else if (combi instanceof DamageBeamCollectionManager) {
			calcBeamCombiSettings(out, fireingCollection, (DamageBeamCollectionManager) combi, effectCollectionManager);
			return;
		} else if (combi instanceof DumbMissileCollectionManager) {
			calcMissileCombiPowerSettings(out, fireingCollection, (DumbMissileCollectionManager) combi, effectCollectionManager);
			return;
		} 
		throw new RuntimeException("Invalid Combination "+fireingCollection+"; "+combi+"; "+effectCollectionManager);
	}
	
	public abstract void calcCannonCombiSettings(S out, CM fireingCollection, CannonCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	public abstract void calcBeamCombiSettings(S out, CM fireingCollection, DamageBeamCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);
	public abstract void calcMissileCombiPowerSettings(S out, CM fireingCollection, DumbMissileCollectionManager combi,
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);

	public ShootingRespose handle(CM fireingCollection, E firingUnit, ControlBlockElementCollectionManager<?, ?, ?> combi, 
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager, ShootContainer shootContainer, SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout) {

		r.setSeed(23452355785681234L * firingUnit.size());

		
		
		if (firingUnit instanceof FiringUnit && !(firingUnit instanceof BeamUnit<?, ?, ?>) && !((FiringUnit<?, CM, EM>) firingUnit).canUse(firingUnit.getSegmentController().getState().getUpdateTime(), true)) {
			return ShootingRespose.RELOADING;
		}

		if (combi instanceof CannonCollectionManager) {
			return handleCannonCombi(fireingCollection, firingUnit, (CannonCollectionManager) combi, effectCollectionManager, shootContainer, lockOntarget, playerState, timer, beamTimeout);
		} else if (combi instanceof DamageBeamCollectionManager) {
			return handleBeamCombi(fireingCollection, firingUnit, (DamageBeamCollectionManager) combi, effectCollectionManager, shootContainer, lockOntarget, playerState, timer, beamTimeout);
		} else if (combi instanceof DumbMissileCollectionManager) {
			return handleMissileCombi(fireingCollection, firingUnit, (DumbMissileCollectionManager) combi, effectCollectionManager, shootContainer, lockOntarget, playerState, timer, beamTimeout);
		} else {

			if (fireingCollection.getSegmentController().isClientOwnObject()) {
				((GameClientState) fireingCollection.getSegmentController().getState()).getController().popupAlertTextMessage(Lng.str("This combination doesn't work."), 0);
			} else {
				System.err.println("[COMBI] NO CONNECTION " + fireingCollection + " -> " + combi);
			}
			return ShootingRespose.INVALID_COMBI;
		}
	}

	public abstract ShootingRespose handleCannonCombi(CM fireringCollection, E firingUnit, CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager, ShootContainer shootContainer, SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout);

	public abstract ShootingRespose handleBeamCombi(CM fireringCollection, E firingUnit, DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager, ShootContainer shootContainer, SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout);

	public abstract ShootingRespose handleMissileCombi(CM fireringCollection, E firingUnit, DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager, ShootContainer shootContainer, SimpleTransformableSendableObject lockOntarget, PlayerState playerState, Timer timer, float beamTimeout);


	public abstract Modifier<E, S> getGUI(CM fireringCollection, E firingUnit, CannonCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);

	public abstract Modifier<E, S> getGUI(CM fireringCollection, E firingUnit, DamageBeamCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);

	public abstract Modifier<E, S> getGUI(CM fireringCollection, E firingUnit, DumbMissileCollectionManager combi, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager);


	
}
