package org.schema.game.client.view.gui.mail;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUICreateMailPanel extends GUIInputPanel {

	private GUIActivatableTextBar toTextBar;
	private GUIActivatableTextBar subjectTextBar;
	private GUIActivatableTextBar messageTextBar;
	private String predefinedTo;
	private String predefinedTopic;

	public GUICreateMailPanel(InputState state,
	                          GUICallback guiCallback, String predefinedTo,
	                          String predefinedTopic) {
		super("GUICreateMailPanel", state, 750, 320, guiCallback, Lng.str("Create Mail"), "");
		this.predefinedTo = predefinedTo;
		this.predefinedTopic = predefinedTopic;
		setOkButtonText(Lng.str("SEND"));
		onInit();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		toTextBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, Lng.str("RECIPIENT"), ((GUIDialogWindow) background).getMainContentPane().getContent(0), new DefaultTextCallback(), new DefaultTextChangedCallback());
		toTextBar.onInit();
		toTextBar.appendText(predefinedTo);

		subjectTextBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, 80, 1, Lng.str("SUBJECT"), ((GUIDialogWindow) background).getMainContentPane().getContent(0), new DefaultTextCallback(), new DefaultTextChangedCallback());
		subjectTextBar.onInit();
		subjectTextBar.appendText(predefinedTopic);
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(toTextBar);
		subjectTextBar.setPos(0, toTextBar.getHeight(), 0);
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(subjectTextBar);

		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(52));
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1));

		messageTextBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, 420, 8, Lng.str("MESSAGE"), ((GUIDialogWindow) background).getMainContentPane().getContent(1), new DefaultTextCallback(), new DefaultTextChangedCallback()){
			
		};
		messageTextBar.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(messageTextBar);
	}

	public String getTo() {
		return toTextBar.getText();
	}

	public String getSubject() {
		return subjectTextBar.getText();
	}

	public String getMessage() {
		return messageTextBar.getText();
	}

	private class DefaultTextChangedCallback implements OnInputChangedCallback {
		@Override
		public String onInputChanged(String t) {
			return t;
		}
	}

	private class DefaultTextCallback implements TextCallback {
		@Override
		public String[] getCommandPrefixes() {
			return null;
		}

		@Override
		public String handleAutoComplete(String s, TextCallback callback,
		                                 String prefix) throws PrefixNotFoundException {
			return getState().onAutoComplete(s, callback, "#");
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
	}

}
