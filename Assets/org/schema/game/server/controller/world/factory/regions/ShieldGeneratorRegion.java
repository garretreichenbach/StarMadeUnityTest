package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class ShieldGeneratorRegion extends Region {

	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private Vector3f d = new Vector3f();
	private Vector3f d2 = new Vector3f();

	public ShieldGeneratorRegion(Region[] regions, Vector3i min, Vector3i max,
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

		d.set(relPos.x + 0.5f, 0, relPos.z + 0.5f);

		d2.set(relMax.x / 2f, 0, relMax.z / 2f);

		d.sub(d2);
		float l = d.length();

		if (l < 1 && relPos.y < relMax.y - 15) {
			return ElementKeyMap.POWER_ID_OLD;
		}

		if (l < relMax.x / 2f + 0.5f && (l > relMax.x / 2f - 1.5f || relPos.y <= 1)) {
			if (relPos.y > 0 && relPos.y % 20 == 0) {
				return ElementKeyMap.HULL_COLOR_BLUE_ID;
			}
			return ElementKeyMap.HULL_COLOR_BLACK_ID;
		}

		return Element.TYPE_NONE;

	}

	private short placeUpperPart() {
		d.set(relPos.x + 0.5f, 0, relPos.z + 0.5f);
		d2.set(relMax.x / 2f, 0, relMax.z / 2f);

		d.sub(d2);
		float l = d.length();

		float roof = 1;
		if (l < 1 && relPos.y < relMax.y - 15) {
			return ElementKeyMap.SHIELD_CAP_ID;
		}

		if (l < relMax.x / 2f + 0.5f && (l > relMax.x / 2f - 1.5f || relPos.y >= relMax.y - roof)) {
			if (relPos.y > 0 && relPos.y % 20 == 0) {
				return ElementKeyMap.HULL_COLOR_BLUE_ID;
			}
			//			else if( relPos.y > 0 && relPos.y % 20 == 10 ){
			//				return ElementKeyMap.HULL_COLOR_YELLOW_ID;
			//			}else if( relPos.y > 0 && relPos.y % 5 == 0 ){
			//				return ElementKeyMap.HULL_COLOR_RED_ID;
			//			}
			return ElementKeyMap.HULL_COLOR_BLACK_ID;
		}

		return Element.TYPE_NONE;
	}

}
