package org.schema.game.common.controller.rails;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Matrix4f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.rails.RailRelation.DockingPermission;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.linearmath.Transform;

public class RailRequest implements SerializationInterface {
	/**
	 * railContact is the position of the docker from the view of its mother
	 */
	public final Vector3i railDockerPosOnRail = new Vector3i();
	public VoidUniqueSegmentPiece rail;
	public VoidUniqueSegmentPiece docked;
	public Transform turretTransform = new Transform();
	public Transform movedTransform = new Transform();
	public Vector3i railMovingToDockerPosOnRail;
	public boolean sentFromServer;
	public boolean disconnect;
	public Transform railMovingLocalAtDockTransform = new Transform();
	public boolean didRotationInPlace;
	public DockingPermission dockingPermission;
	public long executionTime;
	public boolean fromtag;
	public boolean ignoreCollision;

	public RailRequest() {
		turretTransform.setIdentity();
		movedTransform.setIdentity();
		railMovingLocalAtDockTransform.setIdentity();
	}

	public static RailRequest readFromTag(Tag from, int shift, boolean resetMove) {
		RailRequest r = new RailRequest();
		r.readTag(from, shift, resetMove);
		return r;

	}

	public void readTag(Tag from, int shift, boolean resetMove) {
		if (from.getType() == Type.STRUCT) {
			Tag[] v = from.getStruct();
			rail = SegmentPiece.getFromUniqueTag(v[0], shift);
			docked = SegmentPiece.getFromUniqueTag(v[1], shift);

			turretTransform.set((Matrix4f) v[2].getValue());

			movedTransform.set((Matrix4f) v[3].getValue());
//			assert(false):movedTransform.basis+"\n"+docked.uniqueIdentifierSegmentController+": "+(Matrix4f) v[3].getValue();
			railDockerPosOnRail.set((Vector3i) v[4].getValue());
			railDockerPosOnRail.add(shift, shift, shift);

			railMovingLocalAtDockTransform.set((Matrix4f) v[5].getValue());

			if (v.length > 6 && v[6].getType() == Type.VECTOR3i) {
				if(railMovingToDockerPosOnRail == null){
					railMovingToDockerPosOnRail = new Vector3i();
				}
				//this one is needed!
				//as the that value is a byte if there is no moving to set
				railMovingToDockerPosOnRail.set((Vector3i) v[6].getValue());
				railMovingToDockerPosOnRail.add(shift, shift, shift);
			}
			if (v.length > 7 && v[7].getType() == Type.BYTE) {
				//this one is needed!
				//as the that value is a byte if there is no moving to set
				didRotationInPlace = ((Byte) v[7].getValue() > 0);
			}
			if (v.length > 8 && v[8].getType() == Type.BYTE) {
				byte b = ((Byte) v[8].getValue());
				if (b >= 0) {
					dockingPermission = DockingPermission.values()[b];
				}
			}
			if(resetMove) {
				//set to what the dock is to reset movement
				movedTransform.set(railMovingLocalAtDockTransform);
			}
		} else {
			disconnect = true;
		}
		fromtag = true;
	}

