package org.schema.game.server.data;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;

public interface SpawnInterface {

	SegmentControllerOutline<?> inst(GameServerState state, BlueprintInterface en, String uniqueIdentifier,
	                                 String realName, float[] mat, int factionId, Vector3i min,
	                                 Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats);

}
