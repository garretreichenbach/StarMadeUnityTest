package org.schema.game.common.data.mission.spawner;

import java.util.List;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SpawnController implements TagSerializable {

	public long lastUpdate;

	private List<SpawnMarker> spawnMarkers = new ObjectArrayList<SpawnMarker>(0);

	private SimpleTransformableSendableObject attachedTo;

	public SpawnController(SimpleTransformableSendableObject s) {
		super();
		this.attachedTo = s;
	}

	public void update(Timer timer) {

		if (!spawnMarkers.isEmpty() && System.currentTimeMillis() - lastUpdate > 500) {
			for (int i = 0; i < spawnMarkers.size(); i++) {
				SpawnMarker sm = spawnMarkers.get(i);
				if (sm.canSpawn()) {
					sm.canSpawn();
					sm.spawn();
				}
				if (!sm.isAlive()) {
					spawnMarkers.remove(i);
					i--;
				}
			}
			lastUpdate = System.currentTimeMillis();
		}
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] values = (Tag[]) tag.getValue();
		Tag[] t = (Tag[]) values[0].getValue();

		spawnMarkers = new ObjectArrayList(t.length - 1);
		for (int i = 0; i < t.length - 1; i++) {
			SpawnMarker m = new SpawnMarker();
			m.setAttachedTo(attachedTo);
			m.fromTagStructure(t[i]);
			spawnMarkers.add(m);
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag[] t = new Tag[spawnMarkers.size() + 1];
		t[t.length - 1] = FinishTag.INST;
		for (int i = 0; i < spawnMarkers.size(); i++) {
			t[i] = spawnMarkers.get(i).toTagStructure();
		}

		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.STRUCT, null, t), FinishTag.INST});
	}

	/**
	 * @return the spawnMarker
	 */
	public List<SpawnMarker> getSpawnMarker() {
		return spawnMarkers;
	}

	public void removeAll() {
		spawnMarkers.clear();
	}

}
