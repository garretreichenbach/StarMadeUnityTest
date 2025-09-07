package org.schema.game.common.controller.elements;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class RecharchableActivatableDurationSingleModule extends RecharchableSingleModule implements ManagerActivityInterface{

	public SingleModuleActivation activation;
	
	public RecharchableActivatableDurationSingleModule(ManagerContainer<?> man) {
		super(man);
	}
	//MODIFIED METHOD
	@Override
	public boolean executeModule() {

		if (this.getCharges() > 0) {
			if (this.getSegmentController().isOnServer()) {

				this.activation = new SingleModuleActivation();
				this.activation.startTime = System.currentTimeMillis();
				this.setCharge(0.0F);
				this.removeCharge();
				this.
				sendChargeUpdate();
				System.err.println("[SERVER][RECHARGE] ACTIVATE " + this.getWeaponRowName() + "; " + this.getSegmentController());
			}
				return true;
			} else {
				System.err.println("[SERVER][RECHARGE] NO CHRAGES FOR ACTIVATE " + this.getWeaponRowName() + "; " + this.getSegmentController());


		}
		return false;
	}
	////
	@Override
	public void dischargeFully() {
		super.dischargeFully();
		deactivateManually();
	}
	@Override
	protected void setActiveFromTag(boolean active) {
		if(active){
			this.activation = new SingleModuleActivation();
			this.activation.startTime = System.currentTimeMillis();
		}
	}
	private void resetActivation() {
		this.activation = null;
	}
	@Override
	public void deactivateManually() {
		resetActivation();
//		System.err.println(getSegmentController().getState()+" "+getName()+" "+getSegmentController()+" DEACTIVATED MANUALLY!");
		sendChargeUpdate();
	}
	@Override
	protected abstract boolean isDeactivatableManually();
	@Override
	public void update(Timer timer) {
		super.update(timer);
		
		if(isActive()){
//			if(!isOnServer() && this instanceof StealthAddOn) {
//				System.err.println(((GameClientState)getState()).getPlayerName()+" "+isActive()+" STEALTH DRIVE "+getSegmentController());
//			}
			float duration = getDuration();
			if(duration >= 0 && timer.currentTime - this.activation.startTime > (long)(duration * 1000f)){
				resetActivation();
				System.err.println(getSegmentController().getState()+" "+getName()+" "+getSegmentController()+" DEACTIVATED BY DURATION TIMEOUT!");
				sendChargeUpdate();
			}
		}
	}
	@Override
	public long getTimeLeftMs(){
		long timeLeftMS = -1;
		if(this.activation != null && getDuration() >= 0){
			long timeRunning = System.currentTimeMillis() - this.activation.startTime;
			long durMs = (long)(getDuration() * 1000f);
			long time = durMs - timeRunning;
			if( time > 0){
				timeLeftMS = time;
			}
		}
		return timeLeftMS;
	}
	
	public long getStarted() {
		return activation != null ? activation.startTime : 0L;
	}

	public void receivedActive(long started) {
		if(started <= 0){
			resetActivation();
		}else{
			if(activation == null){
				activation = new SingleModuleActivation();
			}
			if(activation.startTime != started){
				activation.startTime = started;
				System.err.println("[CLIENT][RECHARGE] "+((GameClientState)getState()).getPlayerName()+" ACTIVATE "+getWeaponRowName()+"; "+getSegmentController());
			}
		}
	}
	public abstract float getDuration();
	@Override
	public boolean isActive(){
		return this.activation != null;
	}
	@Override
	public boolean isAutoChargeOn(){
		return super.isAutoChargeOn() && !isActive();
	}
	
	@Override
	public ManagerActivityInterface getActivityInterface() {
		return this;
	}
}
