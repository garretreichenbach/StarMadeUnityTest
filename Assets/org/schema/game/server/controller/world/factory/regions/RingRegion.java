package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector2f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class RingRegion extends Region {

	public static float distUpper = 2;
	Vector2f v = new Vector2f();
	Vector2f v2 = new Vector2f();
	float innerRadius = 5;
	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private float radius;

	public RingRegion(Region[] regions, Vector3i min, Vector3i max,
	                  int priority, int orientation, float radius) {
		super(regions, min, max, priority, orientation);
		this.radius = radius;

	}

	@Override
	public boolean contains(Vector3i pos) {
		if (pos.y >= min.y && pos.y <= max.y) {
			v.x = pos.x;
			v.y = pos.z;
			if (v.length() < radius + innerRadius && v.length() > radius - innerRadius) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		assert (contains(pos));
		if (pos.y > min.y && pos.y < max.y) {

			v.x = pos.x;
			v.y = pos.z;
			if (v.length() < radius + innerRadius && v.length() > radius - innerRadius) {

				if (v.length() < radius + (innerRadius - 2) && v.length() > radius - (innerRadius - 2)) {
					return Element.TYPE_NONE;
				} else {
					return ElementKeyMap.HULL_ID;
				}
			}

		} else {
			if (pos.y == min.y || pos.y == max.y) {

				v.x = pos.x;
				v.y = pos.z;
				if (v.length() < radius + 1 && v.length() > radius - 1) {
					return ElementKeyMap.LIGHT_ID;
				}
				return ElementKeyMap.HULL_ID;
			}
		}

		return Element.TYPE_NONE;

	}

}
