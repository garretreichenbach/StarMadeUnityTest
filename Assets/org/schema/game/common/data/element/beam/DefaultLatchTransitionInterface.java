package org.schema.game.common.data.element.beam;

import java.util.Random;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;

public class DefaultLatchTransitionInterface implements BeamLatchTransitionInterface{

	private final Vector3f toTmp = new Vector3f();
	
	private final SegmentPiece tmp = new SegmentPiece();
	
	@Override
	public <E extends SimpleTransformableSendableObject> SegmentPiece selectNextToLatch(
			BeamState bState,
			short oldType, 
			long firstLatchAbsIndex, 
			long currentAbsIndex, 
			Vector3f from, 
			Vector3f to,
			AbstractBeamHandler<E> abstractBeamHandler, 
			SegmentController hitController) {
		SegmentPiece currentSelect = new SegmentPiece();	
		
		int x = ElementCollection.getPosX(currentAbsIndex);
		int y = ElementCollection.getPosY(currentAbsIndex);
		int z = ElementCollection.getPosZ(currentAbsIndex);

		Vector3f firingDir = new Vector3f(to);
		firingDir.sub(from);
		firingDir.normalize();

		int length = 6;
		//Worst value is 1, best is 0
		float bestBlockValue = 1f;
		Random r = new Random();
		
		for(int l = 1; l <= length; l++) {
			//loop over the 3 dimensions
			for(int c = 0; c < Element.DIRECTIONSi.length; c+= 2) {
				int dX = Element.DIRECTIONSi[c].x*l;
				int dY = Element.DIRECTIONSi[c].y*l;
				int dZ = Element.DIRECTIONSi[c].z*l;
				
				//flip 50% of the time to cover all 6 dimensions
				Vector3i nextDir = new Vector3i(dX, dY, dZ);
				if (r.nextInt(2) == 0) {
					nextDir.scale(-1);
				}
				
				int nX = x+ nextDir.x;
				int nY = y+ nextDir.y;
				int nZ = z+ nextDir.z;
				SegmentPiece p = hitController.getSegmentBuffer().getPointUnsave(nX, nY, nZ, tmp);
				if(p == null) {
					//ignore and continue to the next iteration
					continue;
				}
					
				Vector3f angleVector = new Vector3f(nextDir.x, nextDir.y, nextDir.z);
				float angle = Math.abs(firingDir.dot(angleVector));

				// better block found
				if (bestBlockValue > angle && p.isValid() && p.isAlive()) {
					bestBlockValue = angle;
					currentSelect.setByReference(p);
				}
			}
			//Already found a block
			if(currentSelect.isValid() && currentSelect.isAlive()) {

				currentSelect.getWorldPos(toTmp, abstractBeamHandler.getBeamShooter().getSectorId());
				CubeRayCastResult rayCallback = abstractBeamHandler.testRayOnOvelappingSectors(from, toTmp, bState, abstractBeamHandler.getBeamShooter());

				if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
					DebugLine d = new DebugLine(new Vector3f(from), new Vector3f(toTmp));
					if (rayCallback.hasHit() && rayCallback.getSegment() != null) {
						if (currentSelect.equalsSegmentPos(rayCallback.getSegment(), rayCallback.getCubePos())) {
							d.setColor(new Vector4f(1, 0, 0, 1));
						} else {
							d.setColor(new Vector4f(0, 1, 0, 1));
						}
					} else {
						d.setColor(new Vector4f(0, 1, 1, 1));
					}
					DebugDrawer.lines.add(d);
				}
				//re-assign found block to the one in front of the beam, else damage is dealt behind solid blocks
				if(rayCallback.hasHit() && rayCallback.getSegment() != null 
						&& !currentSelect.equalsSegmentPos(rayCallback.getSegment(), rayCallback.getCubePos())) {
					//don't re-assign here, break latch instead
					if(bState.checkLatchConnection) {
						//detach completely
						return null;
					}
					currentSelect.setByReference(rayCallback.getSegment(), rayCallback.getCubePos());
					break;
				} 
			}
		}
		if(currentSelect.getSegment() != null) {			
			return currentSelect;
		}else {
			return null;
		}
	}

}
