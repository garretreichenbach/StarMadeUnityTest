package org.schema.game.client.view.gui.manualtrade;

import org.schema.game.client.controller.PlayerManualTradeInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerTextbox;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIPlayerTradePanel extends GUIInputPanel implements GUIActiveInterface {

	private PlayerManualTradeInput dialog;

	public GUIPlayerTradePanel(InputState state, int width, int height, PlayerManualTradeInput guiCallback) {
		super("GUIPlayerTradePanel", state, width, height, guiCallback, "Trade", "");
		setOkButton(false);
		this.dialog = guiCallback;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GUIContentPane mc = ((GUIDialogWindow) background).getMainContentPane();
		mc.addDivider(280);
		mc.setEqualazingHorizontalDividers(0);
		createDiv(0, mc, dialog.trade.a[0]);
		createDiv(1, mc, dialog.trade.a[0]);
	}

	private void createDiv(int div, GUIContentPane mc, final AbstractOwnerState p) {
		mc.setTextBoxHeightLast(div, 49);
		GUIInnerTextbox tb0 = mc.getTextboxes(div).get(0);
		GUIHorizontalButtonTablePane bn = new GUIHorizontalButtonTablePane(getState(), 1, 2, tb0);
		bn.onInit();
		bn.addButton(0, 0, Lng.str("READY"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return p != ((GameClientState) getState()).getPlayer();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					assert (p == ((GameClientState) getState()).getPlayer());
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(550);
					dialog.trade.clientAccept(((GameClientState) getState()).getPlayer(), !dialog.trade.isReady(((GameClientState) getState()).getPlayer()));
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIPlayerTradePanel.this.isActive();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return dialog.trade.isReady(p);
			}
		});
		bn.addButton(0, 1, Lng.str("CANCEL"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return p != ((GameClientState) getState()).getPlayer();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(551);
					assert (p == ((GameClientState) getState()).getPlayer());
					dialog.trade.clientCancel(((GameClientState) getState()).getPlayer());
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return p == ((GameClientState) getState()).getPlayer();
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIPlayerTradePanel.this.isActive();
			}
		});
		tb0.attach(bn);
		mc.addNewTextBox(div, 86);
		GUIInnerTextbox tb1 = mc.getTextboxes(div).get(1);
		GUIManualTradeItemScrollableList lRec = new GUIManualTradeItemScrollableList(getState(), tb1, dialog);
		lRec.onInit();
		tb1.attach(lRec);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == this) && super.isActive();
	}
}
