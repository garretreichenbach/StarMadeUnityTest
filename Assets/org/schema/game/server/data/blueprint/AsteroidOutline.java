package org.schema.game.server.data.blueprint;

import java.io.IOException;
import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.data.world.Sector;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AsteroidOutline extends SegmentControllerOutline<FloatingRock> {
	private int factionId;

	public AsteroidOutline(GameServerState state, BlueprintInterface en, String uniqueIdentifier,
	                       String realName, float[] mat, int factionId, Vector3i min,
	                       Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		super(state, en, uniqueIdentifier, realName, mat, min, max, playerUID, activeAI, sector, stats);
		this.factionId = factionId;
	}

	@Override
	public FloatingRock spawn(Vector3i sectorId, boolean checkProspectedBlockCounts, ChildStats stats, SegmentControllerSpawnCallback callback) throws EntityAlreadyExistsException, StateParameterNotFoundException {

		try {
			Sector sector = callback.sector;
			EntityRequest.existsIdentifier(state, uniqueIdentifier);
			FloatingRock s = EntityRequest.getNewAsteroid(state, uniqueIdentifier, sector.getId(), realName, mat, min.x, min.y, min.z, max.x, max.y, max.z, playerUID, en.isChunk16());
			s.setFactionId(factionId);
			s.setTouched(true, false);
			s.getControlElementMap().setFromMap(en.getControllingMap());
			if (sector != null && sector.isActive()) {
//				state.getController().getSynchController().addNewSynchronizedObjectQueued(s);
				callback.onSpawn(s);
				return s;
			}else {
				callback.onNullSector(s);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	public long spawnInDatabase(Vector3i startSector, GameServerState state,
	                            int creatorThreadId, ObjectArrayList<String> members, ChildStats stats, boolean purgeDuplicate)
			throws SQLException, EntityAlreadyExistsException,
			StateParameterNotFoundException, IOException {
		throw new IllegalArgumentException();

	}

}
