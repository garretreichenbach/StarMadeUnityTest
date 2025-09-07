package org.schema.game.common.data.world;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.data.DataUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.physics.AABBVarSet;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentAabbInterface;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.SimplePosElement;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

public abstract class Segment implements SegmentAabbInterface{

	public static final int DIM_BITS = ByteUtil.Chunk32 ? 5 : 4;
	public static final int DIM_BITS_X2 = DIM_BITS * 2;
	public static final byte DIM = 1 << DIM_BITS;
	public static final byte HALF_DIM = DIM/2;
	static final int HALF_SIZE = (int) Element.BLOCK_SIZE;
	public static boolean ALLOW_ADMIN_OVERRIDE = false;
	public static long debugTime = 0;
	private static int idGen;
	public final float[] cachedTransform = new float[16];
	public final Vector3i pos = new Vector3i();
	public final Vector3i absPos = new Vector3i();
	public float cacheBBMinX;
	public float cacheBBMinY;
	public float cacheBBMinZ;
	public float cacheBBMaxX;
	public float cacheBBMaxY;
	public float cacheBBMaxZ;
	public short cacheDate;
	protected int id;
	private int size = 0;
	private SegmentData segmentData;
	private SegmentController segmentController;

	public Segment(SegmentController segmentController) {
		this.segmentController = segmentController;
		this.id = idGen++;

		cachedTransform[15] = 1.0f;
	}

	public static void debugDraw(Vector3i segPos, Transform segContrTransform, float extraSize, float r, float g, float b, float a, float lifetime) {

		Transform t = new Transform(segContrTransform);
		Vector3f pos = new Vector3f(segPos.x, segPos.y, segPos.z);
		t.basis.transform(pos);
		t.origin.add(pos);
		DebugBox b1 = new DebugBox(
				new Vector3f((-SegmentData.SEG_HALF - extraSize) - 0.5f, (-SegmentData.SEG_HALF - extraSize) - 0.5f, (-SegmentData.SEG_HALF - extraSize) - 0.5f),
				new Vector3f((SegmentData.SEG_HALF + extraSize) - 0.5f, (SegmentData.SEG_HALF + extraSize) - 0.5f, (SegmentData.SEG_HALF + extraSize) - 0.5f),
				new Transform(t),
				r, g, b, a);
		b1.LIFETIME = (long)lifetime;
		DebugDrawer.boxes.add(b1);

	}

	public static void debugDraw(int blockX, int blockY, int blockZ, Vector3i segPos, Transform segContrTransform, float extraSize, float r, float g, float b, float a, float lifetime) {

		Transform t = new Transform(segContrTransform);
		Vector3f pos = new Vector3f(segPos.x + blockX - SegmentData.SEG_HALF, segPos.y + blockY - SegmentData.SEG_HALF, segPos.z + blockZ - SegmentData.SEG_HALF);
		t.basis.transform(pos);
		t.origin.add(pos);
		DebugBox b1 = new DebugBox(
				new Vector3f((-0.5f - extraSize), (-0.5f - extraSize), (-0.5f - extraSize)),
				new Vector3f((0.5f + extraSize), (0.5f + extraSize), (0.5f + extraSize)),
				new Transform(t),
				r, g, b, a);
		b1.LIFETIME = (long)lifetime;
		DebugDrawer.boxes.add(b1);

	}

	/**
	 * @param x   without Element.HALF_SIZE
	 * @param y   without Element.HALF_SIZE
	 * @param z   without Element.HALF_SIZE
	 * @param out
	 * @return
	 */
	public static Vector3b getElementIndexFrom(int x, int y, int z, Vector3b out) {
		/*
		 * the bit function modu16 will produce the same result as this function
		 * 6 times faster. (tested with every int value)
		 *
		 * byte xS = (byte) (Math.abs(x) % SegmentData.SEG);
		 * xS = (byte) (x < 0 ? (SegmentData.SEG - xS) % SegmentData.SEG: xS);
		 */

		out.set((byte) ByteUtil.modUSeg(x), (byte) ByteUtil.modUSeg(y), (byte) ByteUtil.modUSeg(z));

		return out;
	}

