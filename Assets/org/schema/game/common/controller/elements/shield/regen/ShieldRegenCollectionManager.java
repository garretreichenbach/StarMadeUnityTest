package org.schema.game.common.controller.elements.shield.regen;

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
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ShieldRegenCollectionManager extends ElementCollectionManager<ShieldRegenUnit, ShieldRegenCollectionManager, VoidElementManager<ShieldRegenUnit, ShieldRegenCollectionManager>>{

	private long lastLocalShieldGet;
	private final Long2ObjectOpenHashMap<ShieldLocal> lastLocalShieldCache = new Long2ObjectOpenHashMap<ShieldLocal>();
	public ShieldRegenCollectionManager(SegmentController segController, VoidElementManager<ShieldRegenUnit, ShieldRegenCollectionManager> em) {
		super(ElementKeyMap.SHIELD_REGEN_ID, segController, em);
	}

	private void updateCapabilities() {
		long shieldRechargeRate = 0;
		double shieldBlocks = 0;

		for (ShieldRegenUnit c : getElementCollections()) {
			shieldRechargeRate += c.size() * VoidElementManager.SHIELD_EXTRA_RECHARGE_MULT_PER_UNIT;
			shieldBlocks += c.size();
		}

		shieldRechargeRate = (long) (Math.pow(shieldRechargeRate * VoidElementManager.SHIELD_RECHARGE_PRE_POW_MUL, VoidElementManager.SHIELD_RECHARGE_POW) * VoidElementManager.SHIELD_RECHARGE_TOTAL_MUL);

		//		double shields = Math.min(shields, shieldCapacityHP);
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getShieldAddOn();
		
		shieldAddOn.setShieldRechargeRate(shieldRechargeRate);
		shieldAddOn.setShields(Math.min(shieldAddOn.getShields(), shieldAddOn.getShieldCapacity()));

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
	protected Class<ShieldRegenUnit> getType() {
		return ShieldRegenUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public ShieldRegenUnit getInstance() {
		return new ShieldRegenUnit();
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
	public void clear() {
		super.clear();
		lastLocalShieldCache.clear();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Shield System");
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

	public ShieldAddOn getShieldAddOn(){
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()).getShieldAddOn();;
		return shieldAddOn;
	}
	public void shieldHit(ShieldHitCallback hit) {
		if(getSegmentController().isOnServer()){
			checkIntegrityForced(hit.damager);
		}
	}
	
}
