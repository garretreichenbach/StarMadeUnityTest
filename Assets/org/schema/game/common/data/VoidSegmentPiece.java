package org.schema.game.common.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementCollection;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VoidSegmentPiece extends SegmentPiece implements Comparable<VoidSegmentPiece> {

	public Vector3i voidPos = new Vector3i();
	public int senderId;
	public long controllerPos = Long.MIN_VALUE;
	public boolean onlyHitpointsChanged;
	public boolean onlyActiveChanged;

	@Override
	public int compareTo(VoidSegmentPiece o) {
		long indexA = getSegmentAbsoluteIndex();
		long indexB = o.getSegmentAbsoluteIndex();

		return CompareTools.compare(indexA, indexB);
	}
	
	public void serialize(DataOutput w) throws IOException {
		w.writeInt(voidPos.x);
		w.writeInt(voidPos.y);
		w.writeInt(voidPos.z);

		serializeData(w, getData());
	}

	public void setByValue(VoidSegmentPiece p) {
		this.voidPos.set(p.voidPos);
		this.controllerPos = p.controllerPos;
		this.data = p.data;
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
		this.senderId = p.senderId;
		this.onlyHitpointsChanged = p.onlyHitpointsChanged;
		this.onlyActiveChanged = p.onlyActiveChanged;
		this.setSegment(p.getSegment());
	}

	@Override
	public void reset() {
		super.reset();
		this.senderId = 0;
		this.onlyActiveChanged = false;
		this.onlyHitpointsChanged = false;
		this.controllerPos = Long.MIN_VALUE;
		this.voidPos.set(0, 0, 0);
	}
	
	/**
	 * Deserializes the data from the given DataInput stream.
	 * <p>This is for deserializing 4-byte segment data.
	 * @param w the DataInput stream to read from
	 * @throws IOException if an I/O error occurs
	 */
	public void deserialize(DataInput w) throws IOException {
		voidPos.set(w.readInt(), w.readInt(), w.readInt());
		setDataByReference(deserializeData(w));
	}

	@Override
	public int hashCode() {
		return voidPos.hashCode();
	}

	public long getAbsoluteIndex() {
		return ElementCollection.getIndex(voidPos);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((SegmentPiece) obj).equalsPos(voidPos);
	}

	@Override
	public boolean equalsPos(Vector3i obj) {
		return voidPos.equals(obj);
	}

	public long getSegmentAbsoluteIndex() {
		return ElementCollection.getIndex(ByteUtil.divUSeg(voidPos.x), ByteUtil.divUSeg(voidPos.y), ByteUtil.divUSeg(voidPos.z));
	}

	public static class VoidSegmentPiecePool {
		private final List<VoidSegmentPiece> pool = new ObjectArrayList<VoidSegmentPiece>();

		public void free(VoidSegmentPiece p) {
			p.reset();
			pool.add(p);
		}

		public VoidSegmentPiece get() {
			if(pool.isEmpty()) {
				return new VoidSegmentPiece();
			} else {
				return pool.remove(pool.size() - 1);
			}
		}
	}

}
