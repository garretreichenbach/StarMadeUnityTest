package org.schema.game.common.data.mission.spawner.condition;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentCollisionCheckerCallback;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class SpawnConditionNonBlocked implements SpawnCondition {

	public Vector3i relativeToSpawnerPos = new Vector3i();

	public SpawnConditionNonBlocked(Vector3i relativeToSpawnerPos) {
		this.relativeToSpawnerPos.set(relativeToSpawnerPos);
	}

	public SpawnConditionNonBlocked() {
	}

	@Override
	public boolean isSatisfied(SpawnMarker marker) {

		if (marker.attachedTo() instanceof EditableSendableSegmentController) {
			EditableSendableSegmentController c = (EditableSendableSegmentController) marker.attachedTo();

			Vector3i pos = new Vector3i(marker.getPos());
			pos.add(relativeToSpawnerPos);
			SegmentPiece pointUnsave = c.getSegmentBuffer().getPointUnsave(pos);

			if (pointUnsave != null && !c.getCollisionChecker().checkPieceCollision(pointUnsave, new SegmentCollisionCheckerCallback(), false)) {
				return true;
			} else {
				//we don't know yet what there is. wait for it to load to safe performance on not autorequsting
				return false;
			}
		}

		return true;
	}

	@Override
	public SpawnConditionType getType() {
		return SpawnConditionType.POS_NON_BLOCKED;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		relativeToSpawnerPos = (Vector3i) t[0].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.VECTOR3i, null, relativeToSpawnerPos), FinishTag.INST});
	}

}
