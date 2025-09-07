package org.schema.game.common.controller.elements.thrust;

import api.listener.events.systems.ThrustCalculateEvent;
import api.mod.StarLoader;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.BlockKillInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

public class ThrusterCollectionManager extends ElementCollectionManager<ThrusterUnit, ThrusterCollectionManager, ThrusterElementManager> implements PlayerUsableInterface, BlockKillInterface{

	private float totalThrust;
	private float totalThrustRaw;


	public ThrusterCollectionManager(SegmentController segController, ThrusterElementManager em) {
		super(ElementKeyMap.THRUSTER_ID, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<ThrusterUnit> getType() {
		return ThrusterUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public ThrusterUnit getInstance() {
		return new ThrusterUnit();
	}
	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
	}
	@Override
	protected void onChangedCollection() {
		refreshMaxThrust();
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getPlumAndMuzzleDrawer().scheduleUpdatePlums();
		}

	}
	public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
		
	}
	@Override
	public boolean isDetailedElementCollections() {
		return false;
	}
	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		getElementManager();
		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Total Thrust (this structure)"), StringTools.formatPointZero(getTotalThrust())),
				new ModuleValueEntry(Lng.str("Total Thrust Raw (this structure)"), StringTools.formatPointZero(getElementManager().getSingleThrustRaw())),
				new ModuleValueEntry(Lng.str("Total Thrust Raw (all)"), StringTools.formatPointZero(getElementManager().getSharedThrustRaw())),
				new ModuleValueEntry(Lng.str("Total Thrust (all)"), StringTools.formatPointZero(getElementManager().getActualThrust())),
				new ModuleValueEntry(Lng.str("Total Power Consumption/sec "), StringTools.formatPointZero(getElementManager().getPowerConsumption()/ThrusterElementManager.getUpdateFrequency()))};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Thrust System");
	}

	/**
	 * @return the totalThrust
	 */
	public float getTotalThrust() {
		return getTotalThrust(true);
	}

	public float getTotalThrust(boolean inclRule) {
		if(inclRule) return totalThrust * getElementManager().ruleModifierOnThrust;
		else return totalThrust;
	}

	/**
	 * @param totalThrust the totalThrust to set
	 */
	public void setTotalThrust(float totalThrust) {
		this.totalThrust = totalThrust;
	}

	private void refreshMaxThrust() {
		//		System.err.println("REFRESH MAX THRUST!!!!!!!!!!!!!");
		totalThrust = 0;
		for (ThrusterUnit p : getElementCollections()) {
			p.refreshThrusterCapabilities();
			this.totalThrust = this.getTotalThrust() + p.thrust;
		}
		this.totalThrustRaw = getTotalThrust();
		this.totalThrust = (float) (Math.pow(this.getTotalThrust(), ThrusterElementManager.POW_TOTAL.get(isUsingPowerReactors())) * ThrusterElementManager.MUL_TOTAL.get(isUsingPowerReactors()));

        //INSERTED CODE @???
        ThrustCalculateEvent event = new ThrustCalculateEvent(this, this.getTotalThrust(), this.getTotalThrustRaw());
        StarLoader.fireEvent(event, isOnServer());

        this.setTotalThrust(event.getCalculatedThrust());
        this.setTotalThrustRaw(event.getCalculatedThrustRaw());
        ///
	}

	/**
	 * thrust without diminishing modifiers applied
	 * @return
	 */
	public float getTotalThrustRaw() {
		return totalThrustRaw;
	}

	public void setTotalThrustRaw(float totalThrustRaw) {
		this.totalThrustRaw = totalThrustRaw;
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		return Math.min(1f, (getSegmentController().getSpeedCurrent() / getElementManager().getMaxSpeedAbsolute()));
	}
	@Override
	public WeaponRowElementInterface getWeaponRow() {
		return null;
	}

	@Override
	public boolean isControllerConnectedTo(long index, short type) {
		return true;
	}

	@Override
	public boolean isPlayerUsable() {
		return true;
	}

	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_THRUSTER;
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
		getElementManager().handle(unit, timer);
	}
	@Override
	public org.schema.game.common.controller.elements.ElementCollectionManager.CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.ALL_IN_ONE;
	}

	@Override
	public void onKilledBlock(long pos, short type, Damager from) {
		checkIntegrity(pos, type, from);
	}
	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		
	}
}