	/**
	 * @param x   without Element.HALF_SIZE
	 * @param y   without Element.HALF_SIZE
	 * @param z   without Element.HALF_SIZE
	 * @param out
	 * @return
	 */
	public static Vector3i getSegmentIndexFromSegmentElement(int x, int y, int z, Vector3i out) {
		/*
		 * divided by as an unsinged int, the result is exactly the result needed.
		 * the old function to archive that, was slow, and also had
		 * wrong results for values > MAX_INT/16 or MIN_INT/16 because
		 * the floor function converts the int into a float, taking away integer
		 * presicion
		 *
		 * (results tested with every int value)
		 *
		 *  (int) FastMath.floor((y+(y < 0 ? 1 : 0))/SegmentData.SEG)-(y < 0 ? 1 : 0);
		 */
		out.set(ByteUtil.divUSeg(x), ByteUtil.divUSeg(y), ByteUtil.divUSeg(z));
		return out;
	}

	/**
	 * @param localX without Element.HALF_SIZE
	 * @param localY without Element.HALF_SIZE
	 * @param localZ without Element.HALF_SIZE
	 * @param ofX    without Element.HALF_SIZE
	 * @param ofY    without Element.HALF_SIZE
	 * @param ofZ    without Element.HALF_SIZE
	 * @param range
	 * @return
	 */
	public static boolean isInRange(int localX, int localY, int localZ, int ofX, int ofY, int ofZ, int range) {
		ofX -= localX;
		ofY -= localZ;
		ofZ -= localZ;
		float len = FastMath.sqrt(ofX * ofX + ofY * ofY + ofZ * ofZ);
		return len < range;
	}

	public static void main(String[] args) {
		// CAN BE USED AS TEST
		for (int i = -64; i < 64; i++) {
			System.err.println("0, 0, " + i + " -> " + getElementIndexFrom(0, 0, i, new Vector3b()));
		}
	}

	public static Vector3i getAbsoluteElemPos(Vector3i segPos, final byte x, final byte y, final byte z, final Vector3i out) {
		out.x = segPos.x + x;
		out.y = segPos.y + y;
		out.z = segPos.z + z;
		return out;
	}

	public boolean addElement(short type, Vector3b pos,
	                          int elementOrientation, boolean activateBlock, short hitpoints, SegmentController segController) {
		return addElement(type, pos.x, pos.y, pos.z, elementOrientation, activateBlock, hitpoints, segController);
	}

	public boolean addElement(short type, byte x, byte y, byte z,
	                          int elementOrientation, boolean activateBlock, short hitpoints, SegmentController segController) {
		boolean addedAnElement = false;
		boolean forceDraw = false;
		
		if (isEmpty()) {
			if (segmentData == null) {
				SegmentData mine = segmentController.getSegmentProvider().getFreeSegmentData();
				mine.assignData(this);
			}
			forceDraw = true;
		}
		
		int oldElement = SegmentData.getInfoIndex(x, y, z);
		if (!segmentData.containsUnsave(x, y, z)) {
			
			//defaiult active is true for all blocks taht dont need it (hulls)
			byte activeByte = !activateBlock ? (byte) 0 : ElementInformation.defaultActive(type);
			try{
				segmentData.setInfoElement(x, y, z, type, (byte) elementOrientation, activeByte, hitpoints, true, getAbsoluteIndex(x, y, z), segController.getState().getUpdateTime());
				
//				System.err.println("ADDING "+x+", "+y+", "+z+" TO "+this+"; "+getSegmentData().getType(x,y,z));
			}catch(SegmentDataWriteException ee){
//				ee.printStackTrace();
				SegmentDataWriteException.replaceData(this);
				try {
					segmentData.setInfoElement(x, y, z, type, (byte) elementOrientation, activeByte, hitpoints, true, getAbsoluteIndex(x, y, z), segController.getState().getUpdateTime());
				} catch (SegmentDataWriteException e) {
					throw new RuntimeException(e);
				}
			}
			
			addedAnElement = true;
		}else{
//			System.err.println("NOT ADDING ADDING "+x+", "+y+", "+z+" TO "+this+"; "+getSegmentData().getType(x,y,z));
		}
		if (forceDraw && addedAnElement && !segmentController.isOnServer()) {
			((GameClientState) segmentController.getState()).getWorldDrawer().getSegmentDrawer().forceAdd(((DrawableRemoteSegment) this));
		}
		return addedAnElement;
	}

	public int calculateSurrrounding() {
		int surround = 0;
		for (int i = 0; i < 6; i++) {
			if (segmentController.existsNeighborSegment(pos, i)) {
				surround++;
			}
		}
		return surround;
	}

