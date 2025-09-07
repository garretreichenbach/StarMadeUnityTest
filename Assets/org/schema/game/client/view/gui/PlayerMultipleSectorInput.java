package org.schema.game.client.view.gui;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.navigation.navigationnew.SavedCoordinatesScrollableListNew;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public abstract class PlayerMultipleSectorInput extends PlayerSectorInput {

	public PlayerMultipleSectorInput(GameClientState state, Object title, Object desc, String predefined) {
		super(state, title, desc, predefined);
		getInputPanel().onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().setTextBoxHeightLast(230);
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().addNewTextBox(1);
		SavedCoordinatesScrollableListNew r = new SavedCoordinatesScrollableListNew(getState(), ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1), this);
		r.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).attach(r);

		setInputChecker((entry, callback) -> {
			if(entry.isEmpty()) return true;
			String[] split = entry.split(";");
			for(String s : split) {
				try {
					Vector3i.parseVector3iFree(s.trim());
				} catch(NumberFormatException e) {
					callback.onFailedTextCheck(Lng.str("Wrong Format!"));
					return false;
				}
			}
			return true;
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
			else handleEntered(convert(entry));
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return false;
	}
	public abstract void handleEnteredEmpty();
	public abstract void handleEntered(Vector3i... p);

	public abstract Object getSelectCoordinateButtonText();

	private Vector3i[] convert(String entry) {
		String[] split = entry.split(";");
		Vector3i[] ret = new Vector3i[split.length];
		for(int i = 0; i < split.length; i++) ret[i] = translateRelativeCoords(split[i].trim());
		if(ret.length > 10) {
			Vector3i[] ret2 = new Vector3i[10];
			System.arraycopy(ret, 0, ret2, 0, 10);
			return ret2;
		}
		return ret;
	}

	public void addCoordinate(SavedCoordinate f) {
		//if(getInputPanel().getText().contains(f.getSector().toString())) return;
		String s = getInputPanel().getText();
		if(getCurrentInput().length > 0) s += ";";
		s += f.getSector().toString();
		getInputPanel().setText(s);
	}

	public Vector3i[] getCurrentInput() {
		if(getInputPanel().getText().isEmpty()) return new Vector3i[0];
		else return convert(getInputPanel().getText());
	}
}
