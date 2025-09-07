package org.schema.game.common.controller.elements.power;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.ManagerUpdatableInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.*;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.util.List;
/**
 * Old power management addon from Power 1.x. Only preserved for backwards compatibility.
 */
@Deprecated
public class PowerAddOn implements ManagerUpdatableInterface {
	private final PowerManagerInterface powerManager;
	private final SegmentController segmentController;
	private double power;
	private double batteryPower;
	private double maxPower;
	private double recharge;
	private double powerRailed;
	private double maxPowerRailed;
	private double rechargeRailed;
	private long recovery;
	private double initialPower;
	private long disabledTime = -1;
	private long lastDiabledTime;
	private double powerBefore;
	private long lastSend;
	private double powerConsumedPerSecondTmp;
	private boolean rechargeEnabled = true;
	private float powerTimeAdd;
	private double powerConsumedPerSecond;
	private double over;
	private double batteryMaxPower;
	private double batteryRecharge;
	private double initialBatteryPower;
	private boolean batteryActive = true;
	private Vector3i selectedSlotBinding = new Vector3i();
	private int expectedPowerClient = -1;

	public PowerAddOn(PowerManagerInterface p, SegmentController s) {
		this.powerManager = p;
		this.segmentController = s;
		
		p.addUpdatable(this);
	}

	public double consumePower(double amount, Timer timer) {
		if (consumePowerInstantly(amount * timer.getDelta())) {
			return amount * timer.getDelta();
		}
		return 0;
	}

	public boolean canConsumePowerInstantly(float amount) {
		if (amount == 0) {
			return true;
		}
		if (!segmentController.railController.isDockedAndExecuted() && !segmentController.getDockingController().isDocked() && !sufficientPower(amount)) {
			return false;
		}
		if (segmentController.getDockingController().isDocked()) {
			SegmentController c = segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
			if (c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface) {
				PowerManagerInterface managerContainer = (PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				double powerBefore = (power + over);

				if (amount <= (power + over)) {
					//					power = Math.max( 0, power - amount );
				} else {
					//not enough power in here.
					//consume on mothership
					return managerContainer.getPowerAddOn().canConsumePowerInstantly(amount);
				}

			} else {
				//is docked on structure, that is not a power maanager
				//				power = Math.max( 0, power - amount );
			}
		} else if (segmentController.railController.isDockedAndExecuted()) {
			return sufficientPowerRail(amount);
		}

		return true;
	}

	public boolean consumePowerInstantly(double amount) {
		return consumePowerInstantly(amount, false);
	}

	public boolean consumePowerInstantly(double amount, boolean forced) {
		double rAmount = amount;
		if (over > 0) {
			double bonus = Math.min(amount, over);
			;
			amount -= bonus;
			over -= bonus;
		}
		if (!forced && !segmentController.railController.isDockedAndExecuted() && !segmentController.getDockingController().isDocked() && !sufficientPowerRail(amount)) {
			if (recovery <= 0) {
				recovery = System.currentTimeMillis();
			}
			return false;
		}
		boolean consumedOk = true;
		double bef = (power);
		if (segmentController.getDockingController().isDocked()) {
			SegmentController c = segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
			if (c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface) {
				PowerManagerInterface managerContainer = (PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				double powerBefore = (power);

				if (amount <= power) {
					if (over > amount) {
						over -= amount;
					} else {
						power = Math.max(0, (power) - amount);
						powerRailed = Math.max(0, (powerRailed) - amount);
					}
				} else {
					//not enough power in here.
					//consume on mothership
					return managerContainer.getPowerAddOn().consumePowerInstantly(amount);
				}

			} else {
				//is docked on structure, that is not a power maanager
				power = Math.max(0, (power) - amount);
				powerRailed = Math.max(0, (powerRailed) - amount);
			}
		} else if (segmentController.railController.isDockedAndExecuted()) {
			consumedOk = false;
			if (power >= amount) {
				power -= amount;
				powerRailed -= amount;
				consumedOk = true;
			} else {
				//all power must be consumed, and then go to the next in chain
				amount -= power;
				powerRailed = Math.max(0, (powerRailed) - power);
				power = 0;
				if (recovery <= 0) {
					recovery = System.currentTimeMillis();
				}
				SegmentController r = segmentController.railController.previous.rail.getSegmentController();
				if (r instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) r).getManagerContainer() instanceof PowerManagerInterface) {
					PowerManagerInterface pw = (PowerManagerInterface) ((ManagedSegmentController<?>) r).getManagerContainer();
					consumedOk = pw.getPowerAddOn().consumePowerInstantly(amount);

				}
			}
		} else {
			power = Math.max(0, (power) - amount);
			powerRailed = Math.max(0, (powerRailed) - amount);
		}

		if (power == 0 && bef > 0) {

			if (recovery <= 0) {
				recovery = System.currentTimeMillis();
			}
			if (segmentController.isOnServer()) {
				sendPowerUpdate();
			}
		}

		powerConsumedPerSecondTmp += rAmount;
		return consumedOk;
	}

