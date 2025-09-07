package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerState;

public class GameModeRequest implements GameRequestInterface{

	public int id = -1;
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(id);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		id = b.readInt();
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.GAME_MODE;
	}

	@Override
	public void free() {
	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState sd) throws IOException {
		GameServerState state = (GameServerState)sd;
		
		GameModeAnswer a = (GameModeAnswer)getFactory().getAnswerInstance();
		
		
		
		GameServerState s = ((GameServerState) state);

		RegisteredClientOnServer client = (RegisteredClientOnServer) ((ServerProcessorInterface)p).getClient();

		PlayerState player = (PlayerState) client.getPlayerObject();

		Sector sector = s.getUniverse().getSector(player.getCurrentSector());

		
		a.gameMode = s.getGameMode();
		a.sectorId = sector.getId();
		a.sectorPos = new Vector3i(sector.pos);
		a.blockConfigChecksum = s.getConfigCheckSum();
		a.blockPropertiesChecksum = s.getConfigPropertiesCheckSum();
		a.blockBehaviorChecksum = s.getBlockBehaviorChecksum();
		a.customBlockTextureChecksum = s.getCustomTexturesChecksum();
		a.customTexturesChecksum = s.getCustomTexturesChecksum();
		a.factionConfigChecksum = s.getFactionConfigCheckSum();
		a.segmentPieceQueueSize = s.getSegmentPieceQueueSize();
		a.asteroidPhysics = ServerConfig.ASTEROIDS_ENABLE_DYNAMIC_PHYSICS.isOn();
		
		
		
		
		GameRequestAnswerFactory.send(a, p);
	}

}
