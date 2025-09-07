package org.schema.game.common.data.mission.spawner;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class SpawnMarker implements TagSerializable {

	private Vector3i pos;
	private SimpleTransformableSendableObject attachedTo;
	private SpawnerInterface spawner;
	private long lastSpawned;

	public SpawnMarker() {
	}

	public SpawnMarker(Vector3i pos,
	                   SimpleTransformableSendableObject attachedTo,
	                   SpawnerInterface spawner) {
		super();
		this.pos = pos;
		this.attachedTo = attachedTo;
		this.spawner = spawner;
	}

	public Vector3i getPos() {
		return pos;
	}

	public SimpleTransformableSendableObject attachedTo() {
		return attachedTo;
	}

	public boolean canSpawn() {
		return spawner.canSpawn(this);
	}

	public void spawn() {
		spawner.spawn(this);
		lastSpawned = System.currentTimeMillis();
	}

	public boolean isAlive() {
		return spawner.isAlive();
	}

	public long getLastSpawned() {
		return lastSpawned;
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		lastSpawned = (Long) t[1].getValue();
		pos = (Vector3i) t[2].getValue();
		spawner = DefaultSpawner.instantiate(t[0]);
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{spawner.toTagStructure(), new Tag(Type.LONG, null, lastSpawned), new Tag(Type.VECTOR3i, null, pos), FinishTag.INST});
	}

	public StateInterface getState() {
		return attachedTo.getState();
	}

	/**
	 * @return the spawner
	 */
	public SpawnerInterface getSpawner() {
		return spawner;
	}

	/**
	 * @param spawner the spawner to set
	 */
	public void setSpawner(SpawnerInterface spawner) {
		this.spawner = spawner;
	}

	public void setAttachedTo(SimpleTransformableSendableObject s) {
		attachedTo = s;
	}
}
