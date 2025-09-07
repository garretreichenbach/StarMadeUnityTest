package org.schema.game.common.controller.elements.effectblock;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;
import org.schema.game.common.data.blockeffects.TakeOffEffect;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate.Type;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public class EffectAddOn extends RecharchableActivatableDurationSingleModule{

	private final BlockEffectTypes type;

	public EffectAddOn(ManagerContainer<?> man, BlockEffectTypes type) {
		super(man);
		this.type = type;
		assert(type != null);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		switch (getPowerConsumptionEffect(type)) {
			case THRUSTER_BLAST_POWER_CONSUMPTION_CHARGING:
				if(man instanceof ShipManagerContainer){
					return getConfigManager().apply(getPowerConsumptionEffect(type), ((ShipManagerContainer) man).getThrusterElementManager().getPowerConsumedPerSecondCharging());
				}
			default:
				return getConfigManager().apply(getPowerConsumptionEffect(type), 1d);
		}

	}

	@Override
	public int getMaxCharges() {
		return getMaxCharges(type);
	}
	public StatusEffectType getPowerConsumptionEffect(BlockEffectTypes type){
		return switch(type) {
			case TAKE_OFF -> StatusEffectType.THRUSTER_BLAST_POWER_CONSUMPTION_CHARGING;
			default -> StatusEffectType.THRUSTER_BLAST_POWER_CONSUMPTION_CHARGING;
		};
	}
	private int getMaxCharges(BlockEffectTypes type) {
		return switch(type) {
			case TAKE_OFF -> getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_BLAST_MULTI_CHARGE_COUNT, 1);
			default -> 1;
		};
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.OTHERS;
	}

	@Override
	public ConfigEntityManager getConfigManager(){
		return getSegmentController().getConfigManager();
	}
	@Override
	public boolean isPowerConsumerActive() {
		return getConfigManager().apply(type.getAssociatedStatusEffectType(), false);
	}
	@Override
	public long getUsableId() {
		return type.getUsableId();
	}

	@Override
	public String getTagId() {
		return "EF"+getUsableId();
	}

	@Override
	public int updatePrio() {
		return 1;
	}

	@Override
	public void sendChargeUpdate() {
		if(isOnServer()){
			EffectAddOnChargeValueUpdate v = new EffectAddOnChargeValueUpdate();
			v.setServer(
			((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), 
				getUsableId());
			assert(v.getType() == ValTypes.EFFECT_ADD_ON_CHARGE);
			((NTValueUpdateInterface) getSegmentController().getNetworkObject())
			.getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		}
	}
	@Override
	public boolean isDischargedOnHit() {
		return false;
	}
	
	
	
	@Override
	public void update(Timer timer) {
		super.update(timer);
		
		if(isActive()){
			startEffect();
			if(isOnServer() && type.oneTimeUse){
				//deactivate after usage if its one time usage
				deactivateManually();
			}
		}else{
			if(!type.oneTimeUse){
				//one time used effects deactivate themselves after use. no need to end it manually
				endEffect();
			}
		}
	}

	
	public void endEffect(){
		SendableSegmentController s = (SendableSegmentController)segmentController;
		if (s.getBlockEffectManager().hasEffect(type)) {
			s.getBlockEffectManager().getEffect(type).end();
		}
	}
	public void startEffect(){
		SendableSegmentController s = (SendableSegmentController)segmentController;
		if (!s.getBlockEffectManager().hasEffect(type)) {
			BlockEffectFactory<?> instance = this.type.effectFactory.getInstance();
			
			BlockEffect effect = instance.getInstanceFromNT(s);
			configureEffect(effect);
			s.getBlockEffectManager().addEffect(effect);
		}
	}
	private void configureEffect(BlockEffect effect) {
		switch(type) {
			case TAKE_OFF -> {
				TakeOffEffect e = (TakeOffEffect) effect;
				float speedFromBoost = getConfigManager().apply(StatusEffectType.THRUSTER_BLAST_STRENGTH, 1.0f);
				Vector3f n = ((ShipManagerContainer) man).getThrusterElementManager().getInputVectorNormalize(new Vector3f());
				float thrustMassRatio = ((ShipManagerContainer) man).getThrusterElementManager().getThrustMassRatio();
				if(n.lengthSquared() == 0) {
					//no dir. use forward
					//				GlUtil.getForwardVector(n, segmentController.getWorldTransform());
					GlUtil.getForwardVector(n, segmentController.getWorldTransform());
				}
				e.getDirection().set(n);
				System.err.println(getState() + " BLAST EFFECT CONF: thrustMassRatio " + thrustMassRatio + "; Mass " + segmentController.getMass() + "; strength: " + speedFromBoost + " -> " + (thrustMassRatio * segmentController.getMass() * speedFromBoost));
				if(thrustMassRatio <= 0.001) {
					if(isOnServer()) {
						getSegmentController().sendControllingPlayersServerMessage(Lng.astr("WARNING: Low thrust to do blast!"), ServerMessage.MESSAGE_TYPE_ERROR);
					}
				}
				e.setForce(thrustMassRatio * segmentController.getMass() * speedFromBoost);
				break;
			}
			default -> throw new RuntimeException("Effect not configured: " + effect.getType().name());
		}
	}

	@Override
	public void onChargedFullyNotAutocharged() {
		getSegmentController().popupOwnClientMessage(Lng.str("%s Charged!\nRight click to activate!", type.getName()), ServerMessage.MESSAGE_TYPE_INFO);
	}

	@Override
	public float getChargeRateFull() {
		return switch(type) {
			case TAKE_OFF -> getConfigManager().apply(StatusEffectType.THRUSTER_BLAST_COOLDOWN, 1f);
			default -> throw new RuntimeException("no mapped consumption: " + type.getName());
		};
		
	}

	@Override
	public boolean isAutoCharging() {
		return false;
	}

	@Override
	public boolean isAutoChargeToggable() {
		return false;
	}
	
	@Override
	public void chargingMessage() {
	}

	@Override
	public void onUnpowered() {
	}

	@Override
	public void onCooldown(long diff) {
		
	}

	@Override
	public boolean canExecute() {
		return true;
	}


	@Override
	public String getWeaponRowName() {
		return type.getName();
	}

	@Override
	public short getWeaponRowIcon() {
		return switch(type) {
			case TAKE_OFF -> ElementKeyMap.THRUSTER_ID;
			default -> ElementKeyMap.EFFECT_OVERDRIVE_COMPUTER;
		};
	}
	@Override
	public String getName() {
		return "EffectAddOn";
	}

	@Override
	protected Type getServerRequestType() {
		return Type.EFFECT;
	}

	@Override
	protected void onNoLongerConsumerActiveOrUsable(Timer timer) {
		endEffect();
	}


	@Override
	protected boolean isDeactivatableManually() {
		return true;
	}

	@Override
	public float getDuration() {
		return -1;
	}
	@Override
	public String getExecuteVerb() {
		return Lng.str("Activate Effect");
	}
}
