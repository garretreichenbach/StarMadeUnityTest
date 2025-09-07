package org.schema.game.common.data.mission.spawner.component;

public enum SpawnComponentType {
	CREATURE(SpawnComponentCreature.class),
	META_ITEM(SpawnComponentMetaItem.class),
	BLOCKS(SpawnComponentBlocks.class),
	DESTROY_SPAWNER_AFTER_COUNT(SpawnComponentDestroySpawnerAfterCount.class),;
	private final Class<? extends SpawnComponent> clazz;

	private SpawnComponentType(Class<? extends SpawnComponent> clazz) {
		this.clazz = clazz;
	}

	public static SpawnComponent instantiate(SpawnComponentType type) {
		try {
			SpawnComponent newInstance = type.clazz.newInstance();
			assert (newInstance.getType() == type) : type + "; " + type.clazz + " instantiated " + newInstance.getType() + "; " + newInstance.getClass();
			return newInstance;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