	public abstract void dataChanged(boolean meshUpdateNeeded);

	public void debugDraw(float extraSize, float r, float g, float b, float a, float lifetime) {
		debugDraw(this.pos, segmentController.getWorldTransform(), extraSize, r, g, b, a, lifetime);
	}

	public void debugDrawPoint(int x, int y, int z, float extraSize, float r, float g, float b, float a, float lifetime) {
		debugDraw(x, y, z, this.pos, segmentController.getWorldTransform(), extraSize, r, g, b, a, lifetime);
	}

	/**
	 * @param elemPos
	 * @param out
	 * @return absolute pos without Element.HALF_SIZE
	 */
	public boolean equalsAbsoluteElemPos(Vector3i check, final byte x, final byte y, final byte z) {
		return check.equals(pos.x + x, pos.y + y, pos.z + z);
	}

	/**
	 * @param elemPos
	 * @param out
	 * @return absolute pos without Element.HALF_SIZE
	 */
	public Vector3i getAbsoluteElemPos(final byte x, final byte y, final byte z, final Vector3i out) {
		out.x = pos.x + x;
		out.y = pos.y + y;
		out.z = pos.z + z;
		return out;
	}
	public Vector3i getAbsoluteElemPos(final int infoIndex, final Vector3i out) {
		int x = SegmentData.getPosXFromIndex(infoIndex);
		int y = SegmentData.getPosYFromIndex(infoIndex);
		int z = SegmentData.getPosZFromIndex(infoIndex);
		out.x = pos.x + x;
		out.y = pos.y + y;
		out.z = pos.z + z;
		return out;
	}
	public long getAbsoluteIndex(int infoIndex) {
		int x = SegmentData.getPosXFromIndex(infoIndex);
		int y = SegmentData.getPosYFromIndex(infoIndex);
		int z = SegmentData.getPosZFromIndex(infoIndex);
		
		return ElementCollection.getIndex(pos.x + x, pos.y + y, pos.z + z);
	}
	public Vector3f getAbsoluteElemPos(final byte x, final byte y, final byte z, final Vector3f out) {
		out.x = pos.x + x;
		out.y = pos.y + y;
		out.z = pos.z + z;
		return out;
	}

	/**
	 * @param elemPos
	 * @param out
	 * @return absolute pos without Element.HALF_SIZE
	 */
	public Vector3i getAbsoluteElemPos(final Vector3b elemPos, final Vector3i out) {
		return getAbsoluteElemPos(elemPos.x, elemPos.y, elemPos.z, out);
	}

	public long getAbsoluteIndex(final byte x, final byte y, final byte z) {
		int xO = pos.x + x;
		int yO = pos.y + y;
		int zO = pos.z + z;
		return ElementCollection.getIndex(xO, yO, zO);
	}

	public long getIndex() {
		return ElementCollection.getIndex(pos);
	}


	public Vector3b getNearestIntersectingElementPosition(Vector3f fromRay,
	                                                      Vector3f toRay) {
		StaticObjectIntersection intersect = new StaticObjectIntersection();
		intersect.getIntersect(fromRay, toRay, segmentData);

		if (intersect.isIntersection()) {
			System.err.println("[SEGMENT] element to delete found at "
					+ intersect.nearest);
			// Vector3f normalizedToDim = new Vector3f(intersect.nearest);
			// partsToDel.put(intersect.nearest, element);
			return intersect.nearest;
		}
		return null;

	}

	/**
	 * returns the pos of the added element if added
	 *
	 * @param toAddType
	 * @param fromRay
	 * @param toRay
	 * @return
	 */
	public Vector3b getNearestIntersectionLocal(Vector3f fromRay,
	                                            Vector3f toRay) {
		StaticObjectIntersection intersect = new StaticObjectIntersection();
		intersect.getIntersect(fromRay, toRay, segmentData);

		if (intersect.isIntersection()) {
			Vector3b pos = new Vector3b(intersect.nearest);
			switch(intersect.side) {
				case SimplePosElement.RIGHT -> pos.x += 1f;
				case SimplePosElement.TOP -> pos.y += 1f;
				case SimplePosElement.FRONT -> pos.z += 1f;
				case SimplePosElement.LEFT -> pos.x -= 1f;
				case SimplePosElement.BOTTOM -> pos.y -= 1f;
				case SimplePosElement.BACK -> pos.z -= 1f;
				default -> throw new IllegalArgumentException("unknown side: " + intersect.side);
			}

			return pos;
		}
		return null;
	}

