package org.schema.game.common.data.mission.spawner.component;

import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.TagSerializable;

public interface SpawnComponent extends TagSerializable {
	public void execute(SpawnMarker marker);

	public SpawnComponentType getType();
}
