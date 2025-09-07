package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUICreateNewsPostPanel extends GUIInputPanel {

	private GUIActivatableTextBar subjectTextBar;
	private GUIActivatableTextBar messageTextBar;

	public GUICreateNewsPostPanel(InputState state,
	                              GUICallback guiCallback) {
		super("GUICreateNewsPostPanel", state, UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(220), guiCallback, "Create News Post", "");
		setOkButtonText("POST");
		onInit();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		subjectTextBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, 80, 1, "NEWS TOPIC", ((GUIDialogWindow) background).getMainContentPane().getContent(0), new DefaultTextCallback(), new DefaultTextChangedCallback());
		subjectTextBar.onInit();
		subjectTextBar.setPos(0, 0, 0);
		((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(subjectTextBar);

		((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(10));

		messageTextBar = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, 144, 5, "NEWS BODY", ((GUIDialogWindow) background).getMainContentPane().getContent(1), new DefaultTextCallback(), new DefaultTextChangedCallback());
		messageTextBar.onInit();
		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(messageTextBar);
		
		
		subjectTextBar.addTextBarToSwitch(messageTextBar);
		messageTextBar.addTextBarToSwitch(subjectTextBar);
		
		subjectTextBar.activateBar();
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
	}

}
