package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class TunnelRegion extends Region {

	public static float distUpper = 2;
	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private Vector3f d = new Vector3f();
	private Vector3f d2 = new Vector3f();

	public TunnelRegion(Region[] regions, Vector3i min, Vector3i max,
	                    int priority, int orientation) {
		super(regions, min, max, priority, orientation);
	}

	public short getColor1() {
		return ElementKeyMap.HULL_COLOR_BLACK_ID;
	}

	public short getColor2() {
		return ElementKeyMap.HULL_COLOR_RED_ID;
	}

	public short getColor3() {
		return ElementKeyMap.HULL_COLOR_GREEN_ID;
	}

	public short getHull() {
		return ElementKeyMap.HULL_ID;
	}

	public short getLight() {
		return ElementKeyMap.LIGHT_ID;
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
		if (relPos.y < relMax.y / 2 + distUpper) {
			return placeDownPart();
		} else {
			return placeUpperPart();
		}

	}

	private short placeDownPart() {
		short normal = getHull();

		if (relPos.y == relMax.y / 2) {
			normal = getColor1();
		} else if (relPos.y == relMax.y / 2 + 1 || relPos.y == relMax.y / 2 - 1) {
			normal = getColor2();
		} else if (relPos.z % 8 == 0) {
			if (relPos.y == 2) {
				normal = getLight();
			} else {
				normal = getColor3();
			}
		}

		if (relPos.y == 0 || relPos.y == relMax.y - 1) {
			if (relPos.y == 0 && relPos.x > 0 && relPos.x % 10 == 0 && relPos.z % 5 == 0) {
				return getLight();
			} else {
				return getHull();
			}
		}
		if (relPos.x == relMax.x - 1 || relPos.x == 0) {
			return normal;
		}

		return Element.TYPE_NONE;
	}

	private short placeUpperPart() {
		d.set(relPos.x + 0.5f, relPos.y * 1.3f, 0);
		d2.set(relMax.x / 2f, relMax.y / 2f + distUpper, 0);

		d.sub(d2);
		float l = d.length();
		if (l < relMax.x / 2f + 0.5f && (l > relMax.x / 2f - 1.5f || relPos.y == relMax.y - 1 || relPos.y == relMax.y - 2 || relPos.z == relMax.z - 1)) {
			return getHull();
		}

		return Element.TYPE_NONE;
	}

}
