package org.schema.game.client.view.gui.factorymanager;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerCollection;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerModule;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class FactoryManagerDialog extends PlayerInput {
	
	private final FactoryManagerPanel panel;
	
	public FactoryManagerDialog(GameClientState state, SegmentPiece segmentPiece) {
		super(state);
		(panel = new FactoryManagerPanel(state, this, segmentPiece)).onInit();
	}

	@Override
	public void onDeactivate() {
		
	}

	@Override
	public FactoryManagerPanel getInputPanel() {
		return panel;
	}
	
	public static class FactoryManagerPanel extends GUIInputPanel {

		private final SegmentPiece segmentPiece;
		private final FactoryManagerModule factoryManagerModule;
		
		public FactoryManagerPanel(InputState state, GUICallback guiCallback, SegmentPiece segmentPiece) {
			super("Factory_Manager_Panel", state, guiCallback, Lng.str("Factory Manager"), "");
			this.segmentPiece = segmentPiece;
			factoryManagerModule = FactoryManagerCollection.getFromSegmentPiece(segmentPiece);
		}
		
		@Override
		public void onInit() {
			super.onInit();
			GUIContentPane contentPane = ((GUIDialogWindow) background).getMainContentPane();
		}
	}
}
