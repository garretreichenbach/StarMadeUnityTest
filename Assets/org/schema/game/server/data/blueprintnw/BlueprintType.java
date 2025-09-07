package org.schema.game.server.data.blueprintnw;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.FloatingRockManaged;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.SpawnInterface;
import org.schema.game.server.data.blueprint.AsteroidOutline;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.PlanetOutline;
import org.schema.game.server.data.blueprint.ShipOutline;
import org.schema.game.server.data.blueprint.ShopOutline;
import org.schema.game.server.data.blueprint.SpaceStationOutline;

public enum BlueprintType {
	SHIP(EntityType.SHIP, Ship.class, ShipOutline::new),
	SHOP(EntityType.SHOP, ShopSpaceStation.class, ShopOutline::new),
	SPACE_STATION(EntityType.SPACE_STATION, SpaceStation.class, SpaceStationOutline::new),
	MANAGED_ASTEROID(EntityType.ASTEROID_MANAGED, FloatingRockManaged.class, SpaceStationOutline::new),
	ASTEROID(EntityType.ASTEROID, FloatingRock.class, AsteroidOutline::new),
	PLANET(EntityType.PLANET_SEGMENT, Planet.class, PlanetOutline::new),;

	public SpawnInterface iFace;
	private Class<? extends SegmentController> clazz;
	public final EntityType type;

	private BlueprintType(EntityType type, Class<? extends SegmentController> clazz, SpawnInterface iFace) {
		this.clazz = clazz;
		this.iFace = iFace;
		this.type = type;
	}

	public static BlueprintType getType(Class<? extends SegmentController> c) {
		for (int i = 0; i < values().length; i++) {
			if (values()[i].clazz == c) {
				return values()[i];
			}
		}
		throw new NullPointerException("SegControllerType Not Found: " + c.getClass()+"; "+c);
	}

	public boolean enemySpawnable() {
		return this == SHIP;
	}
}
