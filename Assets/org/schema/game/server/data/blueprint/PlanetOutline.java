package org.schema.game.server.data.blueprint;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Planet;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlanetOutline extends SegmentControllerOutline<Planet> {
	public PlanetOutline(GameServerState state, BlueprintInterface en, String uniqueIdentifier,
	                     String realName, float[] mat, int sentinelTeam, Vector3i min,
	                     Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		super(state, en, uniqueIdentifier, realName, mat, min, max, playerUID, activeAI, sector, stats);
	}

	@Override
	public Planet spawn(Vector3i sectorId, boolean checkProspectedBlockCounts, ChildStats stats, SegmentControllerSpawnCallback callback) throws EntityAlreadyExistsException, StateParameterNotFoundException {
		throw new IllegalAccessError();
		//		EntityRequest.existsIdentifier(state, uniqueIdentifier);
		//		Ship s = EntityRequest.getNewShip(state, uniqueIdentifier, sectorId, realName, mat, min.x, min.y, min.z, max.x, max.y, max.z);
		//		s.getControlElementMap().fromTagStructure(en.controlStructure);
		//
		//		if(sentinelTeam != FactionManager.ID_NEUTRAL){
		//			s.getAiConfiguration().get(Types.AIM_AT).switchSetting("Any", true);
		//			s.getAiConfiguration().get(Types.TYPE).switchSetting("Ship", true);
		//			s.getAiConfiguration().get(Types.TEAM).switchSetting(AIConfiguration.getTeam(sentinelTeam.getTeamId()), true);
		//			s.getAiConfiguration().get(Types.ACTIVE).switchSetting("true", true);
		//
		//			s.getAiConfiguration().applyServerSettings();
		//		}
		//
		//		((GameServerState)state).getController().getSynchController().addNewSynchronizedObjectQueued(s);
	}

	@Override
	public long spawnInDatabase(Vector3i startSector, GameServerState state, int creatorThreadId, ObjectArrayList<String> members, ChildStats stats, boolean purgeDuplicate) {
		throw new IllegalArgumentException();

	}

}
