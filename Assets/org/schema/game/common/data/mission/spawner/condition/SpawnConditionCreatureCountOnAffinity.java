package org.schema.game.common.data.mission.spawner.condition;

import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.creature.AIRandomCompositeCreature;
import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SpawnConditionCreatureCountOnAffinity implements SpawnCondition {

	private int count;

	public SpawnConditionCreatureCountOnAffinity() {
	}

	public SpawnConditionCreatureCountOnAffinity(int count) {
		super();
		this.count = count;
	}

	@Override
	public boolean isSatisfied(SpawnMarker marker) {
		int c = 0;
		assert (marker.attachedTo() != null);
		if (marker.attachedTo().getAttachedAffinity() == null) {
			System.err.println("[SERVER][SPAWNER] Cannot spawn yet: Attached Affinity null of " + marker.attachedTo());
			return false;
		}
		ObjectOpenHashSet<AICreature<? extends AIPlayer>> s = marker.attachedTo().getAttachedAffinity();
		for (AICreature<? extends AIPlayer> a : s) {
			if (a instanceof AIRandomCompositeCreature) {
				c++;
			}
		}
		return c < count;
	}

	@Override
	public SpawnConditionType getType() {
		return SpawnConditionType.CREATURE_COUNT_ON_AFFINITY;
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
