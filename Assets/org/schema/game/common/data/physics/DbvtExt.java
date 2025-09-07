package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.Dbvt;
import com.bulletphysics.collision.broadphase.DbvtAabbMm;

public class DbvtExt extends Dbvt {
	Vector3f tmp = new Vector3f();

	@Override
	public boolean update(Node leaf, DbvtAabbMm volume, Vector3f velocity, float margin) {
		if (leaf.volume.Contain(volume)) {
			return false;
		}
		tmp.set(margin, margin, margin);
		volume.Expand(tmp);
		volume.SignedExpand(velocity);
		update(leaf, volume);
		return true;
	}
}
