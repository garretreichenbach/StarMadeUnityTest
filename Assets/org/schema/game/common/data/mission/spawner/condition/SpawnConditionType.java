package org.schema.game.common.data.mission.spawner.condition;

public enum SpawnConditionType {
	TIME(SpawnConditionTime.class),
	PLAYER_PROXIMITY(SpawnConditionPlayerProximity.class),
	POS_NON_BLOCKED(SpawnConditionPlayerProximity.class),
	CREATURE_COUNT_ON_AFFINITY(SpawnConditionCreatureCountOnAffinity.class),
	CREATURE_COUNT_IN_SECTOR(SpawnConditionCreatureCountOnAffinity.class),;
	private final Class<? extends SpawnCondition> clazz;

	private SpawnConditionType(Class<? extends SpawnCondition> clazz) {
		this.clazz = clazz;
	}

	public static SpawnCondition instantiate(SpawnConditionType type) {
		try {
			SpawnCondition newInstance = type.clazz.newInstance();
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
