package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.RoundEndPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;

public class RoundEndMenu extends PlayerInput {

	private final Faction winner;
	private final Faction loser;
	private RoundEndPanel panel;
	private long started;
	private long duration = 20000;
	private String winnerString;
	private String loserString;

	public RoundEndMenu(GameClientState state, int winner, int loser) {
		super(state);

		this.winner = state.getFactionManager().getFaction(winner);
		this.loser = state.getFactionManager().getFaction(loser);

		this.winnerString = this.winner != null ? this.winner.getName() : "(UNKNOWN)" + winner;
		this.loserString = this.loser != null ? this.loser.getName() : "(UNKNOWN)" + loser;

		this.panel = new RoundEndPanel(state, this, getText());

		this.started = System.currentTimeMillis();

		this.setDeactivateOnEscape(false);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse() && !getState().getGlobalGameControlManager().getMainMenuManager().isActive() && !isDelayedFromMainMenuDeactivation()) {

		}
	}

	public String getText() {

		long timeInSecs = (duration - (System.currentTimeMillis() - started)) / 1000;

		return "Round has ended! \n" +
				"Team \"" + winnerString + "\" has won\n" +
				"by destroying the base of \nthe pathetic team \n"
				+ "\"" + loserString + "\"\n\n\n" +
				"The Round Will \nRestart in " + timeInSecs + " seconds";
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		// nothing to do. just consume the key
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#checkDeactivated()
	 */
	@Override
	public boolean checkDeactivated() {
		panel.setInfo(getText());

		return (System.currentTimeMillis() - started) > duration;
	}

	@Override
	public RoundEndPanel getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {

	}

	@Override
	public boolean isOccluded() {
		return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
	}


}
