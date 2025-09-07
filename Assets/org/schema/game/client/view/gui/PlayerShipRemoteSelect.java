package org.schema.game.client.view.gui;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class PlayerShipRemoteSelect extends PlayerGameTextInput {

	private final Fleet fleet;

	public PlayerShipRemoteSelect(GameClientState state, Fleet fleet) {
		super("REMOTE_SELECT", state, 630, 400, 30, Lng.str("Select a remote to activate."), Lng.str("Enter the name of an inner-ship remote or select a saved one from below."), "");
		this.fleet = fleet;
		getInputPanel().onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().setTextBoxHeightLast(80);
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().addNewTextBox(1);

		SavedRemotesScrollableList list = new SavedRemotesScrollableList(getState(), this, ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).getWidth(), ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).getHeight(), ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1), fleet);
		list.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).attach(list);
	}

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
	public void onDeactivate() {

	}

	@Override
	public boolean onInput(String entry) {
		if(entry == null || entry.isEmpty()) return false;
		else {
			fleet.toggleRemote(entry);
			return true;
		}
	}
}