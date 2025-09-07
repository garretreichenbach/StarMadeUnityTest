package org.schema.game.common.data;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.*;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class SegmentPiece {

	public byte x;
	public byte y;
	;
	public byte z;
	public boolean forceClientSegmentAdd;
	protected int data;
	private Segment segment;
	protected static final byte tagVersion = 0;

	public SegmentPiece() {
	}
	
	/**
	 * Deserializes the data from the given DataInput stream.
	 * <p>This is for deserializing 4-byte segment data.
	 * @param w the DataInput stream to read from
	 * @throws IOException if an I/O error occurs
	 */
	public static int deserializeData(DataInput w) throws IOException {
		return w.readInt();
	}

	public static void serializeData(DataOutput buffer, int data) throws IOException {
		buffer.writeInt(data);
	}

	public SegmentPiece(Segment segment, int infoIndex) {
		this(segment, SegmentData.getPosXFromIndex(infoIndex), SegmentData.getPosYFromIndex(infoIndex), SegmentData.getPosZFromIndex(infoIndex));
	}

	public void setByReference(Segment currentSeg, int infoIndex) {
		setByReference(currentSeg, SegmentData.getPosXFromIndex(infoIndex), SegmentData.getPosYFromIndex(infoIndex), SegmentData.getPosZFromIndex(infoIndex));
	}

	public SegmentPiece(Segment segment, byte x, byte y, byte z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.segment = segment;
		if(segment == null || segment.isEmpty() || segment.getSegmentData() == null) {
			//don't bother to copy
		} else {
			this.data = segment.getSegmentData().getDataAt(SegmentData.getInfoIndex(x, y, z));
		}
	}

	public SegmentPiece(SegmentPiece p) {
		this(p.segment, p.x, p.y, p.z);
	}

	public void setByReference(SegmentPiece p) {
		setByReference(p.segment, p.x, p.y, p.z);
	}

	public SegmentPiece(Segment segment, Vector3b pos) {
		setByReference(segment, pos);
	}

	public SegmentPiece(Segment segment, Vector3b pos, int data) {
		this.segment = segment;
		this.setPos(pos);
		this.data = data;
	}

	public synchronized static float getWorldDistance(SegmentPiece harvester,
	                                                  SegmentPiece harvestee) {

		Vector3i outA = new Vector3i();
		Vector3i outB = new Vector3i();
		Vector3f A = new Vector3f();
		Vector3f B = new Vector3f();

		harvester.getAbsolutePos(outA);
		harvestee.getAbsolutePos(outB);

		A.set(outA.x * Element.BLOCK_SIZE, outA.y * Element.BLOCK_SIZE, outA.z * Element.BLOCK_SIZE);
		B.set(outB.x * Element.BLOCK_SIZE, outB.y * Element.BLOCK_SIZE, outB.z * Element.BLOCK_SIZE);
		harvester.segment.getSegmentController().getWorldTransform().basis.transform(A);
		harvestee.segment.getSegmentController().getWorldTransform().basis.transform(B);

		A.add(harvester.segment.getSegmentController().getWorldTransform().origin);
		B.add(harvestee.segment.getSegmentController().getWorldTransform().origin);

		A.sub(B);

		return A.length();
	}

	public static VoidUniqueSegmentPiece getFromUniqueTag(Tag from, int shift) {

		Tag[] v = (Tag[]) from.getValue();
		VoidUniqueSegmentPiece sp = new VoidUniqueSegmentPiece();

		if(v[0].getType() == Type.BYTE) {
			byte version = v[0].getByte();
			sp.uniqueIdentifierSegmentController = (String) v[1].getValue();
			sp.voidPos.set((Vector3i) v[2].getValue());
//			sp.voidPos.add(shift, shift, shift); //no migration needed as its already guaranteed to be saved in chunk32
			sp.setType((Short) v[3].getValue());
			sp.setOrientation((Byte) v[4].getValue());
			sp.setActive((Byte) v[5].getValue() > 0);
			sp.setHitpointsByte(v[6].getByte());
		} else {
			sp.uniqueIdentifierSegmentController = (String) v[0].getValue();
			sp.voidPos.set((Vector3i) v[1].getValue());
			sp.voidPos.add(shift, shift, shift);
			sp.setType((Short) v[2].getValue());
			sp.setOrientation((Byte) v[3].getValue());
			sp.setActive((Byte) v[4].getValue() > 0);
			sp.setHitpointsByte(v[5].getByte());

			SegmentData3Byte.migratePiece(sp);
		}

		return sp;

	}

	public void reset() {
		this.data = 0;
		this.segment = null;
		this.forceClientSegmentAdd = false;
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public boolean equalsPos(Vector3i obj) {
		return segment.equalsAbsoluteElemPos(obj, x, y, z);
	}

	public boolean equalsPos(final int x, final int y, final int z) {
		return ElementCollection.getIndex(x, y, z) == getAbsoluteIndex();
	}

	public long getAbsoluteIndex() {
		return segment.getAbsoluteIndex(x, y, z);
	}

	public long getAbsoluteIndexWithType4() {
		return ElementCollection.getIndex4(segment.getAbsoluteIndex(x, y, z), getType());
	}

	public long getTextBlockIndex() {
		return ElementCollection.getIndex4(segment.getAbsoluteIndex(x, y, z), getOrientation());

	}

	public Vector3i getAbsolutePos(Vector3i out) {
		return segment != null ? segment.getAbsoluteElemPos(x, y, z, out) : (this instanceof VoidSegmentPiece ? ((VoidSegmentPiece) this).voidPos : null);
	}

	public Vector3f getAbsolutePos(Vector3f out) {
		if(segment != null) {
			return segment.getAbsoluteElemPos(x, y, z, out);
		} else if(this instanceof VoidSegmentPiece) {
			out.set(((VoidSegmentPiece) this).voidPos.x, ((VoidSegmentPiece) this).voidPos.y, ((VoidSegmentPiece) this).voidPos.z);
			return out;
		} else {
			return null;
		}
	}

	public byte getCornerSegmentsDiffs() {
		byte b = 0;
		if(x == 0) {
			b += Element.FLAG_LEFT;
		} else if(x == SegmentData.SEG_MINUS_ONE) {
			b += Element.FLAG_RIGHT;
		}
		if(y == 0) {
			b += Element.FLAG_BOTTOM;
		} else if(y == SegmentData.SEG_MINUS_ONE) {
			b += Element.FLAG_TOP;
		}
		if(z == 0) {
			b += Element.FLAG_BACK;
		} else if(z == SegmentData.SEG_MINUS_ONE) {
			b += Element.FLAG_FRONT;
		}
		return b;
	}

	public int getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setDataByReference(int data) {
		this.data = data;
	}

	public void setType(short newType) {
		data = (data & SegmentData4Byte.typeMaskNot) | newType;
	}

	public void setHitpointsFull(int value) {
		setHitpointsByte((int) (value * getInfo().getMaxHitpointsFullToByte()));
	}

	public void setHitpointsByte(int value) {
		assert (value >= 0 && value < 128) : value;
		data = (data & SegmentData4Byte.hpMaskNot) | (value << SegmentData4Byte.hpIndexStart);
	}

	public void setActive(boolean active) {
		data = active ? (data | SegmentData4Byte.activationMask) : (data & SegmentData4Byte.activationMaskNot);
	}

	public void setOrientation(byte value) {
		assert (value >= 0 && value < 32) : "NOT A SIDE INDEX";
		data = (data & SegmentData4Byte.orientationMaskNot) | (value << SegmentData4Byte.orientationIndexStart);
	}

	public short getType() {
		return getType(data);
	}

	public static short getType(int data) {
		return (short) (SegmentData4Byte.typeMask & data);
	}

	public int getHitpointsFull() {
		if(getType() != 0) {
			return getInfo().convertToFullHp(getHitpointsByte());
		} else {
			return getHitpointsByte();
		}
	}

	public short getHitpointsByte() {
		return (short) ((SegmentData4Byte.hpMask & data) >> SegmentData4Byte.hpIndexStart);
	}

	public byte getOrientation() {
		return (byte) ((SegmentData4Byte.orientationMask & data) >> SegmentData4Byte.orientationIndexStart);
	}

	public boolean isActive() {
		return (SegmentData4Byte.activationMask & data) > 0;
	}

	public Vector3b getPos(Vector3b pos) {
		pos.set(x, y, z);
		return pos;
	}

	public Segment getSegment() {
		return segment;
	}

	/**
	 * @param segment the segment to set
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public SegmentController getSegmentController() {
		return segment.getSegmentController();
	}

	public void getTransform(Transform out) {
		out.set(segment.getSegmentController().getWorldTransform());
		Vector3i absolutePos = getAbsolutePos(new Vector3i());
		Vector3f v = new Vector3f(absolutePos.x - SegmentData.SEG_HALF, absolutePos.y - SegmentData.SEG_HALF, absolutePos.z - SegmentData.SEG_HALF);
		out.basis.transform(v);
		out.origin.add(v);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return segment.pos.hashCode() + posHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof SegmentPiece) {
			SegmentPiece s = (SegmentPiece) obj;
			return s.getSegmentController() == getSegmentController() && s.segment.pos.equals(segment.pos) && s.x == x && s.y == y && s.z == z;
		}
		return false;
	}

	//	public String toString(){
	//		return "Modify(Seg: "+segment.pos+", Pos "+pos+", type: "+getType()+")";
	//	}
	@Override
	public String toString() {
		return getAbsolutePos(new Vector3i()).toString() + "[" +
				(getType() != 0 ? ElementKeyMap.getInfo(getType()).getName() : "NONE") + "]o[" +
				Element.getSideString(getOrientation()) + "][" + (isActive() ? "active" : "inactive") + "][" + getHitpointsByte() + "Bhp; " + getHitpointsFull() + " Fhp]" + getBlockStyleString();
	}

	public boolean isEdgeOfSegment() {
		return x == 0 || y == 0 || z == 0 || x == SegmentData.SEG_MINUS_ONE || y == SegmentData.SEG_MINUS_ONE || z == SegmentData.SEG_MINUS_ONE;
	}

	public void populate(Segment segment, byte x, byte y, byte z) {
		this.segment = segment;
		this.x = x;
		this.y = y;
		this.z = z;
		if(segment.isEmpty() || segment.getSegmentData() == null) {
			//don't bother to copy
			this.data = 0;
		} else {
			this.data = segment.getSegmentData().getDataAt(SegmentData.getInfoIndex(x, y, z));
		}
	}

	public void populate(Segment segment, Vector3b pos) {
		populate(segment, pos.x, pos.y, pos.z);
	}

	public int posHashCode() {
		long bits = 1L;
		bits = 7L * bits + x;
		bits = 7L * bits + y;
		bits = 7L * bits + z;
		return (byte) (bits ^ (bits >> 8));
	}

	public void refresh() {
		populate(segment, x, y, z);
	}

	public void setByValue(SegmentPiece p) {
		setByReference(p);
		this.data = p.data;
	}

	public void setByReference(Segment segment, byte x, byte y, byte z) {
		this.segment = segment;
		this.setPos(x, y, z);

		if(segment.isEmpty()) {
			//don't bother to copy
			data = 0;
		} else {
			this.data = segment.getSegmentData().getDataAt(SegmentData.getInfoIndex(x, y, z));
		}
	}

	public void setByReference(Segment segment, Vector3b pos) {
		setByReference(segment, pos.x, pos.y, pos.z);
	}

	public void setPos(byte x, byte y, byte z) {
		this.x = x;
		this.y = y;
		this.z = z;

	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vector3b pos) {
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
	}

	public void setWithoutData(Segment segment, Vector3b pos) {
		this.segment = segment;
		this.setPos(pos);
	}

	public String getBlockStyleString() {
		if(ElementKeyMap.isValidType(getType())) {
			ElementInformation info = ElementKeyMap.getInfo(getType());
			if(info.getBlockStyle() == BlockStyle.NORMAL) {
				return "[BLOCK]";
			} else {

				return "[" + BlockShapeAlgorithm.getAlgo(info.getBlockStyle(), getOrientation()).getClass().getAnnotation(BlockShape.class).name() + "]";
			}
		}
		return "";
	}

	public int getInfoIndex() {
		return SegmentData.getInfoIndex(x, y, z);
	}

	public Tag getUniqueTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, tagVersion),
				new Tag(Type.STRING, null, getSegmentController().getUniqueIdentifier()),
				new Tag(Type.VECTOR3i, null, getAbsolutePos(new Vector3f())),
				new Tag(Type.SHORT, null, getType()),
				new Tag(Type.BYTE, null, getOrientation()),
				new Tag(Type.BYTE, null, isActive() ? (byte) 1 : (byte) 0),
				new Tag(Type.BYTE, null, (byte) getHitpointsByte()),
				FinishTag.INST});
	}

	public BlockShapeAlgorithm getAlgorithm() {
		return getAlgorithm(getType());
	}

	public BlockShapeAlgorithm getAlgorithm(short asType) {
		if(ElementKeyMap.isValidType(asType) && ElementKeyMap.getInfoFast(asType).getBlockStyle() != BlockStyle.NORMAL) {
			return BlockShapeAlgorithm.getAlgo(ElementKeyMap.getInfoFast(asType).getBlockStyle(), getOrientation());
		} else {
			return null;
		}
	}

	public int getFullOrientation() {
		return getOrientation();
	}

	public void debugDrawPoint(Transform segContrTransform, float extraSize, float r, float g, float b, float a,
	                           long lifetimeMs) {
		Vector3i p = getAbsolutePos(new Vector3i());
		debugDrawPoint(p, segContrTransform, extraSize, r, g, b, a, lifetimeMs);
	}

	public static void debugDrawPoint(Vector3i absPos, Transform segContrTransform, float extraSize, float r, float g, float b, float a,
	                                  long lifetimeMs) {
		Vector3i p = absPos;
		Transform t = new Transform(segContrTransform);
		Vector3f pos = new Vector3f(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
		t.basis.transform(pos);
		t.origin.add(pos);
		DebugBox b1 = new DebugBox(
				new Vector3f((-0.5f - extraSize), (-0.5f - extraSize), (-0.5f - extraSize)),
				new Vector3f((0.5f + extraSize), (0.5f + extraSize), (0.5f + extraSize)),
				new Transform(t),
				r, g, b, a);
		b1.LIFETIME = lifetimeMs;
		DebugDrawer.boxes.add(b1);
	}

	public ElementInformation getInfo() {
		return ElementKeyMap.getInfo(getType());
	}

	public boolean isValid() {
		return ElementKeyMap.isValidType(getType());
	}

	public int getAbsolutePosX() {
		return ElementCollection.getPosX(getAbsoluteIndex());
	}

	public int getAbsolutePosY() {
		return ElementCollection.getPosY(getAbsoluteIndex());
	}

	public int getAbsolutePosZ() {
		return ElementCollection.getPosZ(getAbsoluteIndex());
	}

	public boolean equalsUniqueIdentifier(String uid) {
		return getSegmentController().getUniqueIdentifier().equals(uid);
	}

	public Vector3f getWorldPos(Vector3f out, int sectorId) {
		getSegmentController().getAbsoluteElementWorldPositionShifted(getAbsolutePosX(), getAbsolutePosY(), getAbsolutePosZ(), sectorId, out);
		return out;
	}

	public boolean equalsSegmentPos(Segment s, Vector3b c) {
		return s.equals(segment) && c.x == x && c.y == y && c.z == z;
	}

	public void applyToSegment(boolean send) {
		if(segment != null && segment.getSegmentData() != null) {
			boolean oldExists = ElementKeyMap.isValidType(segment.getSegmentData().getType(getInfoIndex()));
			boolean newExists = ElementKeyMap.isValidType(getType());
			try {
				segment.getSegmentData().applySegmentData(x, y, z, data, 0, false, getAbsoluteIndex(), oldExists && !newExists, newExists, getSegmentController().getState().getUpdateTime());

				if(send) {
					if(newExists) {

						refresh();
						getSegmentController().sendBlockMod(new RemoteSegmentPiece(this, getSegmentController().isOnServer()));
					} else if(oldExists) {
						//block killed
						getSegmentController().sendBlockKill(this);
					}
				}

			} catch(SegmentDataWriteException e) {
				SegmentDataWriteException.replaceData(segment);
				applyToSegment(send);
			}
		}
	}

	public boolean isDead() {
		return !isAlive();
	}

	public boolean isAlive() {
		return getHitpointsByte() > 0;
	}

	public static class SegmentPiecePool {
		private final List<SegmentPiece> pool = new ObjectArrayList<SegmentPiece>();

		public void free(SegmentPiece p) {
			pool.add(p);
		}

		public SegmentPiece get() {
			if(pool.isEmpty()) {
				return new SegmentPiece();
			} else {
				return pool.remove(pool.size() - 1);
			}
		}
	}

}
