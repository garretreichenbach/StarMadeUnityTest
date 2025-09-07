package org.schema.game.client.view.gui.fleetmanager;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.input.InputState;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class FleetManagerDialog extends PlayerInput {
	
	private final FleetManagerPanel panel;
	
	public FleetManagerDialog(GameClientState state, SegmentPiece segmentPiece) {
		super(state);
		(panel = new FleetManagerPanel(state, this, segmentPiece)).onInit();
	}

	@Override
	public void onDeactivate() {
		
	}

	@Override
	public FleetManagerPanel getInputPanel() {
		return panel;
	}

	public static class FleetManagerPanel extends GUIInputPanel {
		
		private final SegmentPiece segmentPiece;

		public FleetManagerPanel(InputState state, GUICallback guiCallback, SegmentPiece segmentPiece) {
			super("Fleet_Manager_Panel", state, guiCallback, Lng.str("Fleet Manager"), "");
			this.segmentPiece = segmentPiece;
		}

		@Override
		public void onInit() {
			super.onInit();
		}
	}
}
