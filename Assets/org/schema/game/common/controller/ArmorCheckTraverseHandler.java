package org.schema.game.common.controller;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.InnerSegmentIterator;
import org.schema.game.common.data.physics.RayTraceGridTraverser;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

import com.bulletphysics.linearmath.Transform;

public class ArmorCheckTraverseHandler extends InnerSegmentIterator{
	

	public ArmorValue armorValue;
	
	@Override
	public boolean handle(int absX, int absY, int absZ, RayTraceGridTraverser traverser) {
		assert(armorValue != null);
		SegmentController controller = getContextObj();

		int x = (absX - currentSeg.pos.x) + SegmentData.SEG_HALF;
		int y = (absY - currentSeg.pos.y) + SegmentData.SEG_HALF;
		int z = (absZ - currentSeg.pos.z) + SegmentData.SEG_HALF;
		if (debug) {
			traverser.drawDebug(absX+SegmentData.SEG_HALF, absY+SegmentData.SEG_HALF, absZ+SegmentData.SEG_HALF, tests, controller.getWorldTransform());
		}
		tests++;
		
		
		SegmentData data0 = currentSeg.getSegmentData();
		short type;
		int infoIndex;
		if (x >= 0 && x < SegmentData.SEG && y >= 0 && y < SegmentData.SEG && z >= 0 && z < SegmentData.SEG ) {
			if((type = data0.getType((infoIndex = SegmentData.getInfoIndex((byte) x, (byte) y, (byte) z)))) > 0 && 
					ElementInformation.isPhysicalRayTests(type, data0, infoIndex) && 
					isZeroHpPhysical(data0, infoIndex)
					) {
				v.elemA.set((byte) x, (byte) y, (byte) z);
				v.elemPosA.set(
						v.elemA.x - SegmentData.SEG_HALF,
						v.elemA.y - SegmentData.SEG_HALF,
						v.elemA.z - SegmentData.SEG_HALF);

				v.elemPosA.x += currentSeg.pos.x;
				v.elemPosA.y += currentSeg.pos.y;
				v.elemPosA.z += currentSeg.pos.z;

				rayResult.collisionObject = collisionObject;
				//the test of the parameters may be set here but not necessary
				
				
				
				
				boolean continueShot = processRawHitUnshielded(currentSeg, infoIndex, type, v.elemA, v.elemPosA, testCubes);
				if(!continueShot) {
					//dont continue with next segment in outer handler only if we didn't hit any armor
					this.hitSignal = true;
				}
				
				return continueShot;
			}else {
				if(armorValue.typesHit.size() > 0) {
					//hit signal here is ok, since we actually want to stop
					this.hitSignal = true;
					//we hit "air". process rest of accumulated
					return false;
				}
			}
		}
		return true;
	}
	private boolean processRawHitUnshielded(Segment currentSeg, int infoIndex, short type, Vector3b segmentPos, Vector3f absolutePos,
			Transform segmentControllerWorldTransform) {
		
		ElementInformation info = ElementKeyMap.getInfoFast(type);
		
		
		armorValue.typesHit.add(info);
		
		armorValue.armorValueAccumulatedRaw += info.getArmorValue() + (info.getArmorValue() * ((float)armorValue.typesHit.size() * VoidElementManager.ARMOR_THICKNESS_BONUS));
		armorValue.armorIntegrity += (float)currentSeg.getSegmentData().getHitpointsByte(infoIndex) * ElementKeyMap.MAX_HITPOINTS_INV;
		
		//preliminary test has already confirmed hit
		return info.isArmor();
	}
}
