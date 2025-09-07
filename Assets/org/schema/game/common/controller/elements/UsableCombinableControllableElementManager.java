package org.schema.game.common.controller.elements;

import java.util.List;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.combination.Combinable;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.CombinationSettings;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public abstract class UsableCombinableControllableElementManager<
				E extends FiringUnit<E, CM, EM>, 
				CM extends ControlBlockElementCollectionManager<E, CM, EM>, 
				EM extends UsableControllableFiringElementManager<E, CM, EM>, 
				S extends CombinationSettings> 
		extends UsableControllableFiringElementManager<E, CM, EM> implements Combinable<E, CM, EM, S>{
	public UsableCombinableControllableElementManager(short controller, short controlling,
			SegmentController segmentController) {
		super(controller, controlling, segmentController);
	}
	public abstract S getCombiSettings();
	public ShootingRespose handleAddOn(Combinable<E, CM, EM, S> combinable, CM m, E c, ManagerModuleCollection<?, ?, ?> managerModuleCollection, ManagerModuleCollection<?, ?, ?> effectModuleCollection, ShootContainer shootContainer, SimpleTransformableSendableObject aquiredTarget, PlayerState playerState, Timer timer, float beamTimeout) {
		
		if(managerModuleCollection == null ) {
			getSegmentController().popupOwnClientMessage("Invalid weapon slave connection", ServerMessage.MESSAGE_TYPE_ERROR);
			System.err.println("Exception: Invalid slave "+getSegmentController()+"; "+this);
			return ShootingRespose.NO_COMBINATION;
		}
		
		
		ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = null;
		
		
		
		try{
			effectCollectionManager = 
				CombinationAddOn.getEffect(m.getEffectConnectedElement(), effectModuleCollection, getSegmentController());
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Exception caught: no effectCollectionManager for "+this);
			return ShootingRespose.NO_COMBINATION;
		}
		List<? extends ControlBlockElementCollectionManager<?, ?, ?>> collectionManagers = managerModuleCollection.getElementManager().getCollectionManagers();

		ControlBlockElementCollectionManager<?, ?, ?> we = managerModuleCollection.getElementManager().getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElement()));
		if (we != null) {
			int slaveType = ElementCollection.getType(m.getSlaveConnectedElement());
			assert (!ElementKeyMap.getInfo((short) slaveType).isEffectCombinationController()) : ElementKeyMap.toString((short) slaveType);
			
			return combinable.getAddOn().handle(m, c, we, effectCollectionManager, shootContainer, aquiredTarget, playerState, timer, beamTimeout);
		}
		return ShootingRespose.NO_COMBINATION;
	}
	public double calculateReload( E u) {
		CM cm = u.elementCollectionManager;
		ManagerModuleCollection<?, ?, ?> managerModuleCollection = null;
		if (cm.getSlaveConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(cm.getSlaveConnectedElement());
			managerModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
		}
		return calculateReloadCombi(this, cm, u, managerModuleCollection);
	}
	public double calculatePowerConsumptionCombiCharging(double power, E u) {
		if(getAddOn() != null && u.consumptionCombiSignCharge != getManagerContainer().lastChangedElement){
			CM cm = u.elementCollectionManager;
			ManagerModuleCollection<?, ?, ?> effectModuleCollection = null;
			
			if (cm.getEffectConnectedElement() != Long.MIN_VALUE) {
				short connectedType = 0;
				String errorReason = "";
				connectedType = (short) ElementCollection.getType(cm.getEffectConnectedElement());
				effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
	
			}
			if (cm.getEffectConnectedElement() != Long.MIN_VALUE) {
				short connectedType = 0;
				String errorReason = "";
				connectedType = (short) ElementCollection.getType(cm.getEffectConnectedElement());
				effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
	
				ControlBlockElementCollectionManager<?, ?, ?> effect = CombinationAddOn.getEffect(cm.getEffectConnectedElement(), effectModuleCollection, getSegmentController());
				if (effect != null) {
					cm.setEffectTotal(effect.getTotalSize());
				}
			}
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = null;
			if (cm.getSlaveConnectedElement() != Long.MIN_VALUE) {
				short connectedType = 0;
				String errorReason = "";
				connectedType = (short) ElementCollection.getType(cm.getSlaveConnectedElement());
				managerModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
			}
			u.combiConsumptionCharge = calculatePowerConsumptionCombi(power, this, cm, u, managerModuleCollection, effectModuleCollection);
			u.consumptionCombiSignCharge = getManagerContainer().lastChangedElement;
		}
		return u.combiConsumptionCharge;
	}
	public double calculatePowerConsumptionCombiResting(double power, E u) {
		if(getAddOn() != null && u.consumptionCombiSignRest != getManagerContainer().lastChangedElement){
			CM cm = u.elementCollectionManager;
			ManagerModuleCollection<?, ?, ?> effectModuleCollection = null;
			
			if (cm.getEffectConnectedElement() != Long.MIN_VALUE) {
				short connectedType = 0;
				String errorReason = "";
				connectedType = (short) ElementCollection.getType(cm.getEffectConnectedElement());
				effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
				
			}
			if (cm.getEffectConnectedElement() != Long.MIN_VALUE) {
				short connectedType = 0;
				String errorReason = "";
				connectedType = (short) ElementCollection.getType(cm.getEffectConnectedElement());
				effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
				
				ControlBlockElementCollectionManager<?, ?, ?> effect = CombinationAddOn.getEffect(cm.getEffectConnectedElement(), effectModuleCollection, getSegmentController());
				if (effect != null) {
					cm.setEffectTotal(effect.getTotalSize());
				}
			}
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = null;
			if (cm.getSlaveConnectedElement() != Long.MIN_VALUE) {
				short connectedType = 0;
				String errorReason = "";
				connectedType = (short) ElementCollection.getType(cm.getSlaveConnectedElement());
				managerModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
			}
			u.combiConsumptionRest = calculatePowerConsumptionCombi(power, this, cm, u, managerModuleCollection, effectModuleCollection);
			u.consumptionCombiSignRest = getManagerContainer().lastChangedElement;
		}
		return u.combiConsumptionRest;
	}
	public double calculatePowerConsumptionCombi(double power, boolean charging, E u) {
		if(charging){
			return calculatePowerConsumptionCombiCharging(power, u);
		}else{
			return calculatePowerConsumptionCombiResting(power, u);
		}
	}
	public double calculateReloadCombi(Combinable<E, CM, EM, S> combinable, CM m, E c, ManagerModuleCollection<?, ?, ?> combined) {
		if(combined != null && combinable.getAddOn() != null){
			List<? extends ControlBlockElementCollectionManager<?, ?, ?>> collectionManagers = combined.getElementManager().getCollectionManagers();
			ControlBlockElementCollectionManager<?, ?, ?> we = combined.getElementManager().getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElement()));
			if (we != null) {
				assert (!ElementKeyMap.getInfo((short) ElementCollection.getType(m.getSlaveConnectedElement())).isEffectCombinationController()) : ElementKeyMap.toString((short) ElementCollection.getType(m.getSlaveConnectedElement()));
				
				return combinable.getAddOn().calculateReloadCombi(m, c, we, null);
			}
		}
		return c.getReloadTimeMs();
		
	}
	public double calculatePowerConsumptionCombi(double powerPerBlock, Combinable<E, CM, EM, S> combinable, CM m, E c, ManagerModuleCollection<?, ?, ?> combined, ManagerModuleCollection<?, ?, ?> effect) {
		ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = null;
		try{
			effectCollectionManager = 
					CombinationAddOn.getEffect(m.getEffectConnectedElement(), effect, getSegmentController());
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Exception caught: no effectCollectionManager for "+this);
			return 0;
		}
		if(combined != null){
			List<? extends ControlBlockElementCollectionManager<?, ?, ?>> collectionManagers = combined.getElementManager().getCollectionManagers();
			ControlBlockElementCollectionManager<?, ?, ?> we = combined.getElementManager().getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElement()));
			if (we != null) {
				assert (!ElementKeyMap.getInfo((short) ElementCollection.getType(m.getSlaveConnectedElement())).isEffectCombinationController()) : ElementKeyMap.toString((short) ElementCollection.getType(m.getSlaveConnectedElement()));
				
				return combinable.getAddOn().calculatePowerConsumptionCombi(powerPerBlock, m, c, we, effectCollectionManager);
			}
		}
		int effectSize = 0;
		if(effectCollectionManager != null){
			effectSize = effectCollectionManager.getTotalSize();
		}
		//consumption with tertiary effect only 
		return (c.size() + c.getEffectBonus()) * powerPerBlock * c.getExtraConsume();
	}
}
