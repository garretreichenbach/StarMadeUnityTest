package org.schema.game.common.data.fleet.formation;

import java.util.List;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.linearmath.Transform;

public class FleetFormationSpherical implements FleetFormation{

	
	

	void fibonacciSphere(List<Transform> out, float radius) {
		float num = out.size();
		float rnd = 0;//Math.random() * num;
		float offset = 2f / num;
		float increment = FastMath.PI * (3 - FastMath.carmackSqrt(5f));

		for (int i = 0; i < out.size(); i++) {
			float y = (i * offset) - 1f + (offset * 0.5f);
			float r = FastMath.carmackSqrt(1 - FastMath.pow(y, 2));
			float phi = ((i + rnd) % out.size()) * increment;

			float x = FastMath.cosFast(phi) * r;
			float z = FastMath.sinFast(phi) * r;

			
			out.get(i).origin.set(x * radius, y * radius, z * radius);
			
		}
	}
	
	@Override
	public List<Transform> getFormation(SegmentController flagShip, List<Ship> others,
			List<Transform> out) {
		
		if(flagShip.railController.isDockedAndExecuted()){
			return null;
		}
		
		BoundingBox flagShipBB = new BoundingBox();
		flagShip.getPhysicsDataContainer().getShape().getAabb(TransformTools.ident, flagShipBB.min, flagShipBB.max);
		
		float largest = 0;
		for(Ship o : others){
			BoundingBox flagShipOO = new BoundingBox();
			if(o.getPhysicsDataContainer().getObject() != null){
				o.getPhysicsDataContainer().getShape().getAabb(TransformTools.ident, flagShipOO.min, flagShipOO.max);
			}
			float maxSize = flagShipOO.maxSize();
			if(maxSize > largest ){
				largest = maxSize;
			}
		}
		
		fibonacciSphere(out, (flagShipBB.maxSize() + largest*2f) * 1.7f);
		
		
		
		return out;
	}
	
	
}
