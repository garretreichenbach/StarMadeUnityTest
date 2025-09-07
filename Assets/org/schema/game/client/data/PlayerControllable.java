package org.schema.game.client.data;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.objects.Sendable;

public interface PlayerControllable extends Identifiable {

	public List<PlayerState> getAttachedPlayers();

	public void handleKeyEvent(ControllerStateUnit u, KeyboardMappings mapping, Timer timer);
	public void handleKeyPress(Timer timer, ControllerStateInterface unit);
	public boolean isControlHandled(ControllerStateInterface unit);

	public boolean isClientOwnObject();

	public boolean isHidden();

	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter);

	public void onDetachPlayer(PlayerState playerState, boolean hide, Vector3i parameter);

	public boolean hasSpectatorPlayers();

	public void onPlayerDetachedFromThis(PlayerState pState,
			PlayerControllable newAttached);

	

//	public void handleMouseEvent(ControllerStateUnit u, MouseEvent e);
//	public void handleKeyEvent(ControllerStateUnit u, KeyboardMappings mapping);


}
