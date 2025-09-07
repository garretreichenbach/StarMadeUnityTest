package org.schema.game.common.controller.elements.cargo;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class CargoElementManager extends UsableControllableElementManager<CargoUnit, CargoCollectionManager, CargoElementManager> {

	public static boolean debug = false;


	@ConfigurationElement(name = "CapacityPerBlockMultShip")
	public static double CAPACITY_PER_BLOCK_MULT_SHIP = 1000;
	@ConfigurationElement(name = "CapacityPerGroupQuadraticShip")
	public static double CAPACITY_PER_GROUP_QUADRATIC_SHIP = 1000;

	@ConfigurationElement(name = "CapacityPerBlockMultStation")
	public static double CAPACITY_PER_BLOCK_MULT_STATION = 1000;
	@ConfigurationElement(name = "CapacityPerGroupQuadraticStation")
	public static double CAPACITY_PER_GROUP_QUADRATIC_STATION = 1000;

	@ConfigurationElement(name = "InventoryBaseCapacityShip")
	public static double INVENTORY_BASE_CAPACITY_SHIP = 1000;
	@ConfigurationElement(name = "InventoryBaseCapacityStation")
	public static double INVENTORY_BASE_CAPACITY_STATION = 1000;

	@ConfigurationElement(name = "PercentageBledPerMinute")
	public static double PERCENTAGE_BLEED_PER_MINUTE = 1000;


	@ConfigurationElement(name = "PersonalInventoryBaseCapacity")
	public static double PERSONAL_INVENTORY_BASE_CAPACITY = 1000;
	@ConfigurationElement(name = "PersonalFactoryBaseCapacity")
	public static double PERSONAL_FACTORY_BASE_CAPACITY = 1000;

	@ConfigurationElement(name = "LockBoxUnlockTime", description = "How long a lock box will stay unlocked after being opened by a player.")
	public static int LOCK_BOX_UNLOCK_TIME = 60 * 1000; // 60 seconds

	public CargoElementManager(final SegmentController segmentController, short elementKey) {
		super(elementKey, ElementKeyMap.CARGO_SPACE, segmentController);
	}

	public double getCapacityPerBlockMult() {
		if(getSegmentController() instanceof Ship) {
			return CAPACITY_PER_BLOCK_MULT_SHIP;
		} else {
			return CAPACITY_PER_BLOCK_MULT_STATION;
		}
	}

	public double getCapacityPerGroupQuadratic() {
		if(getSegmentController() instanceof Ship) {
			return CAPACITY_PER_GROUP_QUADRATIC_STATION;
		} else {
			return CAPACITY_PER_GROUP_QUADRATIC_SHIP;
		}
	}

	public double getInventoryBaseCapacity() {
		if(getSegmentController() instanceof Ship) {
			return INVENTORY_BASE_CAPACITY_SHIP;
		} else {
			return INVENTORY_BASE_CAPACITY_STATION;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(CargoUnit firingUnit, CargoCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), "Cargo Unit", firingUnit);
	}


	@Override
	protected String getTag() {
		return "cargo";
	}

	@Override
	public CargoCollectionManager getNewCollectionManager(SegmentPiece position, Class<CargoCollectionManager> clazz) {

		return new CargoCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Cargo System Collective");
	}

	@Override
	public boolean isCheckForUniqueConnections() {
		return true;
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}

}
