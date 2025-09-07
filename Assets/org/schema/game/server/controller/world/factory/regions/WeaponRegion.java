package org.schema.game.server.controller.world.factory.regions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.element.ControlledElementContainer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.world.factory.regions.hooks.RegionHook;

public class WeaponRegion extends UsableRegion {

	Vector3i tmp = new Vector3i();
	public WeaponRegion(Vector3i controllerPosition, Region[] regions, Vector3i min, Vector3i max,
	                    int priority, int orientation, byte controlBlockOrientation) {
		super(controllerPosition, regions, min, max, priority, orientation, controlBlockOrientation);

	}

	@Override
	public RegionHook<?> createHook(Segment lastCreated) {
		return null;
	}

	@Override
	public boolean hasHook() {
		return false;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		if (pos.equals(controllerPosition)) {
			//connect Controller to core
			getcMap().addDelayed(new ControlledElementContainer(
					ElementCollection.getIndex(Ship.core),
					ElementCollection.getIndex(pos),
					ElementKeyMap.WEAPON_CONTROLLER_ID,
					true, true));

			return ElementKeyMap.WEAPON_CONTROLLER_ID;

		} else {
			tmp.set(controllerPosition);
			Vector3i dir = Element.DIRECTIONSi[getBlockOrientation(controllerPosition)];
			tmp.add(dir);

			if ((dir.x > 0 && pos.x > controllerPosition.x) || (dir.x < 0 && pos.x < controllerPosition.x) ||
					(dir.y > 0 && pos.y > controllerPosition.y) || (dir.y < 0 && pos.y < controllerPosition.y) ||
					(dir.z > 0 && pos.z > controllerPosition.z) || (dir.z < 0 && pos.z < controllerPosition.z)
					) {
				//not part of weapon
				return Element.TYPE_ALL;
			} else {
				tmp.set(controllerPosition);
				tmp.sub(dir);

				getcMap().addDelayed(new ControlledElementContainer(
						ElementCollection.getIndex(controllerPosition),
						ElementCollection.getIndex(pos), ElementKeyMap.WEAPON_ID, true, true));

				return ElementKeyMap.WEAPON_ID;
			}

		}

	}

}
