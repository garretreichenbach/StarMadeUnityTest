package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.gamemodes.GameModes;
import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;

public class GameModeAnswer implements GameAnswerInterface{

	public GameModes gameMode;
	public int sectorId;
	public Vector3i sectorPos;
	public String blockConfigChecksum;
	public String blockPropertiesChecksum;
	public String blockBehaviorChecksum;
	public String customBlockTextureChecksum;
	public String customTexturesChecksum;
	public String factionConfigChecksum;
	public boolean useGalaxy;
	public int segmentPieceQueueSize;
	public boolean asteroidPhysics;
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(gameMode.ordinal());
		b.writeInt(sectorId);
		sectorPos.serialize(b);
		b.writeUTF(blockConfigChecksum);
		b.writeUTF(blockPropertiesChecksum);
		b.writeUTF(blockBehaviorChecksum);
		b.writeUTF(customBlockTextureChecksum);
		b.writeUTF(customTexturesChecksum);
		b.writeUTF(factionConfigChecksum);
		
		b.writeBoolean(useGalaxy);
		b.writeInt(segmentPieceQueueSize);
		b.writeBoolean(asteroidPhysics);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		gameMode = GameModes.values()[b.readByte()];
		sectorId = b.readInt();
		sectorPos = Vector3i.deserializeStatic(b);
		blockConfigChecksum = b.readUTF();
		blockPropertiesChecksum = b.readUTF();
		blockBehaviorChecksum = b.readUTF();
		customBlockTextureChecksum = b.readUTF();
		customTexturesChecksum = b.readUTF();
		factionConfigChecksum = b.readUTF();

		useGalaxy = b.readBoolean();
		segmentPieceQueueSize = b.readInt();
		asteroidPhysics = b.readBoolean();
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.GAME_MODE;
	}

	@Override
	public void free() {
	}

	

}
