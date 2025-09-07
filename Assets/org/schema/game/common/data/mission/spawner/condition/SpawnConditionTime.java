package org.schema.game.common.data.mission.spawner.condition;

import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class SpawnConditionTime implements SpawnCondition {

	private long time;

	public SpawnConditionTime() {
	}

	public SpawnConditionTime(long time) {
		super();
		this.time = time;
	}

	@Override
	public boolean isSatisfied(SpawnMarker marker) {
		return System.currentTimeMillis() - marker.getLastSpawned() > this.time;
	}

	@Override
	public SpawnConditionType getType() {
		return SpawnConditionType.TIME;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		time = (Long) t[0].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.LONG, null, time), FinishTag.INST});
	}


}
