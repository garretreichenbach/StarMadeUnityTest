package org.schema.game.common.controller.elements.stealth;

import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.controller.elements.RevealingActionListener;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate.Type;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

@Deprecated
public class StealthAddOn extends RecharchableActivatableDurationSingleModule implements RevealingActionListener{
	public enum StealthLvl{ //TODO move elsewhere
		JAMMING(1),
		CLOAKING(2),
		;
		
		public final int bitmask;

		private StealthLvl(int b){
			this.bitmask = b;
		}
		
		public static boolean isOn(int mask, StealthLvl lvl){
			return (mask & lvl.bitmask) == lvl.bitmask;
		}
		public static int add(int mask, StealthLvl lvl){
			return mask | lvl.bitmask;
		}
		public static int remove(int mask, StealthLvl lvl){
			return mask & ~lvl.bitmask;
		}
	}
	
	public StealthAddOn(ManagerContainer<?> man){
		super(man);
	}

	@Override
	public void sendChargeUpdate() {
		if(isOnServer()){
			StealthAddOnChargeValueUpdate v = new StealthAddOnChargeValueUpdate();
			v.setServer(
			((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), USABLE_ID_STEALTH_REACTOR);
			assert(v.getType() == ValTypes.STEALTH_CHARGE_REACTOR);
			((NTValueUpdateInterface) getSegmentController().getNetworkObject())
			.getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		}
	}

	@Override
	public boolean isDischargedOnHit(){
		return true;
	}
	
	@Override
	public  void onChargedFullyNotAutocharged() {
		getSegmentController().popupOwnClientMessage(Lng.str("Stealth module Charged!\nRight click on icon to scan!"), ServerMessage.MESSAGE_TYPE_INFO);		
	}

	@Override
	public float getChargeRateFull() {
		float cNeeded = VoidElementManager.STEALTH_CHARGE_NEEDED;
		float r = getConfigManager().apply(StatusEffectType.STEALTH_CHARGE_TIME, cNeeded);
		return r;
	}
	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		float p = VoidElementManager.STEALTH_CONSUMPTION_RESTING + 
				getMassWithDocks() * VoidElementManager.STEALTH_CONSUMPTION_RESTING_ADDED_BY_MASS;
		float r = getConfigManager().apply(StatusEffectType.STEALTH_POWER_TOPOFF_RATE, p);
		double powCons = r;
		
		if(isActive()){
			boolean val = getConfigManager().apply(StatusEffectType.STEALTH_ACTIVE_RESTING_POWER_CONS, false);
			if(val){
				return getConfigManager().apply(StatusEffectType.STEALTH_ACTIVE_RESTING_POWER_CONS_MULT, getPowerConsumedPerSecondCharging());
			}
			return powCons;
		}else{
			boolean val = getConfigManager().apply(StatusEffectType.STEALTH_INACTIVE_RESTING_POWER_CONS, false);
			if(val){
				return getConfigManager().apply(StatusEffectType.STEALTH_INACTIVE_RESTING_POWER_CONS_MULT, getPowerConsumedPerSecondCharging());
			}
			return powCons;
		}
	}

	

	@Override
	public double getPowerConsumedPerSecondCharging() {
		float p = VoidElementManager.STEALTH_CONSUMPTION_CHARGING + 
				getMassWithDocks() * VoidElementManager.STEALTH_CONSUMPTION_CHARGING_ADDED_BY_MASS;
		float r = getConfigManager().apply(StatusEffectType.STEALTH_POWER_CHARGE_RATE, p);
		double powCons = r;
		return powCons;
	}

	

	@Override
	public boolean isAutoCharging(){
		return true;
	}

	@Override
	public boolean isAutoChargeToggable() {
		return true;
	}
	
	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_STEALTH_REACTOR;
	}

	@Override
	public void chargingMessage() {
		getSegmentController().popupOwnClientMessage(Lng.str("Steath drive not charged\nHold left mouse to charge!"), ServerMessage.MESSAGE_TYPE_INFO);		
	}
	
	@Override
	public void onCooldown(long diff) {
		getSegmentController().popupOwnClientMessage(Lng.str("Cannot use Stealth!\nStealth drive on Cooldown!\n(%d secs)", diff), ServerMessage.MESSAGE_TYPE_ERROR);		
	}

	@Override
	public void onUnpowered() {
		getSegmentController().popupOwnClientMessage(Lng.str("WARNING!\n \nStealth drive unpowered!"), ServerMessage.MESSAGE_TYPE_ERROR);		
	}

	@Override
	public String getTagId() {
		return "RSTLTH";
	}
	@Override
	public int updatePrio() {
		return 1;
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.STEALTH;
	}

	@Override
	public String getWeaponRowName() {
		return Lng.str("Stealth Drive");
	}

	@Override
	public short getWeaponRowIcon() {
		return ElementKeyMap.STEALTH_COMPUTER;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return getStealthStrength() > 0 ||
				getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, false) || 
				getConfigManager().apply(StatusEffectType.STEALTH_JAMMER_CAPABILITY, false);
	}

	@Override
	public float getDuration() {
		return getConfigManager().apply(StatusEffectType.STEALTH_USAGE_TIME, VoidElementManager.STEALTH_DURATION_BASIC);
	}

	public float getActiveStrength() {
		if(isActive()){
			return getStealthStrength();
		}else{
			return 0;
		}
	}
	public float getStealthStrength(){
		return getConfigManager().apply(StatusEffectType.STEALTH_STRENGTH, VoidElementManager.STEALTH_STRENGTH_BASIC)-1f;
	}
	public boolean hasStealth(StealthLvl lvl) {
		if(isActive()){
			if(lvl == StealthLvl.CLOAKING){
				return getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, false);
			}else if(lvl == StealthLvl.JAMMING){
				return getConfigManager().apply(StatusEffectType.STEALTH_JAMMER_CAPABILITY, false);
			}
		}
		return false;
	}
	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(isActive()){
			getSegmentController().popupOwnClientMessage("STEALTH: "+getActiveStrength()+": Jam: "+hasStealth(StealthLvl.JAMMING)+"; Cloak: "+hasStealth(StealthLvl.CLOAKING), ServerMessage.MESSAGE_TYPE_INFO);
		}
	}
	@Override
	public String getName() {
		return "StealthAddOn";
	}
	@Override
	protected Type getServerRequestType() {
		return Type.STEALTH;
	}

	@Override
	protected boolean isDeactivatableManually() {
		return true;
	}

	@Override
	protected void onNoLongerConsumerActiveOrUsable(Timer timer) {
		deactivateManually();
	}
	
	@Override
	public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState,
			PlayerControllable newAttached){
		if(isOnServer()){
			if(newAttached instanceof SegmentController && ((SegmentController)newAttached).railController.isInAnyRailRelationWith(originalCaller)){
				//still on the same entity. do not decloak
			}else{
				if(getSegmentController() instanceof PlayerControllable && ((PlayerControllable)getSegmentController()).getAttachedPlayers().isEmpty()){
					if(isActive() && getDuration() <= 0 && getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, false)){
						System.err.println("[SERVER] "+getSegmentController()+"; Stealth drive with permanent usage and cloak disabled by exiting ship");
						pState.sendServerMessagePlayerWarning(Lng.astr("Permanent cloak disengaged when exiting vessel"));
						deactivateManually();
					}
				}
			}
		}
	}


	@Override
	public void onRevealingAction() {
		if(isOnServer() && isActive() && getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, false)){
			//deactivate when stealth drive has cloaking ability
			deactivateManually();
		}
	}
	@Override
	public String getExecuteVerb() {
		return Lng.str("Activate Stealth Drive");
	}
}
