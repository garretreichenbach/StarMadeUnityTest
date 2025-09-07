package org.schema.game.common.controller.elements.spacescanner;

import api.common.GameServer;
import api.listener.events.entity.EntityScanEvent;
import api.listener.events.register.RegisterAddonsEvent;
import api.mod.StarLoader;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.playermessage.ServerPlayerMessager;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

@Deprecated
public class ScanAddOn extends RecharchableActivatableDurationSingleModule{
	
	
	
	public static final long CARGO_SCAN_TIME = 15000;
	private int currentCargoScanning;
	private long currentCargoScanningStart;
	private int lastScanned;

	public ScanAddOn(ManagerContainer<?> man){
		super(man);
		//INSERTED CODE @42
		//TODO Move to proper place
		RegisterAddonsEvent event = new RegisterAddonsEvent(man);
		StarLoader.fireEvent(RegisterAddonsEvent.class, event, isOnServer());
		///

	}

	//INSERTED CODE @49
	@Override
	public boolean executeModule() {
		boolean success = super.executeModule();
		AbstractOwnerState ownerState = getSegmentController().getOwnerState();
		EntityScanEvent event = new EntityScanEvent(this, success, ownerState, getSegmentController());
		StarLoader.fireEvent(event, isOnServer());
		if(event.isCanceled()) return false;
		if(ownerState instanceof PlayerState) GameServer.getServerState().scanOnServer((PlayerState) ownerState, getDistance());
		return success;
	}
	///

	public int getDistance(){
		return (int) getConfigManager().apply(StatusEffectType.SCAN_LONG_RANGE_DISTANCE, LongRangeScannerElementManager.DEFAULT_SCAN_DISTANCE);
	}

	@Override
	public void sendChargeUpdate() {
		if(isOnServer()){
			ScanAddOnChargeValueUpdate v = new ScanAddOnChargeValueUpdate();
			v.setServer(
			((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), USABLE_ID_SPACE_SCAN);
			assert(v.getType() == ValueUpdate.ValTypes.SCAN_CHARGE_REACTOR);
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
		getSegmentController().popupOwnClientMessage(Lng.str("Scanner Charged!\nRight click on icon to scan!"), ServerMessage.MESSAGE_TYPE_INFO);		
	}

	@Override
	public float getChargeRateFull() {
		float cNeeded = VoidElementManager.SCAN_CHARGE_NEEDED;
		float r = getConfigManager().apply(StatusEffectType.SCAN_CHARGE_TIME, cNeeded);
		return r;
	}


	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		float p = VoidElementManager.SCAN_CONSUMPTION_RESTING + 
				getMassWithDocks() * VoidElementManager.SCAN_CONSUMPTION_RESTING_ADDED_BY_MASS;
		float r = getConfigManager().apply(StatusEffectType.SCAN_POWER_TOPOFF_RATE, p);
		double powCons = r;
		
		if(isActive()){
			
			boolean val = getConfigManager().apply(StatusEffectType.SCAN_ACTIVE_RESTING_POWER_CONS, false);
			if(val){
				return getConfigManager().apply(StatusEffectType.SCAN_ACTIVE_RESTING_POWER_CONS_MULT, getPowerConsumedPerSecondCharging());
			} else {
				return powCons;
			}
			
		}else{
			boolean val = getConfigManager().apply(StatusEffectType.SCAN_INACTIVE_RESTING_POWER_CONS, false);
			if(val){
				return getConfigManager().apply(StatusEffectType.SCAN_INACTIVE_RESTING_POWER_CONS_MULT, getPowerConsumedPerSecondCharging());
			} else {
				return powCons;
			}
			
		}
		
	}

	

	@Override
	public double getPowerConsumedPerSecondCharging() {
		float p = VoidElementManager.SCAN_CONSUMPTION_CHARGING + 
				getMassWithDocks() * VoidElementManager.SCAN_CONSUMPTION_CHARGING_ADDED_BY_MASS;
		float r = getConfigManager().apply(StatusEffectType.SCAN_POWER_CHARGE_RATE, p);
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
		return PlayerUsableInterface.USABLE_ID_SPACE_SCAN;
	}

	@Override
	public void chargingMessage() {
		getSegmentController().popupOwnClientMessage(Lng.str("Scanner not charged\nHold left mouse to charge!"), ServerMessage.MESSAGE_TYPE_INFO);		
	}
	
	@Override
	public void onCooldown(long diff) {
		getSegmentController().popupOwnClientMessage(Lng.str("Cannot Scan!\nScanner on Cooldown!\n(%d secs)", diff), ServerMessage.MESSAGE_TYPE_ERROR);		
	}

	@Override
	public void onUnpowered() {
		getSegmentController().popupOwnClientMessage(Lng.str("WARNING!\n \nScanner unpowered!"), ServerMessage.MESSAGE_TYPE_ERROR);		
	}

	@Override
	public String getTagId() {
		return "RSCN";
	}
	@Override
	public int updatePrio() {
		return 1;
	}
	
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SCANNER;
	}

