package org.schema.game.common.controller.elements.trigger;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class TriggerCollectionManager extends ControlBlockElementCollectionManager<TriggerUnit, TriggerCollectionManager, TriggerElementManager> {

	public TriggerCollectionManager(SegmentPiece element,
	                                SegmentController segController, TriggerElementManager em) {
		super(element, ElementKeyMap.SIGNAL_TRIGGER_AREA, segController, em);
	}

	@Override
	public int getMargin() {
				return 0;
	}

	@Override
	protected Class<TriggerUnit> getType() {
		return TriggerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public TriggerUnit getInstance() {
		return new TriggerUnit();
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Trigger (area) System");
	}

}
