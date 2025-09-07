package org.schema.game.common.data.mission.spawner.component;

import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class SpawnComponentDestroySpawnerAfterCount implements SpawnComponent {

	int count;
	private int maxCount;

	public SpawnComponentDestroySpawnerAfterCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public SpawnComponentDestroySpawnerAfterCount() {
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		count = (Integer) t[0].getValue();
		maxCount = (Integer) t[1].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.INT, null, count), new Tag(Type.INT, null, maxCount), FinishTag.INST});
	}

	@Override
	public void execute(SpawnMarker marker) {
		count++;
		if (count >= maxCount) {
			marker.getSpawner().setAlive(false);
		}
	}

	@Override
	public SpawnComponentType getType() {
		return SpawnComponentType.DESTROY_SPAWNER_AFTER_COUNT;
	}
}
