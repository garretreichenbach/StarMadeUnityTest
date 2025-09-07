package org.schema.game.client.view.gui.navigation.navigationnew;

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.WaypointIcons;
import org.schema.game.client.view.gui.GUIColorPicker;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.GUIScrollableOverlayList;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.Locale;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class SaveCoordinateDialog extends DialogInput {

	private final SaveCoordinatePanel panel;

	public SaveCoordinateDialog(InputState state, Vector3i pos) {
		super(state);
		if(pos == null) pos = GameClient.getClientPlayerState().getCurrentSector();
		(panel = new SaveCoordinatePanel(state, this, pos)).onInit();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(event.pressedLeftMouse() && callingGuiElement.getUserPointer() instanceof String pointer) {
			switch(pointer.toUpperCase(Locale.ENGLISH)) {
				case "X":
				case "CANCEL":
					deactivate();
					break;
				case "OK":
					panel.saveWaypoint();
					deactivate();
					break;
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return false;
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		panel.cleanUp();
		((GameClientState) getState()).getWorldDrawer().getGameMapDrawer().getCamera().setAllowZoom(true);
		((GameClientState) getState()).getWorldDrawer().getGameMapDrawer().getCamera().alwaysAllowWheelZoom = true;
	}

	private static class SaveCoordinatePanel extends GUIInputPanel {

		private final Vector3i pos;
		private GUIActivatableTextBar nameField;
		private GUIColorPicker colorPicker;
		private GUIScrollableOverlayList iconPicker;
		private WaypointIcons icon = WaypointIcons.OUTPOST;

		public SaveCoordinatePanel(InputState state, GUICallback guiCallback, Vector3i pos) {
			super("Save_Coordinate_Panel", state, 650, 750, guiCallback, Lng.str("Save Coordinate"), "");
			this.pos = pos;
		}

		@Override
		public void onInit() {
			super.onInit();
			GUIDialogWindow window = ((GUIDialogWindow) background);
			GUIContentPane contentPane = window.getMainContentPane();
			contentPane.setTextBoxHeightLast(28);

			(nameField = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.MEDIUM_15, 32, 1, Lng.str("New Waypoint"), contentPane.getContent(0), new TextCallback() {
				@Override
				public String[] getCommandPrefixes() {
					return new String[0];
				}

				@Override
				public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
					return null;
				}

				@Override
				public void onFailedTextCheck(String msg) {

				}

				@Override
				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {

				}

				@Override
				public void newLine() {

				}
			}, text -> text)).onInit();
			nameField.setText(Lng.str("New Waypoint"));
			contentPane.getContent(0).attach(nameField);

			contentPane.addNewTextBox(300);
			contentPane.addNewTextBox(300);
			(iconPicker = new GUIScrollableOverlayList(getState(), contentPane.getContent(2), GUIScrollablePanel.SCROLLABLE_VERTICAL, 128, 128)).onInit();
			for(GUIOverlay overlay : WaypointIcons.getOverlays()) iconPicker.addOverlay(overlay, ((String) overlay.getUserPointer()).replaceAll("_", " "), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse() && callingGuiElement.getUserPointer() instanceof String pointer) {
						icon = WaypointIcons.getIcon(pointer);
						iconPicker.setSelected(overlay);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});

			contentPane.getContent(2).attach(iconPicker);
			(colorPicker = new GUIColorPicker(getState(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (oldColor, newColor) -> iconPicker.setColor(newColor))).onInit();
			contentPane.getContent(1).attach(colorPicker);
			iconPicker.setColor(colorPicker.getColor());
			adaptSizes();
		}

		@Override
		public void draw() {
			super.draw();
			((GameClientState) getState()).getWorldDrawer().getGameMapDrawer().getCamera().setAllowZoom(false);
			((GameClientState) getState()).getWorldDrawer().getGameMapDrawer().getCamera().alwaysAllowWheelZoom = false;
		}

		public void saveWaypoint() {
			((GameClientState) getState()).getController().getClientGameData().addSavedCoordinate(new SavedCoordinate(pos, getCoorindateName(), getColor(), icon.index));
		}

		private Vector4f getColor() {
			return colorPicker.getColor();
		}

		private String getCoorindateName() {
			return nameField.getText().trim();
		}
	}
}
