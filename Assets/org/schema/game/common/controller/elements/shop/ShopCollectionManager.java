package org.schema.game.common.controller.elements.shop;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class ShopCollectionManager extends ControlBlockElementCollectionManager<ShopUnit, ShopCollectionManager, ShopElementManager> {



	
	public ShopCollectionManager(SegmentPiece element,
	                                SegmentController segController, ShopElementManager em) {
		super(element, ElementKeyMap.SHOP_BLOCK_ID, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<ShopUnit> getType() {
		return ShopUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public ShopUnit getInstance() {
		return new ShopUnit();
	}

	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {

		return new GUIKeyValueEntry[]{};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Shop System");
	}






}
