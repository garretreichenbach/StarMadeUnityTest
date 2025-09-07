package org.schema.game.common.controller.elements.rail.inv;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.schine.common.language.Lng;

public class RailConnectionCollectionManager extends
		ControlBlockElementCollectionManager<RailConnectionUnit, RailConnectionCollectionManager, RailConnectionElementManager> {

//	private String activatorName;

	public RailConnectionCollectionManager(SegmentPiece element,
	                                  SegmentController segController, RailConnectionElementManager em) {
		super(element, Element.TYPE_RAIL_INV, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<RailConnectionUnit> getType() {
		return RailConnectionUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public RailConnectionUnit getInstance() {
		return new RailConnectionUnit();
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState())
					.getWorldDrawer().getGuiDrawer().managerChanged(this);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Rail System");
	}

	
	

}
