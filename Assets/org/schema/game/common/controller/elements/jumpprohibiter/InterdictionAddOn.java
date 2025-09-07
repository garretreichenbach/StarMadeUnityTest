package org.schema.game.common.controller.elements.jumpprohibiter;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate.Type;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

import java.util.Collection;

@Deprecated
public class InterdictionAddOn extends RecharchableActivatableDurationSingleModule implements ConfigProviderSource {

	private static final long SHIELD_OUTAGE_TIMER = 15000L; //15 seconds
	private final Collection<ConfigGroup> configCollection = new ObjectArrayList<>();
	private boolean wasActive;
	private long firstUpdate;

	public InterdictionAddOn(ManagerContainer<?> man) {
		super(man);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		double powCons = getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_POWER_CONSUMPTION, getBasePowerConsumedPerSecond());
		if(isActive()) {
			return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_ACTIVE_RESTING_POWER_CONS, powCons);
		} else {
			return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_INACTIVE_RESTING_POWER_CONS, powCons);
		}
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_POWER_CONSUMPTION, getBasePowerConsumedPerSecond());
	}

	private double getBasePowerConsumedPerSecond() {
		int reactorLvl = getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_STRENGTH, 1);
		return PowerImplementation.getMinNeededFromReactorLevelRaw(reactorLvl) * VoidElementManager.REACTOR_RECHARGE_PERCENT_PER_SECOND * VoidElementManager.REACTOR_POWER_CAPACITY_MULTIPLIER;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.OTHERS;
	}

	@Override
	public ConfigEntityManager getConfigManager() {
		return getSegmentController().getConfigManager();
	}

	@Override
	public boolean isPowerConsumerActive() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION, false);
	}

	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_INTERDICTION;
	}

	@Override
	public boolean isPlayerUsable() {
		return super.isPlayerUsable() && getConfigManager().apply(StatusEffectType.WARP_INTERDICTION, false);
	}

	@Override
	public String getTagId() {
		return "INTR";
	}

	@Override
	public boolean executeModule() {
		System.err.println("[JUMPINTERDICTION] EXECUTING " + getSegmentController() + " " + getState());
		super.executeModule();
		return true;
	}

	@Override
	public int updatePrio() {
		return 1;
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(firstUpdate <= 0) {
			firstUpdate = timer.currentTime;
		}
		if(isActive()) {

			if(isOnServer() && (getPowered() < 1.0f || timer.currentTime - segmentController.lastAnyDamageTakenServer <= SHIELD_OUTAGE_TIMER) && getSegmentController().isFullyLoaded() && timer.currentTime - firstUpdate > 5000) {
				deactivateManually();
				if(getPowered() < 1.0f) getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Interdiction cancelled. Not enough power to keep up interdiction field. Stronger reactor needed."), ServerMessage.MESSAGE_TYPE_ERROR);
				else getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Interdiction cancelled due to shield outage!"), ServerMessage.MESSAGE_TYPE_ERROR);
			} else {
				getContainer().getPowerInterface().registerProjectionConfigurationSource(this);
			}
		} else {
			getContainer().getPowerInterface().unregisterProjectionConfigurationSource(this);
		}

		if(!wasActive && isActive()) {
			//acumulate the config to send to the sector
			configCollection.clear();
			getCurrentInterdictionConfigGroup(configCollection);
		}

		wasActive = isActive();
	}

	@Override
	public String getReloadStatus(long id) {
		if(isActive()) {
			return Lng.str("ACTIVE (Min Power Needed: %s)", StringTools.formatPointZero(getPowerConsumedPerSecondResting()));
		}
		return super.getReloadStatus(id);
	}

	@Override
	public void sendChargeUpdate() {
		if(isOnServer()) {
			InterdictionAddOnChargeValueUpdate v = new InterdictionAddOnChargeValueUpdate();
			v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), USABLE_ID_INTERDICTION);
			assert (v.getType() == ValTypes.INTERDICTION_CHARGE);
			((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		}
	}

	@Override
	public boolean isDischargedOnHit() {
		return false;
	}

	@Override
	protected void handle(ControllerStateInterface unit, Timer timer) {

		if(!isOnServer() && getPowered() < 1.0f) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Interdiction only possible if module is powered 100%.\nCharge your interdiction module."));
		} else {
			super.handle(unit, timer);
		}
	}

	@Override
	public void onChargedFullyNotAutocharged() {
		getSegmentController().popupOwnClientMessage(Lng.str("%s Charged!\nRight click to activate!", getName()), ServerMessage.MESSAGE_TYPE_INFO);
	}

	@Override
	public float getChargeRateFull() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_COOLDOWN, 1f);
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
		return getPowered() >= 1.0f;
	}

	@Override
	public String getWeaponRowName() {
		return getName();
	}

	@Override
	public short getWeaponRowIcon() {
		return ElementKeyMap.JUMP_INHIBITOR_COMPUTER;
	}

	@Override
	public String getName() {
		return Lng.str("Jump Inhibitor");
	}

	@Override
	protected Type getServerRequestType() {
		return Type.JUMP_INTERDICTION;
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
	protected void onNoLongerConsumerActiveOrUsable(Timer timer) {
		deactivateManually();
		getContainer().getPowerInterface().unregisterProjectionConfigurationSource(this);
	}

	@Override
	public ShortList getAppliedConfigGroups(ShortList out) {

		for(ConfigGroup e : configCollection) {
			out.add(e.ntId);
		}
		return out;
	}

	private void getCurrentInterdictionConfigGroup(Collection<ConfigGroup> out) {
		getContainer().getPowerInterface().getReactorSet().getAllReactorElementsWithConfig(getConfigManager().getConfigPool(), StatusEffectType.WARP_INTERDICTION_ACTIVE, out);
		System.err.println("[INTERDICTION_ADD_ON] " + getSegmentController().getState() + " " + getSegmentController() + " INTERDICT " + out);
	}

	@Override
	public long getSourceId() {
		return USABLE_ID_INTERDICTION;
	}

	@Override
	public String getExecuteVerb() {
		return Lng.str("Start interdiction");
	}
}