	public Tag getTag() {
		if (disconnect) {
			return new Tag(Type.BYTE, null, (byte) 1);
		} else {

			return new Tag(Type.STRUCT, null, new Tag[]{
					rail.getUniqueTag(),
					docked.getUniqueTag(),
					new Tag(Type.MATRIX4f, null, turretTransform.getMatrix(new Matrix4f())),
					new Tag(Type.MATRIX4f, null, movedTransform.getMatrix(new Matrix4f())),
					new Tag(Type.VECTOR3i, null, railDockerPosOnRail),
					new Tag(Type.MATRIX4f, null, railMovingLocalAtDockTransform.getMatrix(new Matrix4f())),
					railMovingToDockerPosOnRail != null ? new Tag(Type.VECTOR3i, null, railMovingToDockerPosOnRail) : new Tag(Type.BYTE, null, (byte) 0),
					new Tag(Type.BYTE, null, didRotationInPlace ? (byte) 1 : (byte) 0),
					new Tag(Type.BYTE, null, dockingPermission != null ? (byte) dockingPermission.ordinal() : (byte) -1),
					FinishTag.INST});
		}
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeBoolean(disconnect);

		if (!disconnect) {
			rail.serialize(b);
			docked.serialize(b);

			TransformTools.serializeFully(b, turretTransform);
			TransformTools.serializeFully(b, movedTransform);
			TransformTools.serializeFully(b, railMovingLocalAtDockTransform);

			b.writeShort(railDockerPosOnRail.x);
			b.writeShort(railDockerPosOnRail.y);
			b.writeShort(railDockerPosOnRail.z);
			if (railMovingToDockerPosOnRail != null) {
				b.writeBoolean(true);
				b.writeShort(railMovingToDockerPosOnRail.x);
				b.writeShort(railMovingToDockerPosOnRail.y);
				b.writeShort(railMovingToDockerPosOnRail.z);
			} else {
				b.writeBoolean(false);
			}

			if (dockingPermission != null) {
				b.writeByte((byte) dockingPermission.ordinal());
			} else {
				b.writeByte(-1);
			}
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {

		disconnect = b.readBoolean();

		if (!disconnect) {

			rail = new VoidUniqueSegmentPiece();
			docked = new VoidUniqueSegmentPiece();
			rail.deserialize(b);
			docked.deserialize(b);

			TransformTools.deserializeFully(b, turretTransform);
			TransformTools.deserializeFully(b, movedTransform);
			TransformTools.deserializeFully(b, railMovingLocalAtDockTransform);

			railDockerPosOnRail.x = b.readShort();
			railDockerPosOnRail.y = b.readShort();
			railDockerPosOnRail.z = b.readShort();

			if (b.readBoolean()) {
				railMovingToDockerPosOnRail = new Vector3i();
				railMovingToDockerPosOnRail.x = b.readShort();
				railMovingToDockerPosOnRail.y = b.readShort();
				railMovingToDockerPosOnRail.z = b.readShort();
			}
			byte railPermissionByte = b.readByte();
			if (railPermissionByte >= 0) {
				dockingPermission = DockingPermission.values()[railPermissionByte];
			}
		}
	}

	

	@Override
	public String toString() {
		return toNiceString();
//		"RailRequest [railDockerPosOnRail=" + railDockerPosOnRail
//				+ ", rail=" + rail + ", docked=" + docked
//				+ ", turretTransform=" + turretTransform + ", movedTransform="
//				+ movedTransform + ", railMovingToDockerPosOnRail="
//				+ railMovingToDockerPosOnRail + ", sentFromServer="
//				+ sentFromServer + ", disconnect=" + disconnect
//				+ ", railMovingLocalAtDockTransform="
//				+ railMovingLocalAtDockTransform + ", didRotationInPlace="
//				+ didRotationInPlace + ", dockingPermission="
//				+ dockingPermission + ", executionTime=" + executionTime
//				+ ", fromtag=" + fromtag + ", ignoreCollision="
//				+ ignoreCollision + "]";
	}

	public String toNiceString() {
		assert(disconnect || docked != null):toString()+"; ";
		assert(disconnect || rail != null):toString()+"; ";
		return "("+(disconnect ? "Disconnect" : "Connect")+"Request)"+(!disconnect ? (": ["+
		(docked.getSegmentController() != null ? docked.getSegmentController() : "UNLOADED: "+docked.uniqueIdentifierSegmentController)
		+" "+docked.getAbsolutePos(new Vector3i())+" "+ElementKeyMap.toString(docked.getType())+"] -> ["+
		(rail.getSegmentController() != null ? docked.getSegmentController() : "UNLOADED: "+rail.uniqueIdentifierSegmentController)
		+" "+rail.getAbsolutePos(new Vector3i())+" "+ElementKeyMap.toString(rail.getType())+"]") : "");
	}

}
