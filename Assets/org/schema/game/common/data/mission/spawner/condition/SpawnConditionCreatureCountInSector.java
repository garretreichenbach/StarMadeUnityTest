package org.schema.game.common.data.mission.spawner.condition;

import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class SpawnConditionCreatureCountInSector implements SpawnCondition {

	private int count;

	public SpawnConditionCreatureCountInSector() {
	}

	public SpawnConditionCreatureCountInSector(int count) {
		super();
		this.count = count;
	}

	@Override
	public boolean isSatisfied(SpawnMarker marker) {
		int c = 0;

		for (Sendable s : marker.getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof AICreature<?> && ((AICreature<?>) s).getSectorId() == marker.attachedTo().getSectorId()) {
				c++;
			}
		}

		return c < count;
	}

	@Override
	public SpawnConditionType getType() {
		return SpawnConditionType.CREATURE_COUNT_IN_SECTOR;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		count = (Integer) t[0].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.INT, null, count), FinishTag.INST});
	}


}
