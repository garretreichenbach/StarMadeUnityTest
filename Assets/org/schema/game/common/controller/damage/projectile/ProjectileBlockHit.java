package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SegmentData;

public class ProjectileBlockHit {
	SegmentController objectId;
	public long block;
	Vector3f absPosition = new Vector3f();
	public int localBlock;
	private SegmentData segmentData;
	public SegmentData getSegmentData() {
		if(segmentData != null && segmentData.getSegment().getSegmentData() != segmentData){
			//refresh in case of BitSegmentData being replaced
			segmentData = segmentData.getSegment().getSegmentData();
		}
		return segmentData;
	}
	public void setSegmentData(SegmentData segmentData) {
		this.segmentData = segmentData;
	}
	
	
	
}
