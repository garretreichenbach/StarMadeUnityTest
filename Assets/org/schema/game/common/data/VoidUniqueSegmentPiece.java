package org.schema.game.common.data;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class VoidUniqueSegmentPiece extends VoidSegmentPiece {

	public String uniqueIdentifierSegmentController;
	private SegmentController segmentController;

	public VoidUniqueSegmentPiece() {

	}

	public VoidUniqueSegmentPiece(SegmentPiece createFrom) {
		super();
		if(createFrom instanceof VoidUniqueSegmentPiece && createFrom.getSegmentController() == null){
			uniqueIdentifierSegmentController = ((VoidUniqueSegmentPiece)createFrom).uniqueIdentifierSegmentController;
		}else{
			uniqueIdentifierSegmentController = createFrom.getSegmentController().getUniqueIdentifier();
			segmentController = createFrom.getSegmentController();
		}
		createFrom.getAbsolutePos(voidPos);
		setDataByReference(createFrom.getData());
	}

	@Override
	public void serialize(DataOutput w) throws IOException {
		w.writeUTF(uniqueIdentifierSegmentController);
		super.serialize(w);
	}

	@Override
	public void deserialize(DataInput w) throws IOException {
		uniqueIdentifierSegmentController = w.readUTF();
		super.deserialize(w);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SegmentPiece#getAbsoluteIndex()
	 */
	@Override
	public long getAbsoluteIndex() {
		return ElementCollection.getIndex(voidPos);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SegmentPiece#getAbsoluteIndexWithType4()
	 */
	@Override
	public long getAbsoluteIndexWithType4() {
		return ElementCollection.getIndex4(voidPos, getType());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SegmentPiece#getAbsolutePos(org.schema.common.util.linAlg.Vector3i)
	 */
	@Override
	public Vector3i getAbsolutePos(Vector3i out) {
		out.set(voidPos);
		return out;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SegmentPiece#getSegmentController()
	 */
	@Override
	public SegmentController getSegmentController() {
		return this.segmentController;
	}

	/**
	 * @param segmentController the segmentController to set
	 */
	public void setSegmentController(SegmentController segmentController) {
		this.segmentController = segmentController;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof VoidUniqueSegmentPiece) {
			VoidUniqueSegmentPiece s = (VoidUniqueSegmentPiece) obj;
			return s.segmentController == segmentController && s.voidPos.equals(voidPos);
		} else if (obj != null && obj instanceof SegmentPiece) {
			SegmentPiece s = (SegmentPiece) obj;
			return s.getSegmentController() == segmentController && s.equalsPos(voidPos);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SegmentPiece#toString()
	 */
	@Override
	public String toString() {
		return "{UniqueSegPiece " + (segmentController != null ? "[LOADED " + segmentController + "]" : "[UNLOADED: " + uniqueIdentifierSegmentController + "]") + super.toString() + "}";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SegmentPiece#getAbsolutePos(javax.vecmath.Vector3f)
	 */
	@Override
	public Vector3f getAbsolutePos(Vector3f out) {
		out.set(voidPos.x, voidPos.y, voidPos.z);
		return out;
	}

	@Override
	public Tag getUniqueTag() {
		assert(uniqueIdentifierSegmentController != null);
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, tagVersion),
				new Tag(Type.STRING, null, uniqueIdentifierSegmentController),
				new Tag(Type.VECTOR3i, null, voidPos),
				new Tag(Type.SHORT, null, getType()),
				new Tag(Type.BYTE, null, getOrientation()),
				new Tag(Type.BYTE, null, isActive() ? (byte) 1 : (byte) 0),
				new Tag(Type.BYTE, null, (byte) getHitpointsByte()),
				FinishTag.INST});
	}
	public void serializeWithoutUID(DataOutputStream b) throws IOException{
		voidPos.serialize(b);
		b.writeShort(getType());
		b.writeByte(getOrientation());
		b.writeBoolean(isActive());
		b.writeByte((byte)getHitpointsByte());
	}
	public static VoidUniqueSegmentPiece deserizalizeWithoutUID(DataInputStream b) throws IOException{
		VoidUniqueSegmentPiece p = new VoidUniqueSegmentPiece();
		p.voidPos.set(b.readInt(), b.readInt(), b.readInt()); 
		p.setType(b.readShort());
		p.setOrientation(b.readByte());
		p.setActive(b.readBoolean());
		p.setHitpointsByte(b.readByte());
		
		return p;
	}
	public void setSegmentControllerFromUID(StateInterface state) {
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uniqueIdentifierSegmentController);
		if (sendable != null && sendable instanceof SegmentController) {
			segmentController = (SegmentController) sendable;
		}
	}
	@Override
	public boolean equalsUniqueIdentifier(String uid) {
		if(segmentController != null){
			return segmentController.getUniqueIdentifier().equals(uid);
		}else{
			return uniqueIdentifierSegmentController.equals(uid);
		}
	}
	

}
