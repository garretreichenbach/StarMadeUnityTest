package org.schema.game.common.controller.elements.power.reactor;

import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate.Type;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public class ReactorBoostAddOn extends RecharchableActivatableDurationSingleModule{


	public ReactorBoostAddOn(ManagerContainer<?> man) {
		super(man);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
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
		return getConfigManager().apply(StatusEffectType.REACTOR_BOOST, false);
	}
	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_REACTOR_BOOST;
	}

	@Override
	public boolean isPlayerUsable() {
		return super.isPlayerUsable() && getConfigManager().apply(StatusEffectType.REACTOR_BOOST, false);
	}

	@Override
	public String getTagId() {
		return "RBST";
	}

	@Override
	public int updatePrio() {
		return 1;
	}
	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(isActive()){
			getContainer().getPowerInterface().setReactorBoost(getConfigManager().apply(StatusEffectType.REACTOR_BOOST_STRENGTH, 1f));
		}else{
			getContainer().getPowerInterface().setReactorBoost(0);	
		}
	}
	@Override
	public void sendChargeUpdate() {
		if(isOnServer()){
			ReactorBoostAddOnChargeValueUpdate v = new ReactorBoostAddOnChargeValueUpdate();
			v.setServer(
			((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), USABLE_ID_REACTOR_BOOST);
			assert(v.getType() == ValTypes.REACTOR_BOOST_CHARGE);
			((NTValueUpdateInterface) getSegmentController().getNetworkObject())
			.getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		}
	}
	@Override
	public boolean isDischargedOnHit() {
		return false;
	}
	@Override
	public void onChargedFullyNotAutocharged() {
		getSegmentController().popupOwnClientMessage(Lng.str("%s Charged!\nRight click to activate!", getName()), ServerMessage.MESSAGE_TYPE_INFO);
	}

	@Override
	public float getChargeRateFull() {
		return getConfigManager().apply(StatusEffectType.REACTOR_BOOST_COOLDOWN, 1f);
	}

	@Override
	public boolean isAutoCharging() {
		return true;
	}
		
	@Override
	public boolean isAutoChargeToggable() {
		return true;
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
		return getName();
	}

	@Override
	public short getWeaponRowIcon() {
		return ElementKeyMap.REACTOR_MAIN;
	}
	@Override
	public String getName() {
		return Lng.str("Reactor Boost");
	}

	@Override
	protected Type getServerRequestType() {
		return Type.REACTOR_BOOST;
	}

	@Override
	protected boolean isDeactivatableManually() {
		return true;
	}

	@Override
	public float getDuration() {
		return getConfigManager().apply(StatusEffectType.REACTOR_BOOST_DURATION, 1f);
	}

	@Override
	protected void onNoLongerConsumerActiveOrUsable(Timer timer) {
		deactivateManually();
		getContainer().getPowerInterface().setReactorBoost(0);
	}
	@Override
	public String getExecuteVerb() {
		return Lng.str("Start Reactor Boost");
	}
}