	public SegmentController getSegmentController() {
		return segmentController;
	}

	public final void setSegmentController(SegmentController segmentController) {
		this.segmentController = segmentController;
	}

	/**
	 * @return the segmentDataArray
	 */
	public SegmentData getSegmentData() {
		return segmentData;
	}

	/**
	 * @param segmentDataArray the segmentDataArray to set
	 */
	public void setSegmentData(SegmentData segmentData) {
		this.segmentData = segmentData;
	}

	//	public Vector3f getWorldPosition(Vector3f in, float shiftX, float shiftY, float shiftZ) {
	//		in.set(getSegmentController().getOpenGLMatrix()[12]+pos.x+shiftX,
	//				getSegmentController().getOpenGLMatrix()[13]+pos.y+shiftY,
	//				getSegmentController().getOpenGLMatrix()[14]+pos.z+shiftZ);
	//		getSegmentController().getWorldTransform().basis.transform(in);
	//		return in;
	//	}
	//	public abstract byte getVisablility(Vector3b pos);

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	public byte getVisablility(Vector3b pos) {
		return (byte) 63;//getVisibilityMask(pos, getSegmentData(), getSegmentController().getSegmentBuffer());
	}

	public Vector3f getWorldPosition(Vector3f in) {
		in.set(pos.x, pos.y, pos.z);
		segmentController.getWorldTransform().basis.transform(in);
		in.add(segmentController.getWorldTransform().origin);
		return in;
	}

	

	public boolean hasBlockInRange(Vector3i min, Vector3i max) {
		if (min.x < pos.x && min.y < pos.y && min.z < pos.z &&

				max.x > pos.x + SegmentData.SEG && max.y > pos.y + SegmentData.SEG && max.z > pos.z + SegmentData.SEG) {

			return !isEmpty();
		}
		return segmentController.getCollisionChecker().hasSegmentPartialAABBBlock(this, min, max);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pos.hashCode() + segmentController.getId();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this == ((Segment) obj);
	}

	@Override
	public String toString() {
		return "DATA:" + (segmentController.getId() + ":s" + size);
	}

//	public float hit(float damageInitial, float armorEfficiency, float armorHarden, Vector3f origin, float radius, SegmentDamageCallback cb) {
//		if (!isEmpty()) {
//			Vector3i oPos = new Vector3i(origin);
//			oPos.sub(pos);
//			
//			try{
//				return getSegmentData().damage(damageInitial, oPos, armorEfficiency, armorHarden, radius, cb);
//			}catch(SegmentDataWriteException ee){
//				SegmentDataWriteException.replaceData(this);
//				try {
//					return getSegmentData().damage(damageInitial, oPos, armorEfficiency, armorHarden, radius, cb);
//				} catch (SegmentDataWriteException e) {
//					throw new RuntimeException(e);
//				}
//			}
//			
//		}
//		return 0;
//	}

	public boolean isEmpty() {
		//light thread might be affected
//		assert(getSegmentData() != null || size == 0);
//		assert(getSegmentData() == null || size == getSegmentData().getSize());
		return size == 0 || segmentData == null;
	}


	public boolean removeElement(short filter, Vector3b position, boolean updateBB) {
		return removeElement(filter, position.x, position.y, position.z, updateBB);
	}

	public boolean removeElement(byte x, byte y, byte z, boolean updateBB) {
		return removeElement(Element.TYPE_ALL, x, y, z, updateBB);
	}

