package org.schema.game.client.view.gui;

import java.awt.Font;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class RadialMenuDialogShapes extends RadialMenuDialog implements GUIActivationCallback {

	private final ElementInformation info;

	public RadialMenuDialogShapes(GameClientState state, ElementInformation info) {
		super(state);
		this.info = info;
		assert (this.info.blocktypeIds != null) : this.info;
	}

	@Override
	public RadialMenu createMenu(RadialMenuDialog radialMenuDialog) {
		final GameClientState s = getState();
		RadialMenu m = new RadialMenu(s, "ShapesRadial", radialMenuDialog, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(600), UIScale.getUIScale().scale(50), FontSize.BIG_20.getStyle(Font.BOLD));
		m.setForceBackButton(true);
		assert (this.info.blocktypeIds != null) : this.info;
		for (int i = -1; i < this.info.blocktypeIds.length; i++) {
			final short type;
			if (i < 0) {
				type = info.id;
			} else {
				type = this.info.blocktypeIds[i];
			}
			ElementInformation sin = ElementKeyMap.getInfo(type);
			m.addItemBlock(type, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !RadialMenuDialogShapes.this.isActive(s);
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(665);
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().selectTypeForced(type);
						deactivate();
					} else if (event.pressedRightMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(664);
						getState().getBlockSyleSubSlotController().switchPerm(type);
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().resetSubSlot();
					}
				}
			}, Lng.str("RMB: sticky to hotbar"), new GUIActivationHighlightCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return RadialMenuDialogShapes.this.isActive();
				}

				@Override
				public boolean isHighlighted(InputState state) {
					return getState().getBlockSyleSubSlotController().isPerm(type);
				}
			});
		}
		s.getController().showBigMessage("ShapeRadialMenu", "", Lng.str("Use %s to open the Shape Radial Menu", KeyboardMappings.SHAPES_RADIAL_MENU.getKeyChar()), 0);
		return m;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if ((isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) || e.isTriggered(KeyboardMappings.SHAPES_RADIAL_MENU)) {
			deactivate();
		}
	}

	public PlayerGameControlManager getPGCM() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	@Override
	public boolean isVisible(InputState state) {
		return true;
	}

	@Override
	public boolean isActive(InputState state) {
		return super.isActive();
	}
}
