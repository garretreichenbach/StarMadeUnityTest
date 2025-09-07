package org.schema.game.common.controller.elements.shield.capacity;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldHitCallback;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ShieldCapacityCollectionManager extends ElementCollectionManager<ShieldCapacityUnit, ShieldCapacityCollectionManager, VoidElementManager<ShieldCapacityUnit, ShieldCapacityCollectionManager>> implements PowerConsumer{

	private float powered;
	private final Long2ObjectOpenHashMap<ShieldLocal> lastLocalShieldCache = new Long2ObjectOpenHashMap<ShieldLocal>();
	private long lastLocalShieldGet;

	public ShieldCapacityCollectionManager(SegmentController segController, VoidElementManager<ShieldCapacityUnit, ShieldCapacityCollectionManager> em) {
		super(ElementKeyMap.SHIELD_CAP_ID, segController, em);
	}

	private void updateCapabilities() {
		long shieldCapacityHP = 0;
		double shieldBlocks = 0;

		for (ShieldCapacityUnit c : getElementCollections()) {
			shieldCapacityHP += c.size() * VoidElementManager.SHIELD_EXTRA_CAPACITY_MULT_PER_UNIT;
			shieldBlocks += c.size();
		}
		shieldCapacityHP = (long) (Math.pow(shieldCapacityHP * VoidElementManager.SHIELD_CAPACITY_PRE_POW_MUL, VoidElementManager.SHIELD_CAPACITY_POW) * VoidElementManager.SHIELD_CAPACITY_TOTAL_MUL);

		//		double shields = Math.min(shields, shieldCapacityHP);
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getShieldAddOn();
		shieldAddOn.setShieldCapacityHP(shieldCapacityHP);
		shieldAddOn.setShields(Math.min(shieldAddOn.getShields(), shieldCapacityHP));

	}

	@Override
	public int getMargin() {
		return 0;
	}
	@Override
	public boolean isDetailedElementCollections() {
		//to mark draw for build mode - server for sensor
		return true; //!getSegmentController().isOnServer();
	}
	@Override
	protected Class<ShieldCapacityUnit> getType() {
		return ShieldCapacityUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public ShieldCapacityUnit getInstance() {
		return new ShieldCapacityUnit();
	}

	@Override
	protected void onChangedCollection() {
		updateCapabilities();
		if(getSegmentController().isUsingPowerReactors()){
			((ShieldContainerInterface)getContainer()).getShieldAddOn().getShieldLocalAddOn().flagCalcLocalShields();
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getShieldAddOn();
		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Capacity "), StringTools.formatPointZero(shieldAddOn.getShieldCapacity())),
				new ModuleValueEntry(Lng.str("Recharge "), StringTools.formatPointZero(shieldAddOn.getShieldRechargeRate())),
		};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Shield Capacity System");
	}
	@Override
	public void clear() {
		super.clear();
		lastLocalShieldCache.clear();
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()).getShieldAddOn();
		if(shieldAddOn.isUsingLocalShields()){
			ShieldLocal shieldLocal = lastLocalShieldCache.get(connected.getAbsoluteIndex());
			if(lastLocalShieldGet+(shieldLocal == null ? 500 : 5000) < getSegmentController().getState().getUpdateTime()){
				lastLocalShieldCache.put(connected.getAbsoluteIndex(), shieldAddOn.getShieldLocalAddOn().getContainingShield(((ShieldContainerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()), connected.getAbsoluteIndex()));
				lastLocalShieldGet = getSegmentController().getState().getUpdateTime();
			}
			ShieldLocal ll = lastLocalShieldCache.get(connected.getAbsoluteIndex());
			if(ll != null){
				return (float) Math.min(1f, ll.getShields() / Math.max(0.0001f, (ll.getShieldCapacity())));
			}else{
				return 0;
			}
		}else{
			return  (float) Math.min(1f, shieldAddOn.getShields() / Math.max(0.0001f, (shieldAddOn.getShieldCapacity())));
		}
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
	}
	public ShieldAddOn getShieldAddOn(){
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()).getShieldAddOn();;
		return shieldAddOn;
	}
	@Override
	public boolean isPowerCharging(long curTime) {
		return getShieldAddOn().getPercentOne() < 0.9999f;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		
	}
	
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SHIELDS;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}
	@Override
	public void dischargeFully() {
	}

	public void shieldHit(ShieldHitCallback hit) {
		if(getSegmentController().isOnServer()){
			checkIntegrityForced(hit.damager);
		}
	}
}
