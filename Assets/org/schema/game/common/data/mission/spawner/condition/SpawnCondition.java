package org.schema.game.common.data.mission.spawner.condition;

import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.TagSerializable;

public interface SpawnCondition extends TagSerializable {
	public boolean isSatisfied(SpawnMarker marker);

	public SpawnConditionType getType();
}
