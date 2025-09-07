package org.schema.game.common.data.mission.spawner.condition;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class SpawnConditionPlayerProximity implements SpawnCondition {

	private final Vector3f tmp = new Vector3f();
	float distance;

	public SpawnConditionPlayerProximity(float distance) {
		super();
		this.distance = distance;
	}

	public SpawnConditionPlayerProximity() {
		super();
	}

	@Override
	public boolean isSatisfied(SpawnMarker marker) {

		for (PlayerState s : ((GameServerState) marker.getState()).getPlayerStatesByName().values()) {
			if (s.getCurrentSectorId() == marker.attachedTo().getSectorId() && s.getFirstControlledTransformableWOExc() != null) {
				tmp.sub(s.getFirstControlledTransformableWOExc().getWorldTransform().origin, marker.attachedTo().getWorldTransform().origin);
				if (tmp.length() < distance) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public SpawnConditionType getType() {
		return SpawnConditionType.PLAYER_PROXIMITY;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		distance = (Float) t[0].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.FLOAT, null, distance), FinishTag.INST});
	}


}