	public void sendPowerUpdate() {
		assert (segmentController.isOnServer());
		PowerValueUpdate powerValueUpdate = new PowerValueUpdate();
		assert (powerValueUpdate.getType() == ValTypes.POWER);
		powerValueUpdate.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer());
		((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(powerValueUpdate, segmentController.isOnServer()));
	}
	public void sendPowerExpectedUpdate() {
		assert (segmentController.isOnServer());
		PowerExpectedValueUpdate powerValueUpdate = new PowerExpectedValueUpdate();
		assert (powerValueUpdate.getType() == ValTypes.POWER_EXPECTED);
		powerValueUpdate.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer());
		((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(powerValueUpdate, segmentController.isOnServer()));
	}
	public void sendBatteryPowerUpdate() {
		assert (segmentController.isOnServer());
		BatteryPowerValueUpdate powerValueUpdate = new BatteryPowerValueUpdate();
		assert (powerValueUpdate.getType() == ValTypes.POWER_BATTERY);
		powerValueUpdate.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer());
		((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(powerValueUpdate, segmentController.isOnServer()));
	}
	public void sendBatteryPowerExpectedUpdate() {
		assert (segmentController.isOnServer());
		BatteryPowerExpectedValueUpdate powerValueUpdate = new BatteryPowerExpectedValueUpdate();
		assert (powerValueUpdate.getType() == ValTypes.POWER_BATTERY_EXPECTED);
		powerValueUpdate.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer());
		((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(powerValueUpdate, segmentController.isOnServer()));
	}
	public void sendBatteryActiveUpdateClient() {
		assert (!segmentController.isOnServer());
		BatteryActiveValueClientToServerUpdate powerValueUpdate = new BatteryActiveValueClientToServerUpdate();
		assert (powerValueUpdate.getType() == ValTypes.POWER_BATTERY_ACTIVE);
		powerValueUpdate.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer());
		((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(powerValueUpdate, segmentController.isOnServer()));
	}

	public void sendRechargeEnabledUpdate() {
		assert (segmentController.isOnServer());
		PowerRechargeValueUpdate update = new PowerRechargeValueUpdate();
		assert (update.getType() == ValTypes.POWER_REGEN_ENABLED);
		update.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer());
		((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(update, segmentController.isOnServer()));
	}

	public SegmentController getSegmentController() {
		return segmentController;
	}

	public void disableTemporary(int milisecs) {
		disabledTime = System.currentTimeMillis() + milisecs;
		lastDiabledTime = System.currentTimeMillis() + milisecs;
	}

	public void fromTagStructure(Tag t) {
		Tag[] tag = (Tag[])t.getValue();
		
		initialPower = (Double) tag[0].getValue();
		initialBatteryPower = (Double) tag[1].getValue();

	}
	public void fromTagStructureOld(Tag tag) {
		initialPower = (Double) tag.getValue();
		
	}

