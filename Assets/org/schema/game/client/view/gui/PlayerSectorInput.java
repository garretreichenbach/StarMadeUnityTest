package org.schema.game.client.view.gui;

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.navigation.navigationnew.SavedCoordinatesScrollableListNew;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public abstract class PlayerSectorInput extends PlayerGameTextInput {

	public PlayerSectorInput(GameClientState state, Object title, Object desc, String predefined) {
		super("SEC_INPUT", state, 630, 400, 100, title, desc, predefined);
		getInputPanel().onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(80));
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1));

		SavedCoordinatesScrollableListNew r = new SavedCoordinatesScrollableListNew(getState(), ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1), this);
		r.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).attach(r);

		setInputChecker((entry, callback) -> {
			try {
				if(entry.length() == 0) return true;
				Vector3i.parseVector3iFree(entry);
				return true;
			} catch(NumberFormatException ignored) {}
			callback.onFailedTextCheck(Lng.str("Wrong Format!"));
			return false;
		});
	}

	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
		return null;
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public void onFailedTextCheck(String msg) {
		setErrorMessage(msg);
	}

	@Override
	public boolean onInput(String entry) {
		try {
			if(entry.trim().length() == 0) handleEnteredEmpty();
			else handleEntered(translateRelativeCoords(entry)); //Vector3i.parseVector3iFree(entry
			return true;
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	public abstract void handleEnteredEmpty();

	public abstract void handleEntered(Vector3i p);

	public abstract Object getSelectCoordinateButtonText();

	public Vector3i translateRelativeCoords(String s) {
		if(!s.contains("~")) return Vector3i.parseVector3iFree(s);
		Vector3i currentSector = GameClient.getClientState().getPlayer().getCurrentSector();
		String[] coords = s.split("[\\s,.]");
		for(int j = 0; j < coords.length; j++) {
			if(coords[j].equals("~")) coords[j] = String.valueOf(currentSector.getCoord(j));
			else if(coords[j].startsWith("~")) coords[j] = String.valueOf(currentSector.getCoord(j) + Integer.parseInt(coords[j].substring(1)));
		}
		return Vector3i.parseVector3iFree(coords[0] + "," + coords[1] + "," + coords[2]);
	}
}
