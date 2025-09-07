package org.schema.game.common.data.mission.spawner;

import java.util.List;

import org.schema.game.common.data.mission.spawner.component.SpawnComponent;
import org.schema.game.common.data.mission.spawner.component.SpawnComponentType;
import org.schema.game.common.data.mission.spawner.condition.SpawnCondition;
import org.schema.game.common.data.mission.spawner.condition.SpawnConditionType;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DefaultSpawner implements SpawnerInterface {

	private final List<SpawnCondition> conditions = new ObjectArrayList<SpawnCondition>();
	private final List<SpawnComponent> components = new ObjectArrayList<SpawnComponent>();
	private boolean alive = true;

	public static SpawnerInterface instantiate(Tag tag) {

		DefaultSpawner p = new DefaultSpawner();

		p.fromTagStructure(tag);

		return p;
	}

	@Override
	public boolean canSpawn(SpawnMarker marker) {
		for (SpawnCondition c : conditions) {
			if (!c.isSatisfied(marker)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void spawn(SpawnMarker marker) {
		for (SpawnComponent c : components) {
			c.execute(marker);
		}
	}

	/**
	 * @return the alive
	 */
	@Override
	public boolean isAlive() {
		return alive;
	}

	/**
	 * @param alive the alive to set
	 */
	@Override
	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	@Override
	public List<SpawnComponent> getComponents() {
		return components;
	}

	@Override
	public List<SpawnCondition> getConditions() {
		return conditions;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();

		getConditionFromTag(t[0]);
		getComponentsFromTag(t[1]);
		alive = ((Byte) t[2].getValue()) != 0;

	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				getConditionToTag(),
				getComponentsToTag(),
				new Tag(Type.BYTE, null, alive ? (byte) 1 : (byte) 0),
				FinishTag.INST});
	}

	private Tag getComponentsToTag() {
		Tag[] t = new Tag[components.size() + 1];
		t[t.length - 1] = FinishTag.INST;

		for (int i = 0; i < components.size(); i++) {
			SpawnComponent c = components.get(i);
			t[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.INT, null, c.getType().ordinal()), c.toTagStructure(), FinishTag.INST});
		}
		return new Tag(Type.STRUCT, null, t);
	}

	private Tag getConditionToTag() {
		Tag[] t = new Tag[conditions.size() + 1];
		t[t.length - 1] = FinishTag.INST;

		for (int i = 0; i < conditions.size(); i++) {
			SpawnCondition c = conditions.get(i);
			t[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.INT, null, c.getType().ordinal()), c.toTagStructure(), FinishTag.INST});
		}
		return new Tag(Type.STRUCT, null, t);
	}

	private void getComponentsFromTag(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();

		for (int i = 0; i < t.length - 1; i++) {
			components.add(getComponentInstanceFromTag(t[i]));
		}
	}

	private SpawnComponent getComponentInstanceFromTag(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();

		SpawnComponentType c = SpawnComponentType.values()[(Integer) t[0].getValue()];
		SpawnComponent inst = SpawnComponentType.instantiate(c);
		inst.fromTagStructure(t[1]);
		return inst;
	}

	private SpawnCondition getConditionInstanceFromTag(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();

		SpawnConditionType c = SpawnConditionType.values()[(Integer) t[0].getValue()];
		SpawnCondition inst = SpawnConditionType.instantiate(c);
		inst.fromTagStructure(t[1]);
		return inst;
	}

	private void getConditionFromTag(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();

		for (int i = 0; i < t.length - 1; i++) {
			conditions.add(getConditionInstanceFromTag(t[i]));
		}
	}

}
