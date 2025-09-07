package org.schema.game.common.data.physics;

import java.util.Comparator;

public class PComp implements Comparator<Vector3fb> {

	Vector3fb cur = new Vector3fb();
	Vector3fb p1 = new Vector3fb();
	Vector3fb p2 = new Vector3fb();
	public PComp(Vector3fb cur) {
		this.cur.set(cur);
	}

	@Override
	public int compare(Vector3fb o1, Vector3fb o2) {
		p1.sub(o1, cur);
		p2.sub(o2, cur);

		return Float.compare(p1.lengthSquared(), p2.lengthSquared());
	}

}