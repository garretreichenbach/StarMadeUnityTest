package org.schema.game.common.data.mission.spawner;

import java.util.List;

import org.schema.game.common.data.mission.spawner.component.SpawnComponent;
import org.schema.game.common.data.mission.spawner.condition.SpawnCondition;
import org.schema.schine.resource.tag.TagSerializable;

public interface SpawnerInterface extends TagSerializable {

	public boolean canSpawn(SpawnMarker marker);

	public void spawn(SpawnMarker marker);

	public boolean isAlive();

	public void setAlive(boolean alive);

	public List<SpawnComponent> getComponents();

	public List<SpawnCondition> getConditions();
}
