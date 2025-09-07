package org.schema.game.client.view.gui.mail;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerMailInputNew;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.game.common.data.player.playermessage.PlayerMessageController;
import org.schema.game.network.objects.remote.RemotePlayerMessage;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIMailPanelNew extends GUIInputPanel {

	private PlayerMessageController messageController;

	public GUIMailPanelNew(InputState state, int width, int height, GUICallback guiCallback) {
		super("GUIMailPanelNew", state, width, height, guiCallback, "Mail", "");
		messageController = ((GameClientState) getState()).getController().getClientChannel().getPlayerMessageController();
		setOkButton(false);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 2, ((GUIDialogWindow) background).getMainContentPane().getContent(0));
		p.onInit();
		p.activeInterface = GUIMailPanelNew.this::isActive;
		p.addButton(0, 0, Lng.str("CREATE NEW MAIL"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerMailInputNew input = new PlayerMailInputNew((GameClientState) getState());
					input.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(540);
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, null);
		p.addButton(1, 0, Lng.str("MARK ALL AS READ"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					for (int i = 0; i < messageController.messagesReceived.size(); i++) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(541);
						PlayerMessage f = messageController.messagesReceived.get(i);
						if (!f.isRead()) {
							f.setRead(true);
							((GameClientState) getState()).getController().getClientChannel().getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(f, false));
						}
					}
				}
			}
		}, null);
		p.addButton(1, 1, Lng.str("DELETE ALL"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("GUIMailPanelNew_DELETE_CONFIRM", (GameClientState) getState(), 300, 100, Lng.str("Confirm"), Lng.str("Do you really want to delete all mails?")) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							messageController.deleteAllMessagesClient();
							deactivate();
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(542);
				}
			}
		}, null);
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(p);
		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(56));
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1));
		GUIMailScrollableList l = new GUIMailScrollableList(getState(), ((GUIDialogWindow) background).getMainContentPane().getContent(1));
		l.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(l);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#isActive()
	 */
	@Override
	public boolean isActive() {
		return (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == this) && super.isActive();
	}
}