	public boolean removeElement(short filter, byte x, byte y, byte z, boolean updateBB) {
		return removeElement(filter, SegmentData.getInfoIndex(x,y,z), updateBB);
	}
	public boolean removeElement(int infoIndex, boolean updateBB) {
		return removeElement(Element.TYPE_ALL, infoIndex, updateBB);
	}
	public boolean removeElement(short filter, int infoIndex, boolean updateBB) {

		//		System.err.println("[SEGMENT]["+getSegmentController().getState()+" "+getSegmentController()+"]: "+pos+": Element to remove has coords " + position.x + ", "
		//				+ position.y + ", " + position.z);
		boolean removed = false;
		if (!isEmpty()) {
			removed = segmentData.contains(infoIndex);
			if (filter == Element.TYPE_ALL || segmentData.getType(infoIndex) == filter) {
				byte x = SegmentData.getPosXFromIndex(infoIndex);
				byte y = SegmentData.getPosYFromIndex(infoIndex);
				byte z = SegmentData.getPosZFromIndex(infoIndex);
				try{
					segmentData.setInfoElement(x, y, z, Element.TYPE_NONE, updateBB, getAbsoluteIndex(infoIndex), segmentController.getState().getUpdateTime());
				}catch(SegmentDataWriteException ee){
					SegmentDataWriteException.replaceData(this);
//					assert(false);
					try {
						segmentData.setInfoElement(x, y, z, Element.TYPE_NONE, updateBB, getAbsoluteIndex(infoIndex), segmentController.getState().getUpdateTime());
					} catch (SegmentDataWriteException e) {
						throw new RuntimeException(e);
					}
				}
				
			}
		}
//		System.err.println("REMO "+getSegmentController()+"; "+getSegmentController().getState()+"; "+removed+"; "+((TransientSegmentController)getSegmentController()).isTouched());
		if (isEmpty() && segmentData != null) {
			segmentController.getSegmentBuffer().restructBBFast(segmentData);

			segmentController.getSegmentProvider().addToFreeSegmentData(segmentData);
		}
		CollisionObject pObject = segmentController.getPhysicsDataContainer().getObject();
		if (pObject != null) {
			boolean wasActive = pObject.isActive();
			pObject.activate(true);
			((RigidBody) pObject).applyGravity();
			assert (pObject.isActive());

			//			System.err.println(getSegmentController().getState()+" REMOVED BLOCK OF "+wasActive+" -> "+getSegmentController()+"; "+((RigidBody)pObject).getActivationState());
		}
		return removed;
	}



	public boolean removeElement(Vector3b position, boolean updateBB) {

		return removeElement(Element.TYPE_ALL, position, updateBB);
	}


	public void setChangedSurround(int x, int y, int z) {
		Segment sX = segmentController.getSegmentFromCache(
				pos.x + x * SegmentData.SEG, 
				pos.y + y * SegmentData.SEG, 
				pos.z + z * SegmentData.SEG);
		if (sX != null) {
			sX.dataChanged(true);
		}
	}

	public void setPos(int x, int y, int z) {
		this.pos.set(x, y, z);
		this.absPos.set(ByteUtil.divUSeg(x), ByteUtil.divUSeg(y), ByteUtil.divUSeg(z));
	}

	public void setPos(Vector3i pos) {
		setPos(pos.x, pos.y, pos.z);
	}
	public void setPos(long index) {
		setPos(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index));
	}

	public void writeToFile() throws IOException {
		BufferedOutputStream f = new BufferedOutputStream(new FileOutputStream(new FileExt(DataUtil.dataPath + "save.smseg")));

		f.write(ByteUtil.intToByteArray(pos.x));
		f.write(ByteUtil.intToByteArray(pos.y));
		f.write(ByteUtil.intToByteArray(pos.z));
		f.write(ByteUtil.intToByteArray(segmentData.getSize()));
		for (byte z = 0; z < SegmentData.SEG; z++) {
			for (byte y = 0; y < SegmentData.SEG; y++) {
				for (byte x = 0; x < SegmentData.SEG; x++) {
					short t = segmentData.getType(x, y, z);
					if (t != Element.TYPE_NONE) {
						f.write(x);
						f.write(y);
						f.write(z);
						f.write(ByteUtil.shortToByteArray(t));
					}
				}
			}
		}
		f.flush();
		f.close();
	}

	@Override
	public void getSegmentAabb(Segment s, Transform trans,
			Vector3f outOuterMin, Vector3f outOuterMax, Vector3f localMinOut,
			Vector3f localMaxOut, AABBVarSet varSet) {
		assert(false);
	}

	@Override
	public void getAabb(Transform tmpAABBTrans0, Vector3f min, Vector3f max) {
		assert(false);
	}

	@Override
	public void getAabbUncached(Transform t, Vector3f aabbMin,
			Vector3f aabbMax, boolean cache) {
		assert(false);
	}

	@Override
	public void getAabbIdent(Vector3f min, Vector3f max) {
		assert(false);
		
	}

	

	
}
