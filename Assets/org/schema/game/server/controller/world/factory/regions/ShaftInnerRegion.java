package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector2f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class ShaftInnerRegion extends Region {

	public static float distUpper = 2;
	Vector2f v = new Vector2f();
	Vector2f v2 = new Vector2f();
	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();

	public ShaftInnerRegion(Region[] regions, Vector3i min, Vector3i max,
	                        int priority, int orientation) {
		super(regions, min, max, priority, orientation);
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		getRelativePos(pos, relPos, true);
		getRelativePos(max, relMax, true);
		getRelativePos(min, relMin, true);

		relBB(relMin, relMax);

		//		if(relPos.x < 0 || relPos.z < 0 || relPos.x >= relMax.x || relPos.z >= relMax.z){
		//			return Element.TYPE_NONE;
		//		}
		v.set(relPos.x + 0.5f, relPos.z + 0.5f);
		v2.set(relMax.x / 2, relMax.z / 2);
		v2.sub(v);
		if (v2.length() > relMax.x / 2) {
			//don't place anything
			return Element.TYPE_ALL;
		}
		v.set(relPos.x, relPos.z);
		v2.set(relMax.x / 2, relMax.z / 2);
		if (v.equals(v2)) {
			return ElementKeyMap.LIFT_ELEMENT;
		}

		return Element.TYPE_NONE;

	}

}
