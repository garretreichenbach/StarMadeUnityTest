package org.schema.game.client.controller;


import org.lwjgl.glfw.GLFW;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUITextAreaInputPanel;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.schine.common.TextAreaInput;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

public class PlayerMessageInput extends PlayerTextAreaInput {

	public static int RECEIVER = 0;
	public static int TOPIC = 1;
	public static int MESSAGE = 2;
	private GUITextInput receiver;
	private GUITextInput topic;
	private int active = RECEIVER;
	private GUITextOverlay to;

	public PlayerMessageInput(GameClientState state, PlayerMessage replyTo) {
		super("PlayerMessageInput", state, 440, 100, 1024, 15, "", "Enter Message", FontSize.MEDIUM_15);

		//		getTextInput().append("enter");

		receiver = new GUITextInput(100, 20, state);
		topic = new GUITextInput(300, 20, state);

		receiver.setTextBox(true);
		topic.setTextBox(true);

		GUITextOverlay recText = new GUITextOverlay(state);
		GUITextOverlay topText = new GUITextOverlay(state);
		recText.setTextSimple("To:");
		topText.setTextSimple("Subject:");

		receiver.getPos().x = 20;
		topic.getPos().x = 20;
		receiver.getPos().y = -45;
		topic.getPos().y = -29;

		receiver.setTextInput(new TextAreaInput(64, 1, new TextCallback() {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback,
			                                 String prefix) throws PrefixNotFoundException {
				return getState().onAutoComplete(s, this, prefix);
			}


		}));
		topic.setTextInput(new TextAreaInput(128, 1, new TextCallback() {

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback,
			                                 String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}));
		topic.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(false);
					receiver.setDrawCarrier(false);
					topic.setDrawCarrier(false);
					topic.setDrawCarrier(true);
					active = TOPIC;
				}
			}			@Override
			public boolean isOccluded() {
				return false;
			}


		});
		receiver.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(false);
					receiver.setDrawCarrier(false);
					topic.setDrawCarrier(false);
					receiver.setDrawCarrier(true);
					active = RECEIVER;
				}
			}
		});
		to = new GUITextOverlay(state) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#draw()
			 */
			@Override
			public void draw() {
				if (!(Boolean) getUserPointer()) {
					super.draw();
				}
			}

		};
		to.setTextSimple("click here to enter message body");
		to.setUserPointer(false);

		GUIAnchor text = new GUIAnchor(state, 400, 100);

		text.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					active = MESSAGE;
					to.setUserPointer(true);
					((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(false);
					receiver.setDrawCarrier(false);
					topic.setDrawCarrier(false);
					((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(true);
				}
			}
		});

		if (replyTo != null) {
			System.err.println("[CLIENT][MESSAGES][GUI] REPLY FOR " + replyTo);
			receiver.getTextInput().append(replyTo.getFrom());
			if (!replyTo.getTopic().startsWith("[RE] ")) {
				topic.getTextInput().append("[RE] ");
			}
			topic.getTextInput().append(replyTo.getTopic());
		} else {
			System.err.println("[CLIENT][MESSAGES][GUI] No reply. New message!");
		}

		text.attach(to);
		text.setMouseUpdateEnabled(true);
		receiver.setMouseUpdateEnabled(true);
		topic.setMouseUpdateEnabled(true);
		getInputPanel().onInit();
		getInputPanel().getContent().attach(receiver);
		getInputPanel().getContent().attach(topic);
		getInputPanel().getContent().attach(text);

		recText.setPos(receiver.getPos());
		topText.setPos(topic.getPos());

		recText.getPos().x -= 50;
		topText.getPos().x -= 50;

		getInputPanel().getContent().attach(recText);
		getInputPanel().getContent().attach(topText);

		getInputPanel().getContent().setMouseUpdateEnabled(true);

		((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(false);
		((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().getTextInput().setLinewrap(80);
		receiver.setDrawCarrier(true);
		topic.setDrawCarrier(false);

	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {

		if (isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			deactivate();
			return;
		} else if (e.getKeyboardKeyRaw() == GLFW.GLFW_KEY_TAB) {
			String bef = receiver.getTextInput().getCache();
			String after = receiver.getTextInput().getCache();
			if (active == RECEIVER) {

				receiver.getTextInput().handleKeyEvent(e);
				after = receiver.getTextInput().getCache();
			}
			if (bef.equals(after)) {
				//change if no change (stay on autocomplete)
				active = (active + 1) % 3;
			}

			return;
		}
		((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(false);
		receiver.setDrawCarrier(false);
		topic.setDrawCarrier(false);

		if (active == MESSAGE) {
			to.setUserPointer(true);
			((GUITextAreaInputPanel) getInputPanel()).getGuiTextInput().setDrawCarrier(true);
			super.handleKeyEvent(e);
		} else if (active == RECEIVER) {
			receiver.setDrawCarrier(true);
			receiver.getTextInput().handleKeyEvent(e);
		} else if (active == TOPIC) {
			topic.setDrawCarrier(true);
			topic.getTextInput().handleKeyEvent(e);
		}

	}
	@Override
	public void handleCharEvent(KeyEventInterface e) {
		if (active == MESSAGE) {
			super.handleCharEvent(e);
		} else if (active == RECEIVER) {
			receiver.getTextInput().handleCharEvent(e);
		} else if (active == TOPIC) {
			topic.getTextInput().handleCharEvent(e);
		}
	}
	@Override
	public void onDeactivate() {
	}	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public boolean onInput(String entry) {
		getState().getController().getClientChannel().getPlayerMessageController().clientSend(getState().getPlayerName(), receiver.getTextInput().getCache().trim(), topic.getTextInput().getCache().trim(), entry);
		return true;
	}	@Override
	public String handleAutoComplete(String s, TextCallback callback,
	                                 String prefix) throws PrefixNotFoundException {
		return null;
	}
	@Override
	public void onFailedTextCheck(String msg) {
	}







}
