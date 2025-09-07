package org.schema.game.client.controller.manager.ingame.faction;

import java.util.regex.Pattern;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;

public class FactionDialog extends PlayerGameTextInput {

	private AbstractControlManager man;

	public FactionDialog(final GameClientState state, String title, AbstractControlManager man) {

		super("FactionDialog", state, 420, 200, FactionManager.CODE_MAX_LENGTH - 1, title, "Enter a name for the new faction\n\nWARNING: if you aleady are in a faction,\nyou will leave that faction\nwhen creating a new one", null);

		System.err.println("CURRENT FACTION CODE: " + state.getPlayer().getFactionId());

		this.man = man;

		setInputChecker((entry, callback) -> {

			if (entry.length() >= FactionManager.CODE_MIN_LENGTH
					&& entry.length() < FactionManager.CODE_MAX_LENGTH) {
				if (Pattern.matches("[a-zA-Z0-9 _-]+", entry)) {
					return true;
				} else {
					System.err.println("MATCH FOUND ^ALPHANUMERIC");
				}
			}

			callback.onFailedTextCheck("Please only alphanumeric (and space, _, -) values \nand between " + FactionManager.CODE_MIN_LENGTH + " and " + FactionManager.CODE_MAX_LENGTH + " long!");
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
		man.suspend(false);
	}

	@Override
	public boolean onInput(String entry) {
		getState().getPlayer().getFactionController().clientCreateFaction(entry, "a faction");
		return true;
	}

}
