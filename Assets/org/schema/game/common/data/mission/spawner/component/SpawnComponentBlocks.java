package org.schema.game.common.data.mission.spawner.component;

import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class SpawnComponentBlocks implements SpawnComponent {

	short type;
	int count;

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		type = (Short) t[0].getValue();
		count = (Integer) t[1].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.SHORT, null, type), new Tag(Type.INT, null, count), FinishTag.INST});
	}


	@Override
	public void execute(SpawnMarker marker) {

	}

	@Override
	public SpawnComponentType getType() {
		return SpawnComponentType.BLOCKS;
	}
}
