package org.schema.game.server.data;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.gamemap.requests.GameMapRequest;

public class ServerGameMapRequest extends GameMapRequest {

	public final ClientChannel gameState;

	public ServerGameMapRequest(GameMapRequest gameMapRequest, ClientChannel ntState) {
		super(gameMapRequest);
		this.gameState = ntState;
	}

}
