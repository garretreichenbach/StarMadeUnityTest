package org.schema.game.common.data.fleet.formation;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class FleetFormationCallback implements FleetFormation{
	LongOpenHashSet taken = new LongOpenHashSet();
	@Override
	public List<Transform> getFormation(SegmentController flagShip, List<Ship> others,
			List<Transform> out) {
		
		
		for(int i = 0; i < others.size(); i++){
			Ship ship = others.get(i);
			
			
			if(ship.lastPickupAreaUsed != Long.MIN_VALUE && !ship.getManagerContainer().getRailBeam().getRailDockers().isEmpty() && !taken.contains(ship.lastPickupAreaUsed)){
				taken.add(ship.lastPickupAreaUsed);
				Vector3i v = ElementCollection.getPosFromIndex(ship.lastPickupAreaUsed, new Vector3i());
				v.x -= SegmentData.SEG_HALF;
				v.y -= SegmentData.SEG_HALF;
				v.z -= SegmentData.SEG_HALF;
				
				long docker = ship.getManagerContainer().getRailBeam().getRailDockers().iterator().nextLong();
				
				Vector3i d = ElementCollection.getPosFromIndex(docker, new Vector3i());
				d.x -= SegmentData.SEG_HALF;
				d.y -= SegmentData.SEG_HALF;
				d.z -= SegmentData.SEG_HALF;
				
				
				out.get(i).setIdentity();
				out.get(i).origin.set(v.x - d.x, v.y - d.y, v.z - d.z);
			}else{
				out.set(i, null);
			}
			
		}
		
		taken.clear();
		return out;
	}
	
	
}
