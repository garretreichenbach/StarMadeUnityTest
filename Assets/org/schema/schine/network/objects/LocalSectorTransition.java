package org.schema.schine.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;

import com.bulletphysics.linearmath.Transform;

public class LocalSectorTransition implements SerializationInterface{
	public Vector3i oldPos = new Vector3i();
	public Vector3i newPos = new Vector3i();
	public float planetRotation; //((GameStateInterface) state).getGameState().getRotationProgession()
	public boolean oldPosPlanet; //oldSector.getSectorType() == SectorType.PLANET
	public boolean newPosPlanet; //newSector.getSectorType() == SectorType.PLANET
	public float sectorSize; //((GameStateInterface) getState()).getSectorSize()
	
	
	
	public Transform getTransitionTransform(Transform inputWorldTransform){ //o.getWorldTransform()

		Vector3i dir = new Vector3i();
		dir.sub(newPos, oldPos);

		Vector3f otherSecCenter = new Vector3f(
				dir.x * sectorSize,
				dir.y * sectorSize,
				dir.z * sectorSize);

		Transform t = new Transform(inputWorldTransform);
		

		Transform newSectorTrans = new Transform();
		newSectorTrans.setIdentity();
		newSectorTrans.origin.set(otherSecCenter);
		
		Matrix3f rot = new Matrix3f();
		rot.rotX((FastMath.PI * 2) * planetRotation);
		
		if (oldPosPlanet) {

			//we are next to a planet sector
			//-> rotate planet sector
			rot.invert();
			Vector3f bb = new Vector3f(otherSecCenter);
			TransformTools.rotateAroundPoint(bb, rot, newSectorTrans, new Transform());

		} else if (!oldPosPlanet && newPosPlanet) {
			//normal -> planet
			//calculate postion as seen from new sector
			TransformTools.rotateAroundPoint(new Vector3f(), rot, newSectorTrans, new Transform());
		} else {
			newSectorTrans.origin.set(otherSecCenter);
		}
		newSectorTrans.inverse();
		//			t.inverse();
		//new sector's view of the object
		newSectorTrans.mul(t);

		t.set(newSectorTrans);
		
		return t;
	}




	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		oldPos.serialize(b);
		newPos.serialize(b);
		b.writeFloat(planetRotation);
		b.writeBoolean(oldPosPlanet);
		b.writeBoolean(newPosPlanet);
		b.writeFloat(sectorSize);
	}




	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		oldPos.deserialize(b);
		newPos.deserialize(b);
		planetRotation = b.readFloat();
		oldPosPlanet = b.readBoolean();
		newPosPlanet = b.readBoolean();
		sectorSize = b.readFloat();
	}
}
