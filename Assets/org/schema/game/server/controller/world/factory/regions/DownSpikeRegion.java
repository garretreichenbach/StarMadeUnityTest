package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Universe;

public class DownSpikeRegion extends Region {

	int count = 20;
	int minLength = 5;
	int maxLength = 25;
	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private Vector3f d = new Vector3f();
	private Vector3f d2 = new Vector3f();
	private Vector3i[] random;

	public DownSpikeRegion(Region[] regions, Vector3i min, Vector3i max,
	                       int priority, int orientation) {
		super(regions, min, max, priority, orientation);
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		getRelativePos(pos, relPos, true);
		getRelativePos(max, relMax, true);
		getRelativePos(min, relMin, true);

		relBB(relMin, relMax);

		if (random == null) {
			random = new Vector3i[count];

			for (int i = 0; i < random.length; i++) {
				random[i] = new Vector3i(Universe.getRandom().nextInt(relMax.x), minLength + Universe.getRandom().nextInt(maxLength - minLength), Universe.getRandom().nextInt(relMax.z));
			}
		}
		//		if(relPos.x < 0 || relPos.z < 0 || relPos.x >= relMax.x || relPos.z >= relMax.z){
		//			return Element.TYPE_NONE;
		//		}
		for (int i = 0; i < random.length; i++) {
			d.set(relPos.x + 0.5f, 0, relPos.z + 0.5f);
			d2.set(relMax.x / 2f, 0, relMax.z / 2f);

			d.sub(d2);

			float l = d.length();

			if (l < relMax.x / 2f + 0.5f && l > 5.5f && relPos.x == random[i].x && relPos.z == random[i].z && relPos.y > relMax.y - random[i].y) {
					return ElementKeyMap.HULL_ID;
			}
		}

		return Element.TYPE_ALL;

	}

}
