package org.schema.game.client.controller.manager.ingame;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.CreateDockRequest;
import org.schema.game.network.objects.remote.RemoteCreateDock;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.sound.controller.AudioController;

public class BuildToolCreateDocking {

	public VoidUniqueSegmentPiece potentialCreateDockPos;

	public VoidUniqueSegmentPiece docker;

	public VoidUniqueSegmentPiece rail;

	public VoidUniqueSegmentPiece core;

	public int coreOrientation = -1;

	public int potentialCoreOrientation;

	public float coreDistance = 3;

	public VoidUniqueSegmentPiece potentialCore;

	public String getButtonMsg() {
		return docker == null ? Lng.str("*Pick Rail*") + " " + Lng.str("(Cancel)") : Lng.str("*Pick Core Pos&Rot*") + " " + Lng.str("(Cancel)");
	}

	public void execute(final GameClientState state) {
		PlayerGameTextInput pp = new PlayerGameTextInput("CONFIRM", state, 50, Lng.str("Spawn Dock"), Lng.str("Please enter a name for the new dock."), Lng.str("Rail Dock %d", System.currentTimeMillis())) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public boolean isOccluded() {
				return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage(Lng.str("NAME INVALID: %s", msg));
			}

			@Override
			public boolean onInput(String entry) {
				CreateDockRequest r = new CreateDockRequest();
				r.core = core;
				r.docker = docker;
				r.rail = rail;
				r.name = entry;
				state.getController().getClientChannel().getNetworkObject().createDockBuffer.add(new RemoteCreateDock(r, false));
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("Must only contain Letters or numbers or (_-)!"));
				return false;
			}
		});
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(103);
	}
}
