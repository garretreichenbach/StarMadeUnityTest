package org.schema.game.server.data.blueprint;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.data.world.Sector;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ShopOutline extends SegmentControllerOutline<ShopSpaceStation> {
	private int sentinelTeam;

	public ShopOutline(GameServerState state, BlueprintInterface en, String uniqueIdentifier,
	                   String realName, float[] mat, int sentinelTeam, Vector3i min,
	                   Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		super(state, en, uniqueIdentifier, realName, mat, min, max, playerUID, activeAI, sector, stats);
		this.sentinelTeam = sentinelTeam;
		checkForChilds(this.sentinelTeam);
	}

	@Override
	public ShopSpaceStation spawn(Vector3i sectorId, boolean checkProspectedBlockCounts, ChildStats stats, SegmentControllerSpawnCallback callback) throws EntityAlreadyExistsException, StateParameterNotFoundException {
		try {
			Sector sector = callback.sector;
			EntityRequest.existsIdentifier(state, uniqueIdentifier);
			ShopSpaceStation s = EntityRequest.getNewShop(state, uniqueIdentifier, sector.getId(), realName, mat, min.x, min.y, min.z, max.x, max.y, max.z, playerUID, en.isChunk16());
			s.setFactionId(sentinelTeam);
			s.getControlElementMap().setFromMap(en.getControllingMap());

			s.blueprintIdentifier = blueprintUID;
			s.blueprintSegmentDataPath = blueprintFolder;

			if (childs != null) {
				for (SegmentControllerOutline<?> c : childs) {
					c.parent = s;
					c.parentOutline = this;
				}
				for (SegmentControllerOutline<?> c : childs) {
					c.scrap = scrap;
					c.dockTo = uniqueIdentifier;
					c.itemsToSpawnWith = itemsToSpawnWith;
					assert (c.dockTo != null);
					c.spawn(sectorId, false, stats, callback);
				}
			}

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
	public long spawnInDatabase(Vector3i startSector, GameServerState state, int creatorThreadId, ObjectArrayList<String> members, ChildStats stats, boolean purgeDuplicate) {
		throw new IllegalArgumentException();

	}

}
