package org.schema.game.common.controller.elements.powerbattery;

import java.util.List;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.client.view.gui.weapon.WeaponPowerBatteryRowElement;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.BlockKillInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ModuleExplosion;
import org.schema.game.common.controller.elements.ModuleExplosion.ExplosionCause;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class PowerBatteryCollectionManager extends ElementCollectionManager<PowerBatteryUnit, PowerBatteryCollectionManager, VoidElementManager<PowerBatteryUnit, PowerBatteryCollectionManager>> implements ManagerActivityInterface, BlockKillInterface, PlayerUsableInterface{

	private double maxPower;
	private double recharge;
	private long lastHit;

	public PowerBatteryCollectionManager(
			SegmentController segController, VoidElementManager<PowerBatteryUnit, PowerBatteryCollectionManager> em) {
		super(ElementKeyMap.POWER_BATTERY, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<PowerBatteryUnit> getType() {
		return PowerBatteryUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}
	public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
		if(!isOnServer() && e.pressedLeftMouse()){
			PowerAddOn powerAddOn = ((PowerManagerInterface)getElementManager().getManagerContainer()).getPowerAddOn();
			powerAddOn.setBatteryActive(!powerAddOn.isBatteryActive());
			powerAddOn.sendBatteryActiveUpdateClient();
		}
	}
	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
	}
	@Override
	public PowerBatteryUnit getInstance() {
		return new PowerBatteryUnit();
	}

	@Override
	protected void onChangedCollection() {
		//		System.err.println("ON ADD POWER OF "+absPos);
		refreshMaxPower();
	}
	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		if(((ManagedSegmentController<?>)getSegmentController()).getManagerContainer() instanceof StationaryManagerContainer<?>){
			return new GUIKeyValueEntry[]{
					new ModuleValueEntry(Lng.str("Total Recharge (turned OFF)"), StringTools.formatPointZero(recharge * VoidElementManager.POWER_BATTERY_TURNED_OFF_MULT)),
					new ModuleValueEntry(Lng.str("Total Recharge (turned ON)"), StringTools.formatPointZero(recharge * VoidElementManager.POWER_BATTERY_TURNED_ON_MULT)),
					new ModuleValueEntry(Lng.str("Total Capacity"), StringTools.formatPointZero(maxPower)),
					new ModuleValueEntry(Lng.str("WARNING"), Lng.str("Auxiliary power is volatile and will explode when destroyed."))
				};	
		}else{
		return new GUIKeyValueEntry[]{
			new ModuleValueEntry(Lng.str("Total Recharge (turned OFF)"), StringTools.formatPointZero(recharge * VoidElementManager.POWER_BATTERY_TURNED_OFF_MULT)),
			new ModuleValueEntry(Lng.str("Total Recharge (turned ON)"), StringTools.formatPointZero(recharge * VoidElementManager.POWER_BATTERY_TURNED_ON_MULT)),
			new ModuleValueEntry(Lng.str("Total Capacity"), StringTools.formatPointZero(maxPower)),
			new ModuleValueEntry(Lng.str("WARNING"), Lng.str("Auxiliary power is volatile and will explode when destroyed."))
		};
		}
	}

	@Override
	public String getModuleName() {
		return Lng.str("Auxiliary Power System");
	}

	/**
	 * @return the maxPower
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * @param maxPower the maxPower to set
	 */
	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	private void refreshMaxPower() {
		maxPower = 0d;
		recharge = 0d;
		int total = 0;
		for (PowerBatteryUnit p : getElementCollections()) {
			p.refreshPowerCapabilities();
			
			
			maxPower += p.getMaxPower();
			recharge += p.getRecharge();
			
			total += p.size();
			
			
		}
		((PowerManagerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getPowerAddOn().setBatteryMaxPower(maxPower);
		
		

//		recharge = Math.max(1f, FastMath.logGrowth(recharge * VoidElementManager.POWER_BATTERY_DIV_FACTOR, VoidElementManager.POWER_BATTERY_GROWTH, VoidElementManager.POWER_BATTERY_CEILING));

		recharge += total * VoidElementManager.POWER_BATTERY_LINEAR_GROWTH;

		((PowerManagerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getPowerAddOn().setBatteryRecharge(recharge);
	}

	@Override
	public boolean isActive() {
		return ((PowerManagerInterface)getElementManager().getManagerContainer()).getPowerAddOn().isBatteryActive();
	}

	@Override
	public void onKilledBlock(long pos, short type, Damager from) {
		if(!getSegmentController().isOnServer()){
			return;
		}
		if(type == ElementKeyMap.POWER_BATTERY && System.currentTimeMillis() - lastHit > 1000){
			lastHit = System.currentTimeMillis();
			List<ModuleExplosion> l = getContainer().getModuleExplosions();
			final int size = l.size();
			for(int i = 0; i < size; i++){
				ModuleExplosion moduleExplosion = l.get(i);
				if(moduleExplosion.getModuleBB().isInside(
						ElementCollection.getPosX(pos), 
						ElementCollection.getPosY(pos), 
						ElementCollection.getPosZ(pos))){
					//don't add explosion, since there is one ongoing in the area
					return;
				}
			}
			
			for(int i = 0; i < getElementCollections().size(); i ++){
				if(getElementCollections().get(i).getNeighboringCollection().contains(pos)){
					PowerBatteryUnit p = getElementCollections().get(i);
					long explosionRate = (long) ((1d / Math.max(0.0000001d, VoidElementManager.POWER_BATTERY_EXPLOSION_RATE)) * 1000d);
					long radLong = 
							(long) (VoidElementManager.POWER_BATTERY_EXPLOSION_RADIUS_PER_BLOCKS * p.size());
					int rad = (int) Math.min(VoidElementManager.POWER_BATTERY_EXPLOSION_RADIUS_MAX,
							Math.min(64, Math.max(3, radLong)));
					long damageLong = 
							(long) (VoidElementManager.POWER_BATTERY_EXPLOSION_DAMAGE_PER_BLOCKS * p.size());
					int damage = (int) Math.min(VoidElementManager.POWER_BATTERY_EXPLOSION_DAMAGE_MAX,
							Math.min(Integer.MAX_VALUE, Math.max(0, damageLong)));
					
					long pCount = (long)(VoidElementManager.POWER_BATTERY_EXPLOSION_COUNT_PER_BLOCKS * p.size());
					int max = (int) (Math.min(1d, VoidElementManager.POWER_BATTERY_EXPLOSION_COUNT_PERCENT)*p.size());
					
					int amount = Math.max(1, (int)Math.min(max, pCount));
					
					p.explodeOnServer(amount, pos, type, explosionRate, rad, damage, false, ExplosionCause.POWER_AUX, from);
					
					break;
				}
			}
			
			
			
			
		}
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		PowerAddOn shieldAddOn = ((PowerManagerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()).getPowerAddOn();
		return  (float) Math.min(1f, shieldAddOn.getBatteryPower() / Math.max(0.0001f, (shieldAddOn.getBatteryMaxPower())));
	}

	@Override
	public WeaponRowElementInterface getWeaponRow() {
		if(getTotalSize() > 0){
			Vector3i idPos = getElementCollections().get(0).getIdPos(new Vector3i());
			SegmentPiece piece = getSegmentController().getSegmentBuffer().getPointUnsave(idPos);
			
			if (piece != null && piece.getType() == ElementKeyMap.POWER_BATTERY) {
				
				WeaponRowElementInterface row = new WeaponPowerBatteryRowElement(piece, this);
				return row;
			}
		}
		return null;
	}

	@Override
	public boolean isControllerConnectedTo(long index, short type) {
		return true;
	}

	@Override
	public boolean isPlayerUsable() {
		return getSegmentController().isUsingOldPower();
	}

	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_POWER_BATTERY;
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
//		PowerAddOn powerAddOn = ((PowerManagerInterface)getElementManager().getManagerContainer()).getPowerAddOn();
//		powerAddOn.handleMouseEventClient(man, unit, e);
		throw new RuntimeException("Implement");
	}
	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Activate"), hos, ContextFilter.IMPORTANT);
	}
}
