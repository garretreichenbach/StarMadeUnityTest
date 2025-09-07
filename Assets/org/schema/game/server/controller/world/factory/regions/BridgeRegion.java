package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class BridgeRegion extends Region {

	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private Vector3f d = new Vector3f();
	private Vector3f d2 = new Vector3f();

	public BridgeRegion(Region[] regions, Vector3i min, Vector3i max,
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
		if (relPos.y < relMax.y / 2) {
			return placeDownPart();
		} else {
			return placeUpperPart();
		}

	}

	private short placeDownPart() {
		short normal = ElementKeyMap.HULL_ID;

		d.set(relPos.x + 0.5f, 0, relPos.z + 0.5f);

		d2.set(relMax.x / 2f, 0, relMax.z / 2f);

		d.sub(d2);
		float l = d.length();
		if (l >= relMax.x / 2f) {
			return Element.TYPE_ALL;
		}

		int uSize = relMax.y / 2 - 4;
		assert (uSize > 0);
		if (relPos.y == uSize) {
			if (relPos.x > 0 && relPos.z > 0 && relPos.x % 5 == 0 && relPos.z % 5 == 0) {
				return ElementKeyMap.HULL_COLOR_YELLOW_ID;
			} else {
				return ElementKeyMap.HULL_ID;
			}
		} else if (relPos.y < uSize) {
			int i = relPos.y - uSize;
			short t = ElementKeyMap.HULL_ID;
			if (i % 2 == 0) {
				t = ElementKeyMap.HULL_COLOR_BLACK_ID;
			}

			if (relPos.x < relMax.x - i && relPos.x > i && relPos.z < relMax.z - i && relPos.z > i) {

				if (relPos.y == 0) {
					for (int j = 0; j < relMax.x / 8; j++) {
						float d = j * 8.0f;
						if (l > d && l < d + 1.5f) {
							t = ElementKeyMap.HULL_COLOR_RED_ID;
							break;
						}
					}
				}

				return t;
			}
		}
		if (l + 1.5f >= (float) relMax.x / 2) {
			if (relPos.y == uSize + 2) {
				return ElementKeyMap.GLASS_ID;
			}
			if (relPos.y == uSize + 3) {
				return ElementKeyMap.HULL_COLOR_RED_ID;
			}
			return normal;
		}

		return Element.TYPE_ALL;
	}

	private short placeUpperPart() {
		d.set(relPos.x + 0.5f, relPos.y * 1.3f, relPos.z + 0.5f);
		d2.set(relMax.x / 2f, relMax.y / 2f, relMax.z / 2f);

		d.sub(d2);
		float l = d.length();

		float roof = 4;
		if (l < relMax.x / 2f + 0.5f && relPos.y == (int) (relMax.y - roof - 1)) {

			if (relPos.x > 0 && relPos.z > 0 && relPos.x % 10 == 0 && relPos.z % 10 == 0) {
				return ElementKeyMap.LIGHT_ID;
			}
		}

		if (l < relMax.x / 2f + 0.5f && (l > relMax.x / 2f - 1.5f || relPos.y >= relMax.y - roof || relPos.z == relMax.z - 1)) {
			if (relPos.y < relMax.y - roof) {
				return ElementKeyMap.GLASS_ID;
			}
			return ElementKeyMap.HULL_ID;
		}

		return Element.TYPE_NONE;
	}

}
