package org.schema.game.client.controller.manager.ingame.faction;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;

import java.util.regex.Pattern;

public class FactionDialogNew extends PlayerGameTextInput {

	public FactionDialogNew(final GameClientState state, String title) {

		super("FactionDialogNew", state, 420, 210, FactionManager.CODE_MAX_LENGTH - 1, title, Lng.str("Enter a name for the faction\n\nWARNING: if you aleady are in a faction,\nyou will leave that faction\nwhen creating a new one"), null);

		setInputChecker((entry, callback) -> {

			if(entry.length() >= FactionManager.CODE_MIN_LENGTH
					&& entry.length() < FactionManager.CODE_MAX_LENGTH) {
				if(Pattern.matches("[a-zA-Z0-9 _-]+", entry)) {
					return true;
				} else {
					callback.onFailedTextCheck("Please only alphanumeric (and space, _, -)!");
					System.err.println("MATCH FOUND ^ALPHANUMERIC");
				}
			} else {
				callback.onFailedTextCheck("Please enter name between " + FactionManager.CODE_MIN_LENGTH + " and " + FactionManager.CODE_MAX_LENGTH + " long!");
			}

			return false;
		});
	}

	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public String handleAutoComplete(String s,
	                                 TextCallback callback, String prefix) {
		return s;
	}

	@Override
	public void onFailedTextCheck(String msg) {
		setErrorMessage(msg);
	}

	@Override
	public boolean isOccluded() {
		return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public boolean onInput(String entry) {
		getState().getPlayer().getFactionController().clientCreateFaction(entry, Lng.str("Faction name"));
		return true;
	}

}
