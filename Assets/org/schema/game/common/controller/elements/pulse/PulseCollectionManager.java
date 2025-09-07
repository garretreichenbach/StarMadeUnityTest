package org.schema.game.common.controller.elements.pulse;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.common.language.Lng;

public abstract class PulseCollectionManager<E extends PulseUnit<E, CM, EM>, CM extends PulseCollectionManager<E, CM, EM>, EM extends PulseElementManager<E, CM, EM>> extends ControlBlockElementCollectionManager<E, CM, EM> implements PlayerUsableInterface{

	public PulseCollectionManager(SegmentPiece controllerElement, short clazz,
	                              SegmentController segController, EM em) {
		super(controllerElement, clazz, segController, em);
	}

	@Override
	public int getMargin() {
				return 0;
	}

	@Override
	public boolean needsUpdate() {
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
		return Lng.str("Pulse System");
	}



}
