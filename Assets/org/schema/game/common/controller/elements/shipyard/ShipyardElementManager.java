package org.schema.game.common.controller.elements.shipyard;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.NTReceiveInterface;
import org.schema.game.common.controller.elements.NTSenderInterface;
import org.schema.game.common.controller.elements.TagModuleUsableInterface;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.factory.CargoCapacityElementManagerInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;

public class ShipyardElementManager extends UsableControllableElementManager<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> implements
CargoCapacityElementManagerInterface, TagModuleUsableInterface, NTSenderInterface, NTReceiveInterface {

	public static final String TAG_ID = "SYRD";
	
	@ConfigurationElement(name = "ConstructionTickInSeconds")
	public static double CONSTRUCTION_TICK_IN_SECONDS = 1;
	
	@ConfigurationElement(name = "ConstructionBlocksTakenPerTick")
	public static int CONSTRUCTION_BLOCKS_TAKEN_PER_TICK = 1;
	
	@ConfigurationElement(name = "DeconstructionTimePerBlockInMilliseconds")
	public static double DECONSTRUCTION_MS_PER_BLOCK = 0.01;
	
	@ConfigurationElement(name = "DeconstructionConstantTimeInMilliseconds")
	public static int DECONSTRUCTION_CONST_TIME_MS = 1;
	
	@ConfigurationElement(name = "PowerNeededPerShipyardBlock")
	public static float POWER_COST_NEEDED_PER_BLOCK = 50;

	@ConfigurationElement(name = "ShipyardArcMaxSpacing")
	public static int ARC_MAX_SPACING = 16;

	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 0;

	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;

	public ShipyardElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.SHIPYARD_COMPUTER, ElementKeyMap.SHIPYARD_MODULE, segmentController);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}

	@Override
	public void updateToFullNT(NetworkObject networkObject) {
		if (getSegmentController().isOnServer()) {
			sendAllShipyardStatesToClient();
		}
	}

	private void sendAllShipyardStatesToClient() {
		for(ShipyardCollectionManager a : getCollectionManagers()){
			a.sendShipyardStateToClient();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(ShipyardUnit firingUnit,
	                                             ShipyardCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Shipyard Unit"), firingUnit);
	}

	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		return false;
	}

	@Override
	protected String getTag() {
		return "shipyard";
	}

	

	@Override
	public ShipyardCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<ShipyardCollectionManager> clazz) {

		return new ShipyardCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Shipyard System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}


	@Override
	public String getTagId() {
		return TAG_ID;
	}

	public boolean isValidShipYard(SegmentPiece rail) {
		return true;
	}

	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new ShipyardMetaDataDummy();
	}
}
