package org.schema.game.common.controller.elements.shop;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.factory.CargoCapacityElementManagerInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class ShopElementManager extends 
UsableControllableElementManager<ShopUnit, ShopCollectionManager, ShopElementManager> implements
CargoCapacityElementManagerInterface
		  {

	public static boolean debug = false;

	@ConfigurationElement(name = "TradingGuildCargoPerShip")
	public static double TRADING_GUILD_CARGO_PER_SHIP = 1.125;

	@ConfigurationElement(name = "TradingGuildCostPerSystem")
	public static long TRADING_GUILD_COST_PER_SYSTEM = 1;
	
	@ConfigurationElement(name = "TradingGuildCostPerCargoShip")
	public static long TRADING_GUILD_COST_PER_CARGO_SHIP = 1;
	
	@ConfigurationElement(name = "TradingGuildTimePerSectorSec")
	public static double TRADING_GUILD_TIME_PER_SECTOR_SEC = 4;
	
	@ConfigurationElement(name = "TradingGuildProfitOfValue")
	public static double TRADING_GUILD_PROFIT_OF_VALUE = 4;
	
	@ConfigurationElement(name = "TradingGuildProfitOfValuePerSystem")
	public static double TRADING_GUILD_PROFIT_OF_VALUE_PER_SYSTEM = 4;

	
	public ShopElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.SHOP_BLOCK_ID, ElementKeyMap.SHOP_BLOCK_ID, segmentController);
	}

	
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(ShopUnit firingUnit,
	                                             ShopCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Shop Unit"), firingUnit);
	}


	@Override
	protected String getTag() {
		return "shop";
	}

	@Override
	public ShopCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<ShopCollectionManager> clazz) {

		return new ShopCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Shop System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		
	}

	

	
}
