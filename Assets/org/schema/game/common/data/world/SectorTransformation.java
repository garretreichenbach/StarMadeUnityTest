package org.schema.game.common.data.world;

import com.bulletphysics.linearmath.Transform;

public class SectorTransformation {
	public final Transform t;
	public final int sectorId;
	public SectorTransformation(Transform t, int sectorId) {
		super();
		this.t = t;
		this.sectorId = sectorId;
	}
	@Override
	public String toString() {
		return "SectorTransformation [t=" + t.origin + ", sectorId=" + sectorId + "]";
	}
	
	
}
