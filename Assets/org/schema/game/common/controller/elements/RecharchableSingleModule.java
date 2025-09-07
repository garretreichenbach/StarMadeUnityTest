package org.schema.game.common.controller.elements;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.client.view.gui.weapon.WeaponSegmentControllerUsableElement;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public abstract class RecharchableSingleModule extends SegmentControllerUsable implements PowerConsumer, ManagerReloadInterface, TagModuleUsableInterface, ManagerUpdatableInterface {
	private static final long RELOAD_AFTER_USE_MS = 400;
	private float initialCharge;
	private long lastSentZero;
	private short lastCharge;
	
	private float charge;
	
	
	private float powered;
	private long lastUse;
	private short chargedOnCycle;
	private boolean hasChargedACycle;
	private boolean autoChargeOn;
	private boolean checkedForInitialMetaData;
	private int charges;
	private boolean updating;
	protected GUITextOverlay chargesText;
	@Override
	public int getMaxCharges(){
		return 1;
	}
	@Override
	public void dischargeFully() {
		charge = 0;
		charges = 0;
	}
	public RecharchableSingleModule(ManagerContainer<?> man){
		super(man);
		man.addRechargeSingleModule(this);
		
	}
	public float getMassWithDocks() {
		return getSegmentController().getMassWithDocks();
	}
	
	@Override
	public boolean canUpdate(){
		return isPowerConsumerActive() && isPlayerUsable();
	}

	@Override
	public void onNoUpdate(Timer timer) {
		if(updating){
			onNoLongerConsumerActiveOrUsable(timer);
			updating = false;
		}
	}

	/**
	 * used to switch off any lasting effect if the ship's properties have
	 * changed like this module being available or usable
	 */
	protected abstract void onNoLongerConsumerActiveOrUsable(Timer timer);

	
	@Override
	public void update(Timer timer){
		
		updating = true;
		
		boolean requestedInitalValues = ((ManagedSegmentController<?>)getSegmentController())
				.getManagerContainer().isRequestedInitalValuesIfNeeded();
		if(!requestedInitalValues){
			return;
		}
		if (getSegmentController().isOnServer() && !checkedForInitialMetaData) {
			BlockMetaDataDummy dummy = getContainer().getInitialBlockMetaData().remove(this.getUsableId());
			if (dummy != null) {
				applyMetaData(dummy);
			}
		}
		checkedForInitialMetaData = true;
		
		if (getSegmentController().isOnServer() && initialCharge > 0) {
			charge = initialCharge;
			initialCharge = 0;
			sendChargeUpdate();
		}
		
		if(isCharged() && !isAllChargesCharged()){
			//happens one time only
			addCharge();
			if(!isAllChargesCharged()){
				charge = 0;
			}
			sendChargeUpdate();
		}
	}
	protected abstract org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate.Type getServerRequestType();

	private void applyMetaData(BlockMetaDataDummy dummy) {
		initialCharge = ((ChargeMetaDummy)dummy).charge;
	}
	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new ChargeMetaDummy(this);
	}
	public abstract void sendChargeUpdate();

	public void onHit(double damage, int damageType) {
		if (getSegmentController().isOnServer() && isDischargedOnHit()) {
			if (charge > 0 && System.currentTimeMillis() - lastSentZero > 5000) {
				charge = 0;
				sendChargeUpdate();
				lastSentZero = System.currentTimeMillis();
			}
		}
	}
	public abstract boolean isDischargedOnHit();
	public float getChargeAddedPerSec() {
		return 1f/getChargeRateFull();
	}
	public abstract boolean executeModule();
	
	public void charge(float timeSec, boolean powerCharging, float poweredResting, float powered) {
		if (!isCharged() && lastCharge != getState().getNumberOfUpdate()) {
			float chargeBefore = charge;
			
			if(poweredResting < VoidElementManager.REACTOR_MODULE_DISCHARGE_MARGIN){
				int chargesBef = charges;
				discharge((VoidElementManager.REACTOR_MODULE_DISCHARGE_MARGIN-poweredResting)*getChargeAddedPerSec());
				if(charge == 0 && chargesBef > 0){
					charges = chargesBef - 1;
					charge = 1;
					if(isOnServer()){
						sendChargeUpdate();
					}
				}
			}else{
				float chargeAdded = powered*getChargeAddedPerSec()*timeSec;
				charge = Math.min(1f, charge + chargeAdded);
				
				if (isCharged()) {
					if(!isAutoCharging()){
						onChargedFullyNotAutocharged();
					}
					if (chargeBefore < charge) {
						//send one when full
						sendChargeUpdate();
					}
				}
			}
			lastCharge = getState().getNumberOfUpdate();
		}
		
	}
	
	public void removeCharge(){
		charges = (Math.max(0, charges -1));
	}
	public void addCharge(){
		charges = Math.min(getMaxCharges(), charges +1);
	}
	public abstract void onChargedFullyNotAutocharged();


	public abstract float getChargeRateFull();
	public boolean isAllChargesCharged() {
		return charges >= getMaxCharges();
	}
	public boolean hasCharges() {
		return charges > 0;
	}
	public boolean isCharged() {
		return charge >= 1f;
	}

	public float getCharge() {
		return charge;
	}
	public void setCharge(float charge) {
		this.charge = charge;
	}
	public void discharge(double changedUsed) {
		charge = (float) Math.max(0, charge - changedUsed);
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		boolean autoCharge = isAllChargesCharged() && (isAutoChargeOn() && !isAllChargesCharged());
		
		boolean chargedCycle = getState().getNumberOfUpdate() <= chargedOnCycle +3;
		
		boolean charging;
		if(hasChargedACycle){
			if(chargedCycle){
				charging = true;
			}else{
				hasChargedACycle = false;
				charging = false;
			}
		}else{
			charging = false;
		}
		boolean c = autoCharge || charging;
		return c;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return this.powered;
	}

	public ConfigEntityManager getConfigManager(){
		return getSegmentController().getConfigManager();
	}
	public abstract boolean isAutoCharging();
	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		
		boolean autoChargeCapable = isAutoCharging();
		if(!autoChargeCapable){
			this.autoChargeOn = false;
		}else if(!isAutoChargeToggable()){
			this.autoChargeOn = true;
		}
		
		if(this.isAutoChargeOn()  && !isCharged()){
			charge(tickTime, powerCharging, poweredResting, powered);
			hasChargedACycle = true;
			chargedOnCycle = getState().getNumberOfUpdate();
		}
	}


	@Override
	public boolean isControllerConnectedTo(long index, short type) {
		return type == ElementKeyMap.CORE_ID;
	}

	@Override
	public boolean isPlayerUsable() {
		return getSegmentController().hasActiveReactors() && isPowerConsumerActive();
	}

	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {

	}
	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
		if((unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) || unit.isDown(KeyboardMappings.SHIP_ZOOM)) && unit.isFlightControllerActive() ){
			handle(unit, timer);
		}
	}

	protected void handle(ControllerStateInterface unit, Timer timer) {
		long timeSinceLastUse = timer.currentTime - lastUse;
		boolean needsSend = false;
		if (timeSinceLastUse > RELOAD_AFTER_USE_MS) {
			
			if(isActive() && isDeactivatableManually() && ((unit.isTriggered(KeyboardMappings.SHIP_PRIMARY_FIRE) && !isAutoChargeToggable()) ||
					(unit.isTriggered(KeyboardMappings.SHIP_ZOOM) && isAutoChargeToggable()) )){
				if(!isOnServer() && unit.getPlayerState() != ((GameClientState)getState()).getPlayer() && this instanceof RecharchableActivatableDurationSingleModule) {
					//do not deactivate on clients, when we received the mouse click used to activate (since activation is managed by server
				}else {
					deactivateManually();
				}
			}else{
				
				boolean autoChargeCapable = isAutoCharging();
				if(!isActive() && autoChargeCapable && (unit.isTriggered(KeyboardMappings.SHIP_PRIMARY_FIRE) || !isAutoChargeToggable())){
					this.autoChargeOn = !this.autoChargeOn;
					needsSend = true;
				}else if (!isCharged() && !isActive() && 
						(unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) ||
						unit.isTriggered(KeyboardMappings.SHIP_PRIMARY_FIRE))) {
					if(autoChargeCapable && (unit.isTriggered(KeyboardMappings.SHIP_PRIMARY_FIRE) || !isAutoChargeToggable())){
						this.autoChargeOn = !this.autoChargeOn;
						needsSend = true;
					}else if(!autoChargeCapable){
						if(this.autoChargeOn){
							this.autoChargeOn = false;
							needsSend = true;
						}
					}
					
					if(!isAutoChargeToggable() && !this.isAutoChargeOn()){
						chargingMessage();
						float restingPower = 1; //no need to discharge on manual charge
						charge(timer.getDelta(), !isCharged(), restingPower, powered);
						hasChargedACycle = true;
						chargedOnCycle = getState().getNumberOfUpdate();
					}
				} 
				
				if (hasCharges() && (unit.isTriggered(KeyboardMappings.SHIP_ZOOM) ||
						(unit.isTriggered(KeyboardMappings.SHIP_PRIMARY_FIRE) && !isAutoChargeToggable() && autoChargeOn))) {
					//left click allowed if you cannot turn off autocharge but it is autocharging
					if(canExecute()){
						executeModule();
						lastUse = timer.currentTime;
					}else{
						System.err.println(getState()+"[RECHARGESINFLEMODULE] CANNOT EXECUTE: "+this);
					}
					
				}
			}
		} else {
			if (timeSinceLastUse > 500) {
				long diff = (RELOAD_AFTER_USE_MS - timeSinceLastUse) / 1000L;
				onCooldown(diff);
			}
		}
		
		if (powered <= 0.0000001f && !isOnServer()) {
			onUnpowered();
		}                  
		if(needsSend){
			sendChargeUpdate();
			if(!isOnServer()){
				requestValueOnClient();
			}
		}
		}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		String st = this.autoChargeOn ? Lng.str("off") : Lng.str("on");
		boolean autoChargeCapable = isAutoCharging();
		if(isActive() && isDeactivatableManually() && !isAutoChargeToggable()){
			h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Deactivate"), hos, ContextFilter.IMPORTANT);
		}else if(!isActive() && autoChargeCapable && isAutoChargeToggable()){

			h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Turn Auto Charge %s", st), hos, ContextFilter.IMPORTANT);
		}else if (!isCharged() && !isActive()) {
			if(isAutoChargeToggable()) {
				h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Turn Auto Charge %s", st), hos, ContextFilter.IMPORTANT);
			}else {
				h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Hold to Charge", st), hos, ContextFilter.IMPORTANT);
			}

		}else if(isCharged() && !isActive()) {
			if(isAutoChargeToggable()) {
				h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Turn Auto Charge %s", st), hos, ContextFilter.IMPORTANT);
			}else {

			}

			if(hasCharges()) {
				if (!isAutoChargeToggable() && autoChargeOn){
					//forced charge
					h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, getExecuteVerb(), hos, ContextFilter.IMPORTANT);
				}else {
					h.addHelper(KeyboardMappings.SHIP_ZOOM, getExecuteVerb(), hos, ContextFilter.IMPORTANT);
				}
			}
		}
	}
	public abstract String getExecuteVerb();
	private void requestValueOnClient() {
		ServerValueRequestUpdate v = new ServerValueRequestUpdate(getServerRequestType());
		assert (v.getType() == ValTypes.SERVER_UPDATE_REQUEST);
		v.setServer(this.getContainer());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		
	}
	protected void deactivateManually() {
	}

	protected boolean isDeactivatableManually() {
		return false;
	}

	public boolean isActive() {
		//only used by RecharchableActivatableDurationSingleModule
		return false;
	}

	public abstract void chargingMessage();
	public abstract void onUnpowered();
	public abstract void onCooldown(long diff);
	public abstract boolean canExecute();
	public abstract boolean isAutoChargeToggable();
	public boolean isOnServer() {
		return segmentController.isOnServer();
	}

	@Override
	public ManagerReloadInterface getReloadInterface() {
		return this;
	}

	@Override
	public ManagerActivityInterface getActivityInterface() {
		return null;
	}
	public long getTimeLeftMs(){
		return -1;
	}
	
	@Override
	public String getReloadStatus(long id) {
		if(getTimeLeftMs() > -1){
			return Lng.str("ACTIVE %s sec",StringTools.formatPointZero(getTimeLeftMs()/1000d));
		}else if(isActive()){
			return Lng.str("ACTIVE");
		}
		return Lng.str("%s%% (%s/%s)", StringTools.formatPointZero(charge *100d), charges, getMaxCharges());
	}
	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		float percent = charge;
		if(chargesText == null){
			chargesText = new GUITextOverlay(FontSize.MEDIUM_15, (InputState) getState());
			chargesText.onInit();
		}
		long timeLeft = -1;
		boolean drawOneCharge = false;
		UsableControllableElementManager.drawReload(
				(InputState)getState(),
				iconPos, 
				iconSize, 
				UsableControllableFiringElementManager.reloadColor, 
				false, 
				percent,
				drawOneCharge, charges,
				getMaxCharges(),
				getTimeLeftMs(),
				chargesText);
	}
	@Override
	public WeaponRowElementInterface getWeaponRow() {
		WeaponRowElementInterface row = new WeaponSegmentControllerUsableElement(this);
		return row;
	}
	@Override
	public Tag toTagStructure() {
		Tag str = new Tag(Type.STRUCT, ((TagModuleUsableInterface)this).getTagId(), new Tag[]{
			new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.LONG, null, getUsableId()), 
					toTagStructurePriv(), 
					FinishTag.INST}),
			FinishTag.INST
		});
		return str;
	}

	public Tag toTagStructurePriv() {
		//encode charges by multi with 10
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.FLOAT, null, encodeCharge(charge, charges)),
				new Tag(Type.BYTE, null, autoChargeOn ? (byte)1 : (byte)0),
				new Tag(Type.BYTE, null, isActive()  ? (byte)1 : (byte)0),
				FinishTag.INST
			});
		
	}
	public void fromTagStructrePriv(Tag tag, int shift) {
		float c;
		if(tag.getType() == Type.FLOAT){
			c = tag.getFloat();
		}else{
			Tag[] s = tag.getStruct();
			c = s[0].getFloat();
			autoChargeOn = s[1].getBoolean();
			if(s[2].getType() == Type.BYTE){
				setActiveFromTag(s[2].getBoolean());
			}
		}
		
		this.charges = Math.min(getMaxCharges(), decodeCharges(c));
		this.charge = Math.min(1f, decodeCharge(c));
		
		
	}
	protected void setActiveFromTag(boolean boolean1) {
	}
	@Override
	public abstract String getWeaponRowName();
	@Override
	public abstract short getWeaponRowIcon();
	public boolean isAutoChargeOn() {
		//always autocharge when enemy AI controlled
		return autoChargeOn || (segmentController.isAIControlled() /*&& FactionManager.isNPCFactionOrPirateOrTrader(segmentController.getFactionId())*/);
	}
	public void setAutoChargeOn(boolean autoChargeOn) {
		this.autoChargeOn = autoChargeOn;
	}

	public static float encodeCharge(float charge, int charges){
		return charge+charges*10;
	}
	public static int decodeCharges(float in){
		return ((int)in)/10;
	}
	public static float decodeCharge(float in){
		return in - (decodeCharges(in)*10f);
	}
	@Override
	public int getCharges() {
		return charges;
	}

	public void setCharges(int c) {
		this.charges = c;
	}
}