	public int getBaseCapacity() {
		if (segmentController instanceof Planet || segmentController instanceof PlanetIco) {
			return VoidElementManager.POWER_FIXED_PLANET_BASE_CAPACITY;
		}
		if (segmentController instanceof FloatingRock) {
			return VoidElementManager.POWER_FIXED_ASTEROID_BASE_CAPACITY;
		}
		return VoidElementManager.POWER_FIXED_BASE_CAPACITY;
	}

	/**
	 * @return the initialPower
	 */
	public double getInitialPower() {
		return initialPower;
	}

	/**
	 * @param initialPower the initialPower to set
	 */
	public void setInitialPower(double initialPower) {
		this.initialPower = initialPower;
	}
	public void setInitialBatteryPower(double initialPower) {
		this.initialBatteryPower = initialPower;
	}

	/**
	 * @return the lastDiabledTime
	 */
	public long getLastDiabledTime() {
		return lastDiabledTime;
	}

	public double getMaxPowerWithoutDock() {
		return getBaseCapacity() + maxPower;
	}

	public double getMaxPower() {
		if ( segmentController.railController.isDockedAndExecuted()) {
			return maxPowerRailed;
		}
		double mPower;
		if (segmentController.getDockingController().isDocked()) {
			SegmentController c = segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
			if (c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface) {
				PowerManagerInterface managerContainer = (PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer();
				mPower = maxPower + managerContainer.getPowerCapacityManager().getMaxPower() + getBaseCapacity();
			} else {
				//is docked on structure, that is not a power manager
				mPower = maxPower;
			}
		} else {
			mPower = maxPower;
		}

		return getBaseCapacity() + mPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	public double getPower() {
		if (segmentController.railController.isDockedAndExecuted()) {
			return powerRailed;
		}
		return power;
	}

	public void setPower(double val) {
//		if(!isOnServer() && getSegmentController().toString().contains("PWTEST")){ 
//			try {
//				throw new Exception("PW: "+power+" -> "+val);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		this.power = val;
	}

	public double getPowerSimple() {
		return power;
	}

	public double getPowerRailed() {
		return powerRailed;
	}

	public double getRechargeForced() {
		return recharge * ((SendableSegmentController) segmentController).getBlockEffectManager().status.powerRegenPercent;
	}

	public double getRecharge() {
		if (!rechargeEnabled) {
			return 0.0;
		}
		return recharge * ((SendableSegmentController) segmentController).getBlockEffectManager().status.powerRegenPercent;
	}

	public void setRecharge(double recharge) {
		this.recharge = recharge;
	}

	public void incPower(double power) {
		double capacity = getBaseCapacity() + maxPower;
		this.power = Math.min(capacity, this.power + power);
	}

	private void recalcPowerRailedRoot() {
		recalcPowerRailedRoot(0, 0, 0);
	}

	private void recalcPowerRailedRoot(double pw, double ch, double mx) {
		if(segmentController.isVirtualBlueprint() && segmentController.railController.isDockedAndExecuted() && !segmentController.railController.previous.rail.getSegmentController().isVirtualBlueprint()){
			//reset on first ship that is a virtual blueprint
			mx = 0;
			ch = 0;
			pw = 0;
		}
		powerRailed = pw + power;
		rechargeRailed = ch + getRecharge();
		maxPowerRailed = mx + maxPower + getBaseCapacity();
		
		List<RailRelation> next = segmentController.railController.next;
		final int size = next.size();
		for (int i = 0; i < size; i++) {
			SegmentController sc = next.get(i).docked.getSegmentController();
			PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) sc).getManagerContainer()).getPowerAddOn();
			powerAddOn.recalcPowerRailedRoot(powerRailed, rechargeRailed, maxPowerRailed);
		}
	}

	public boolean isInRecovery() {
		return recovery >= 0;
	}

	private boolean sufficientPower(double amount) {
		return amount <= power + over;
	}

	private boolean sufficientPowerRail(double amount) {
		return amount <= powerRailed + over;
	}

	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.DOUBLE, null, power),
			new Tag(Type.DOUBLE, null, batteryPower),
			FinishTag.INST,
		});
				
				
	}
	private void handleInitialPower(double capacity){
		if (power >= getMaxPower() && initialPower > 0) {
			int bbPower;
			
			if(isOnServer()){
				if (segmentController instanceof Ship && ((Ship) segmentController).getSpawnedFrom() != null) {
					bbPower = ((Ship) segmentController).getSpawnedFrom().getElementMap().get(ElementKeyMap.POWER_CAP_ID);
				} else {
					bbPower = powerManager.getPowerCapacityManager().expected;
				}
			}else{
				bbPower = expectedPowerClient;
			}
			if (bbPower >= 0 && powerManager.getPowerCapacityManager().getTotalSize() >= bbPower) {
				System.err.println("[POWER] " + segmentController + " CHECKING against bb: " + bbPower + " / " + powerManager.getPowerCapacityManager().getTotalSize() + "; limit reached. setting cap to prevent overcharge");
				//all shield capacity blocks loaded
				//cap to preven overcharge from blueprints saved with different config
				initialPower = 0;

				power = getMaxPower();
				
				if(isOnServer()){
					sendPowerUpdate();
					sendPowerExpectedUpdate();
				}
			}
			return;
		}
		//add power from initial source

		double rest = capacity - power;
		if (rest > 0) {
			rest = Math.min(rest, initialPower);
			initialPower -= rest;
			power = power + rest;
		}
		if(segmentController.getSegmentBuffer().isFullyLoaded() && getPower() >= getMaxPower()){
			initialPower = 0;
		}
	}
	private void handleRecharge(Timer timer, double recharge, double capacity) {
		if (initialPower > 0) {
			handleInitialPower(capacity);
		} else {
			double over = ((power + timer.getDelta() * recharge) - capacity);
			if (over > 0) {
				this.over = over;
			}
			
			
			
			power = Math.min(capacity, power + timer.getDelta() * recharge);
			
			if(isBatteryActive() && batteryPower > 0){
				double transfer = Math.min(batteryPower, getPowerBatteryConsumedPerSecond()* timer.getDelta());
				if(VoidElementManager.POWER_BATTERY_TRANSFER_TOP_OFF_ONLY){
					transfer = Math.min(Math.max(0, capacity - power), transfer);
				}
				
				power = Math.min(capacity, power + transfer);
				batteryPower -= transfer;
			}
		}
	}
	public double getPowerBatteryConsumedPerSecond() {
		return VoidElementManager.POWER_BATTERY_TRANSFER_RATE_PER_SEC * batteryMaxPower ;
	}
	public int getExpectedBatterySize() {
		return powerManager.getPowerBatteryManager().expected;
	}
	public int getExpectedPowerSize() {
		return powerManager.getPowerManager().expected;
	}
	private void handleInitialBatteryPower(double capacity){
		if (batteryPower >= batteryMaxPower && initialBatteryPower > 0) {
			int bbPower;
			if(isOnServer()){
				if (segmentController instanceof Ship && ((Ship) segmentController).getSpawnedFrom() != null) {
					bbPower = ((Ship) segmentController).getSpawnedFrom().getElementMap().get(ElementKeyMap.POWER_BATTERY);
				} else {
					bbPower = powerManager.getPowerBatteryManager().expected;
				}
			}else{
				bbPower = expectedPowerClient;
			}
			if (bbPower >= 0 && powerManager.getPowerBatteryManager().getTotalSize() >= bbPower) {
				System.err.println("[POWERBATTERY] " + segmentController + " CHECKING against bb: " + bbPower + " / " + powerManager.getPowerCapacityManager().getTotalSize() + "; limit reached. setting cap to prevent overcharge");
				//all power battery blocks loaded
				//cap to preven overcharge from blueprints saved with different config
				initialBatteryPower = 0;

				batteryPower = batteryMaxPower;
				if(isOnServer()){
					sendBatteryPowerUpdate();
					sendBatteryPowerExpectedUpdate();
				}
			}
			return;
		}
		//add power from initial source

		double rest = capacity - batteryPower;
		
//		if(!isOnServer()){
//			System.err.println("INITIAL POWER "+initialBatteryPower+"; "+rest+"; "+batteryPower+"/"+capacity);
//		}
		if (rest > 0) {
			rest = Math.min(rest, initialBatteryPower);
			initialBatteryPower -= rest;
			batteryPower += rest;
			
//			if(!isOnServer()){
//				System.err.println("INITIALLL ::: "+rest+" : "+initialBatteryPower);
//			}
		}
		
	}
	public boolean isOnServer(){
		return segmentController.isOnServer();
	}
	private void handleBatteryRecharge(Timer timer, double recharge, double capacity) {
		
		
		if (initialBatteryPower > 0) {
			handleInitialBatteryPower(capacity);
		} else {
			double over = ((batteryPower + timer.getDelta() * recharge) - capacity);
			if (over > 0) {
			}
			batteryPower = Math.min(capacity, batteryPower + timer.getDelta() * recharge);
		}
	}
	@Override
	public void update(Timer timer) {
		boolean requestedInitalValues = ((ManagedSegmentController<?>) segmentController)
				.getManagerContainer().isRequestedInitalValuesIfNeeded();
		if(!requestedInitalValues){
			return;
		}
		this.over = 0;
		powerTimeAdd += timer.getDelta();
		if (powerTimeAdd >= 1) {
			powerConsumedPerSecond = powerConsumedPerSecondTmp;
			powerConsumedPerSecondTmp = 0;
			powerTimeAdd = 0;
		}
		if (System.currentTimeMillis() - recovery > VoidElementManager.POWER_RECOVERY_TIME) {
			recovery = -1;

		}

		if (segmentController.getHpController().isRebooting()) {
			power = 0;

			powerBefore = power;

		} else if (!isInRecovery()) {
			double capacity = getBaseCapacity() + maxPower;
			
			handleBatteryRecharge(timer, getBatteryRecharge(), batteryMaxPower);
			
			if (disabledTime > 0) {
				if (System.currentTimeMillis() < disabledTime) {
					//do not update
				} else {
					disabledTime = -1;
				}
			} else {
				handleRecharge(timer, getRecharge(), capacity);
			}

			if (segmentController.isOnServer() && initialPower <= 0) {
//				segmentController.getNetworkObject().initialPower.set((long) initialPower);
//				sendPowerUpdate();
			}
			if (segmentController.isOnServer() && initialBatteryPower <= 0) {
//				segmentController.getNetworkObject().initialBatteryPower.set((long) initialBatteryPower);
//				sendBatteryPowerUpdate();
			}
			long time = System.currentTimeMillis();
			boolean forceSend = time - lastSend > 25000;
			if (segmentController.isOnServer() && ((powerBefore != power) || forceSend)) {

				double percent = (capacity / 100);
				if (forceSend || Math.abs(powerBefore - power) >= percent * (double)ServerConfig.BROADCAST_SHIELD_PERCENTAGE.getInt()
						|| power == 0 || power == capacity) {
					//update NT when power changed, and change is more then 1% or when it's full or empty

					sendPowerUpdate();
					powerBefore = power;
					lastSend = time;
				}
			}

//			if(Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_PERIOD) && getSegmentController().toString().contains("Harv")){
//				System.err.println(getSegmentController().getState()+" "+getSegmentController()+" REPOWER::::::::::: ### "+recharge+" -> "+power+"; "+over);
//			}
		}
		if (!segmentController.railController.isDockedOrDirty()) {
			recalcPowerRailedRoot();
		}
	}

	

	public String getPowerString() {

		if (EngineSettings.G_SHOW_PURE_NUMBERS_FOR_SHIELD_AND_POWER.isOn()) {
			return "[" + StringTools.formatPointZero(getPower()) + " / " + StringTools.formatPointZero(getMaxPower()) + " # IN " + (initialPower) + " Power]";
		}

		return "[" + StringTools.formatPointZero((getPower() / getMaxPower()) * 100d) + "%" + (initialPower > 0 ? "(+)" : "") + " Power]";

	}

	public float getPercentOne() {
		return (float) (getPower() / getMaxPower());
	}
	public float getBatteryPercentOne() {
		return (float) (batteryPower / Math.max(0.00001d, batteryMaxPower));
	}
	public float getPercentOneOfLowestInChain() {
		if(segmentController.railController.isRoot()){
			return getPercentOne();
		}
		SegmentController r = segmentController.railController.previous.rail.getSegmentController();
		if (r instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) r).getManagerContainer() instanceof PowerManagerInterface) {
			PowerManagerInterface pw = (PowerManagerInterface) ((ManagedSegmentController<?>) r).getManagerContainer();
			return Math.max(getPercentOne(), pw.getPowerAddOn().getPercentOneOfLowestInChain());
		}
		return getPercentOne();
	}
	/**
	 * @return the powerConsumedPerSecond
	 */
	public double getPowerConsumedPerSecond() {
		return powerConsumedPerSecond;
	}

	public void sendNoPowerHitEffectIfNeeded() {
		if (powerRailed <= 0.5f && segmentController.isClientOwnObject()) {
			if (((GameClientState) segmentController.getState()).getWorldDrawer() != null) {
				((GameClientState) segmentController.getState()).getWorldDrawer().getGuiDrawer().notifyEffectHit(this.segmentController, OffensiveEffects.NO_POWER);
			}
		}
	}

	public boolean isRechargeEnabled() {
		return this.rechargeEnabled;
	}

	public void setRechargeEnabled(boolean enabled) {
		this.rechargeEnabled = enabled;
		if (segmentController.isOnServer()) {
			sendRechargeEnabledUpdate();
		}
	}

	public void setBatteryMaxPower(double maxPower) {
		batteryMaxPower = maxPower;
	}

	public void setBatteryRecharge(double recharge) {
		batteryRecharge = recharge;
	}
	public double getBatteryRechargeRaw(){
		return batteryRecharge;
	}
	public double getBatteryRecharge(){
		return isBatteryActive() ? getBatteryRechargeOn() : getBatteryRechargeOff();
	}
	public double getBatteryRechargeOn(){
		return batteryRecharge * VoidElementManager.POWER_BATTERY_TURNED_ON_MULT;
	}
	public double getBatteryRechargeOff(){
		return batteryRecharge * VoidElementManager.POWER_BATTERY_TURNED_OFF_MULT;
	}
	public double getBatteryMaxPower(){
		return batteryMaxPower;
	}
	public double getBatteryPower() {
		return batteryPower;
	}

	public void setBatteryPower(double val) {
		this.batteryPower = val;
	}

	public double getInitialBatteryPower() {
		return initialBatteryPower;
	}

	public boolean isBatteryActive() {
		return ((ManagedSegmentController<?>)segmentController).getManagerContainer().isPowerBatteryAlwaysOn() ? true : batteryActive;
	}
	public void setBatteryActive(boolean a) {
		System.err.println("[POWERADDON] "+ segmentController.getState()+"; "+ segmentController +" BATTERY ACTIVE: "+a);
		batteryActive = a;
	}

	

	public void setExpectedBatteryClient(int val) {
	}

	public void setExpectedPowerClient(int val) {
		expectedPowerClient = val;
	}

	@Override
	public int updatePrio() {
		return 100;
	}

	

	
	@Override
	public boolean canUpdate() {
		return true;
	}
	@Override
	public void onNoUpdate(Timer timer) {
	}
	

	
}
