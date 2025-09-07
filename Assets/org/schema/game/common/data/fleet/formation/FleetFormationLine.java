package org.schema.game.common.data.fleet.formation;

import java.util.List;

import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public class FleetFormationLine implements FleetFormation{

	@Override
	public List<Transform> getFormation(SegmentController flagShip, List<Ship> others,
			List<Transform> out) {
		
		if(flagShip.railController.isDockedAndExecuted()){
			return null;
		}
		
		BoundingBox flagShipBB = new BoundingBox();
		flagShip.getPhysicsDataContainer().getShape().getAabb(TransformTools.ident, flagShipBB.min, flagShipBB.max);
		List<Transform> left = new ObjectArrayList<Transform>();
		List<Transform> right = new ObjectArrayList<Transform>();
		float marginPerc = 1.40f;
		for(int i = 0; i < others.size(); i++){
			Ship ship = others.get(i);
			
			List<Transform> dir = left.size() > right.size() ? right : left;
			boolean dirMul = left.size() > right.size();
			
		
			if(ship.getPhysicsDataContainer().getShape() != null){
				
				
				BoundingBox saabb = new BoundingBox(ship.getMinPos(), ship.getMaxPos());
				saabb.min.scale(16);
				saabb.max.scale(16);
				
//				ship.getPhysicsDataContainer().getShape().getAabb(TransformTools.ident, saabb.min, saabb.max);
				
				float margin = (marginPerc * (saabb.maxSize()));
				
				Transform t = new Transform();
				t.setIdentity();
				t.basis.set(flagShip.getWorldTransform().basis);
				float xPos;
				if(dir.isEmpty()){
					
					if(dirMul){
						xPos = (flagShipBB.min.x - Math.abs(saabb.max.x))-margin; 
					}else{
						xPos = (flagShipBB.max.x + Math.abs(saabb.min.x))+margin; 
					}
					
				}else{
					Transform lastDirEnd = dir.get(dir.size()-1);
					if(dirMul){
						xPos = (lastDirEnd.origin.x - Math.abs(saabb.max.x)) - margin; 
					}else{
						xPos = (lastDirEnd.origin.x + Math.abs(saabb.min.x)) + margin; 
					}
				}
				t.origin.x = (xPos);
				
				out.get(i).set(t);
				
				
				
				Transform tEnd = new Transform(t);
//				tEnd.setIdentity();
				if(dirMul){
					tEnd.origin.x -= Math.abs(saabb.min.x); 
				}else{
					tEnd.origin.x += Math.abs(saabb.max.x); 
				}
//				System.err.println("II "+i+" :::: "+t.origin+" ("+margin+") -> end: "+tEnd.origin+"; "+dirMul);
				dir.add(tEnd);
			}
			
			
		}
		
		
		return out;
	}
	
	
}
