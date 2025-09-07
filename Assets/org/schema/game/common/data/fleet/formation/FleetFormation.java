package org.schema.game.common.data.fleet.formation;

import java.util.List;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;

import com.bulletphysics.linearmath.Transform;

public interface FleetFormation {
	
	
	public List<Transform> getFormation(SegmentController flagShip, List<Ship> others, List<Transform> out);
}