	@Override
	public boolean isPlayerUsable() {
		
		if(!((GameStateInterface)getSegmentController().getState()).getGameState().isModuleEnabledByDefault(USABLE_ID_SPACE_SCAN) && !getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SCANNER_ENABLE, false)){
			return false;
		}
		
		return super.isPlayerUsable();
	}

	@Override
	public String getWeaponRowName() {
		return Lng.str("Scanner");
	}

	@Override
	public short getWeaponRowIcon() {
		return ElementKeyMap.SCANNER_COMPUTER;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public float getDuration() {
		return getConfigManager().apply(StatusEffectType.SCAN_USAGE_TIME, VoidElementManager.SCAN_DURATION_BASIC);
	}

	public float getActiveStrength() {
		if(isActive()){
			return getConfigManager().apply(StatusEffectType.SCAN_STRENGTH, VoidElementManager.SCAN_STRENGTH_BASIC);
		}else{
			return 0;
		}
	}
	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(isActive()){
			if(getConfigManager().apply(StatusEffectType.CARGO_SCANNER, false)){
				AbstractOwnerState ownerState = getSegmentController().getOwnerState();
				if(ownerState != null && ownerState instanceof PlayerState p){
					Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(p.getSelectedEntityId());
					if(sendable != null && sendable instanceof ManagedSegmentController<?>){
						ManagerContainer<?> man = ((ManagedSegmentController<?>)sendable).getManagerContainer();
						long diff = timer.currentTime - currentCargoScanningStart;
						if(currentCargoScanning != man.getSegmentController().getId()){
							currentCargoScanning = man.getSegmentController().getId();
							currentCargoScanningStart = timer.currentTime;
							lastScanned = 0;
						}else if(diff > CARGO_SCAN_TIME){
							if(lastScanned != man.getSegmentController().getId()){
								getSegmentController().popupOwnClientMessage("SCNIDCRGO", Lng.str("Scanning cargo of %s completed. Check your mail",man.getSegmentController()), ServerMessage.MESSAGE_TYPE_INFO);
								if(isOnServer()){
									ServerPlayerMessager m = ((GameServerState)getState()).getServerPlayerMessager();
									StringBuilder content = new StringBuilder();
									ElementCountMap cm = new ElementCountMap();
									for(Inventory inv : man.getInventories().values()){
										inv.addToCountMap(cm);
									}
									for(short type : ElementKeyMap.typeList()){
										if(ElementKeyMap.isValidType(cm.get(type))){
											content.append(String.format("%s: %d",ElementKeyMap.getInfo(type).getName() , cm.get(type)));
											content.append("\n");
										}
									}
									m.send(Lng.str("<system>"), p.getName(), Lng.str("Scan of %s", man.getSegmentController()), content.toString());
								}
								lastScanned = man.getSegmentController().getId();
							}
						}else{
							long left = CARGO_SCAN_TIME - diff;
							getSegmentController().popupOwnClientMessage("SCNIDCRGO", Lng.str("Scanning cargo of %s (%s left)",man.getSegmentController(), StringTools.formatTimeFromMS(left)), ServerMessage.MESSAGE_TYPE_INFO);
						}
					}
				}
			}
			getSegmentController().popupOwnClientMessage("SCNID", Lng.str("Scanning with strength: %s",getActiveStrength()), ServerMessage.MESSAGE_TYPE_INFO);
		}
	}

	@Override
	public String getName() {
		return "ScanAddOn";
	}
	@Override
	protected ServerValueRequestUpdate.Type getServerRequestType() {
		return ServerValueRequestUpdate.Type.SCAN;
	}
	@Override
	protected boolean isDeactivatableManually() {
		return true;
	}

	@Override
	protected void onNoLongerConsumerActiveOrUsable(Timer timer) {
		currentCargoScanning = 0;
	}
	@Override
	public String getExecuteVerb() {
		return Lng.str("Scan");
	}
}
