package org.schema.game.common.data.physics.octree;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.physics.BoxShapeExt;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.simple.Box;

import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.collision.narrowphase.SubsimplexConvexCast;
import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;

public class OctreeLeaf {

	public static final int BLOCK_SIZE =
			ByteUtil.SIZEOF_SHORT +
					ByteUtil.SIZEOF_SHORT +
					ByteUtil.SIZEOF_BYTE +
					ByteUtil.SIZEOF_BYTE;
	private final short id;
	private final byte lvl;
	public int index;
	public int localIndex;
	public int nodeIndex;
	private short cnt;
	private boolean hasHit;
	private boolean onServer;

	public OctreeLeaf(int index, final byte level, final int maxLevel, boolean onServer) {
		super();
		assert (maxLevel >= 0);
		assert (level >= 0);
		this.onServer = onServer;
		this.id = getSet().getId(level, index, OctreeLevel.START);
		this.lvl = level;

	}

	public OctreeLeaf(final Vector3b start, final Vector3b end, int index, final byte level, final int maxLevel, boolean onServer) {
		super();
		assert (maxLevel >= 0);
		assert (level >= 0);

		assert (index < Short.MAX_VALUE);

		this.onServer = onServer;
		this.id = getSet().put(level, index, OctreeLevel.START, start);
		this.lvl = level;

		getSet().put(level, index, OctreeLevel.END, end);

		Vector3b dim = new Vector3b(end);
		dim.sub(start);

		getSet().put(level, index, OctreeLevel.DIM, dim);

		Vector3b halfDim = getDim(new Vector3b());
		halfDim.div((byte) 2);

		getSet().put(level, index, OctreeLevel.HALF, halfDim);

		this.index = index;
		this.localIndex = index % 8;

		if (onServer) {
			this.nodeIndex = ArrayOctree.getIndex(index, level - 1);
			StringBuilder sb = new StringBuilder();
			sb.append(level);
			for (int i = 0; i < level; i++) {
				sb.append("    ");
			}
			sb.append("#### I " + index + " tot " + OctreeVariableSet.nodes + " -> " + ArrayOctree.getIndex((index), level));
			System.err.println(sb);

			OctreeVariableSet.nodes++;
		}
	}

	private boolean between(byte x, byte y, byte z) {

		return (x >= getStartX() && y >= getStartY() && z >= getStartZ()) &&
				(x < getEndX() && y < getEndY() && z < getEndZ());
	}

	public void delete(byte x, byte y, byte z, TreeCache treeCache, int level) {
		if (cnt <= 0) {
			System.err.println("Exception: WARNING Octree Size < 0");
		}
		cnt = (short) Math.max(0, cnt - 1);

	}

	public void deleteCached(TreeCache treeCache, int i) {
		assert (cnt > 0);
		cnt--;
	}

	protected IntersectionCallback doIntersecting(OctreeVariableSet set,
	                                              IntersectionCallback intersectionCall, Segment segment,
	                                              Transform selfTrans, Matrix3f absoluteMat, float margin, Vector3f otherMin, Vector3f otherMax, float scale, boolean cached) {
		intersectionCall.leafCalcs++;

		getStart(set, set.min);
		getEnd(set, set.max);

		Vector3f tmpMin = set.tmpMin;
		Vector3f tmpMax = set.tmpMax;
		Vector3f tmpMinOut = set.tmpMinOut;
		Vector3f tmpMaxOut = set.tmpMaxOut;

		float x = segment.pos.x - 0.5f;
		float y = segment.pos.y - 0.5f;
		float z = segment.pos.z - 0.5f;

		tmpMin.x = set.min.x + x;
		tmpMin.y = set.min.y + y;
		tmpMin.z = set.min.z + z;

		tmpMax.x = set.max.x + x;
		tmpMax.y = set.max.y + y;
		tmpMax.z = set.max.z + z;

		transformAabb(set, tmpMin, tmpMax, absoluteMat, margin, selfTrans, tmpMinOut, tmpMaxOut);

		boolean intersection = AabbUtil2.testAabbAgainstAabb2(tmpMinOut, tmpMaxOut, otherMin, otherMax);
		hasHit = intersection;
		return intersectionCall;
	}

	public void drawOctree(Vector3f offset, boolean force) {
		if (!force) {
			if (!hasHit || isEmpty()) {
				return;
			}
		}

		getSet().tmpMin.set(getStartX(), getStartY(), getStartZ());
		getSet().tmpMin.scale(Element.BLOCK_SIZE);
		getSet().tmpMin.x += (-(Element.BLOCK_SIZE / 2f));
		getSet().tmpMin.y += (-(Element.BLOCK_SIZE / 2f));
		getSet().tmpMin.z += (-(Element.BLOCK_SIZE / 2f));

		getSet().tmpMax.set(getEndX(), getEndY(), getEndZ());
		getSet().tmpMax.scale(Element.BLOCK_SIZE);
		getSet().tmpMax.x += (-(Element.BLOCK_SIZE / 2f));
		getSet().tmpMax.y += (-(Element.BLOCK_SIZE / 2f));
		getSet().tmpMax.z += (-(Element.BLOCK_SIZE / 2f));

		getSet().tmpMax.sub(offset);
		getSet().tmpMin.sub(offset);
		Vector3f[][] box = Box.getVertices(getSet().tmpMin, getSet().tmpMax);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_TEXTURE_1D);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glColor4f(1, 1, 1, 0.2f);
		float blue = 0;
		if (!isEmpty()) {
			blue = 1;
		}
		if (!force) {
			//			if(hasLeafHit){
			GlUtil.glColor4f(1, 0, blue, 0.9f);
			//			hasLeafHit = false;
			//			}else if(isHasHit()){
			//				GlUtil.glColor4f(0, 1, blue, 1);
			//	//			hasHit = false;
			//			}
		} else {
			GlUtil.glColor4f(0, 1, 0, 1);
		}

		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < box.length; i++) {
			for (int k = 0; k < box[i].length; k++) {
				GL11.glVertex3f(box[i][k].x, box[i][k].y, box[i][k].z);
			}
		}
		GL11.glEnd();

		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	public IntersectionCallback findIntersecting(
			OctreeVariableSet set, IntersectionCallback intersectionCall,
			Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin, Vector3f otherMin, Vector3f otherMax, float scale, boolean cached) {

		intersectionCall = doIntersecting(set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, cached);

		//do this only on leaves
		if (hasHit) {

			//			intersectionCall.addHit(set.tmpMinOut, set.tmpMaxOut, set.min.x, set.min.y, set.min.z, set.max.x, set.max.y, set.max.z,255);
		}

		return intersectionCall;
	}

	public IntersectionCallback findIntersectingCast(
			IntersectionCallback intersectionCallBack, Transform selfTrans,
			BoxShapeExt boxShape, ConvexShape convexShape, float margin,
			Segment segment, Transform fromA, Transform toA, float scale) {

		getSet().tmpMin.set(getStartX(), getStartY(), getStartZ());
		getSet().tmpMin.x += (segment.pos.x - (0.5f));
		getSet().tmpMin.y += (segment.pos.y - (0.5f));
		getSet().tmpMin.z += (segment.pos.z - (0.5f));

		getSet().tmpMax.set(getEndX(), getEndY(), getEndZ());
		getSet().tmpMax.x += (segment.pos.x - (0.5f));
		getSet().tmpMax.y += (segment.pos.y - (0.5f));
		getSet().tmpMax.z += (segment.pos.z - (0.5f));

		boxShape.setDimFromBB(getSet().tmpMin, getSet().tmpMax);

		SubsimplexConvexCast convexCaster = new SubsimplexConvexCast(convexShape, boxShape, new VoronoiSimplexSolver());
		CastResult castResult = new CastResult();
		castResult.allowedPenetration = 0.03f;
		castResult.fraction = 1f; // ??

		boolean intersection = convexCaster.calcTimeOfImpact(fromA, toA, selfTrans, selfTrans, castResult);
		hasHit = intersection;

		if (hasHit) {
			System.err.println("NODE hit registered (" + castResult.hitPoint + " in: " + getSet().tmpMin + " - " + getSet().tmpMax + ", dim: " + getDim(new Vector3b()) + ": " + this.getClass());
			if (isLeaf()) {
				//				System.err.println("LEAF hit registered in: "+tmpMin+" - "+tmpMax+", dim: "+getDim()+", lvl: "+getLevel()+": "+this.getClass());
				if (!isEmpty()) {
					//					intersectionCallBack.addHit(getSet().tmpMinOut, getSet().tmpMaxOut, getStartX(), getStartY(), getStartZ(), getEndX(), getEndY(), getEndZ(), 255);
				}
			}

		}
		return intersectionCallBack;
	}

	public IntersectionCallback findIntersectingRay(
			OctreeVariableSet set, IntersectionCallback intersectionCallBack, Transform selfTrans, Matrix3f absoluteMat,
			float margin, Segment segment, Vector3f fromA, Vector3f toA, float scale) {

		intersectionCallBack.leafCalcs++;
		//		if(segment.getSegmentController().getState().getUpdateNumber() == cacheDate && cache != null){
		//			set.tmpMinOut.set(cache[0], cache[1], cache[2]);
		//			set.tmpMaxOut.set(cache[3], cache[4], cache[5]);
		//		}else{

		Vector3f tmpMin = set.tmpMin;
		Vector3f tmpMax = set.tmpMax;
		Vector3f tmpMinOut = set.tmpMinOut;
		Vector3f tmpMaxOut = set.tmpMaxOut;

		getStart(set, set.min);
		getEnd(set, set.max);

		float x = segment.pos.x - 0.5f;
		float y = segment.pos.y - 0.5f;
		float z = segment.pos.z - 0.5f;

		tmpMin.x = set.min.x + x;
		tmpMin.y = set.min.y + y;
		tmpMin.z = set.min.z + z;

		tmpMax.x = set.max.x + x;
		tmpMax.y = set.max.y + y;
		tmpMax.z = set.max.z + z;

		//			set.tmpDistTest.sub(set.tmpMax, set.tmpMin);
		//			if(set.tmpDistTest.lengthSquared() <= 0.0001){
		//				System.err.println("[OCTREE] NO BOUNDING BOX YET! Obj: "+segment.pos+" in "+segment.getSegmentData().getSegmentController());
		//				return intersectionCallBack;
		//			}

		//			Vector3f tMin = new Vector3f(tmpMin);
		//			Vector3f tMax = new Vector3f(tmpMax);
		//			assert(!(	(tMin.x > tMax.x) ||
		//					(tMin.y > tMax.y) ||
		//					(tMin.z > tMax.z))):
		//						"[WARNING] BOUNDING BOX IS FAULTY: "+
		//						segment.pos+" in "+segment.getSegmentData().getSegmentController()+": "
		//						+tMin+" - "+tMax+"; star/end "+
		//						getStart(new Vector3b())+" - "+getEnd(new Vector3b())+
		//						"------ "+
		//						(set.tmpMin.x > set.tmpMax.x)+","+(set.tmpMin.y > set.tmpMax.y)+","+(set.tmpMin.z > set.tmpMax.z);
		//		System.err.println("[WARNING] before BOUNDING BOX IS FAULTY: "+segment.pos+" in "+segment.getSegmentData().getSegmentController()+": "+set.tmpMin+" - "+set.tmpMax+"; star/end "+getStart()+" - "+getEnd());
		assert (!((tmpMin.x > tmpMax.x) ||
				(tmpMin.y > tmpMax.y) ||
				(tmpMin.z > tmpMax.z))) :
				"[WARNING] BOUNDING BOX IS FAULTY: " +
						segment.pos + " in " + segment.getSegmentData().getSegmentController() + ": "
						+ tmpMin + " - " + tmpMax + "; star/end " +
						getStart(new Vector3b()) + " - " + getEnd(new Vector3b()) +
						"------ " +
						(set.tmpMin.x > set.tmpMax.x) + "," + (set.tmpMin.y > set.tmpMax.y) + "," + (set.tmpMin.z > set.tmpMax.z);
		//		System.err.println("[WARNING] before BOUNDING BOX IS FAULTY: "+segment.pos+" in "+segment.getSegmentData().getSegmentController()+": "+set.tmpMin+" - "+set.tmpMax+"; star/end "+getStart()+" - "+getEnd());

		transformAabb(set, tmpMin, tmpMax, absoluteMat, margin, selfTrans, tmpMinOut, tmpMaxOut);

		//			AabbUtil2.transformAabb( tmpMin, tmpMax, margin, selfTrans, tmpMinOut, tmpMaxOut);

		//			cache = set.getFromCache();
		//			cache[0] = set.tmpMinOut.x;
		//			cache[1] = set.tmpMinOut.y;
		//			cache[2] = set.tmpMinOut.z;
		//
		//			cache[3] = set.tmpMaxOut.x;
		//			cache[4] = set.tmpMaxOut.y;
		//			cache[5] = set.tmpMaxOut.z;

		//			cacheMin = set.getFromCache();
		//			cacheMax = set.getFromCache();
		//
		//			cacheMin.set(set.tmpMinOut);
		//			cacheMax.set(set.tmpMaxOut);
		//			cacheDate = segment.getSegmentController().getState().getUpdateNumber();
		//		}

		set.param[0] = 1; //should normally be closest hit fraction of the ray result callback
		set.normal.x = 0;
		set.normal.y = 0;
		set.normal.z = 0;

		boolean intersection = AabbUtil2.rayAabb(fromA, toA, tmpMinOut, tmpMaxOut, set.param, set.normal);

		//only test, if either point is inside AABB
		boolean inside = false;
		if (!intersection) {
			inside = BoundingBox.testPointAABB(toA, tmpMinOut, tmpMaxOut) || BoundingBox.testPointAABB(fromA, tmpMinOut, tmpMaxOut);
		}
		//		intersection = intersection || BoundingBox.testPointAABB(toA, set.tmpMinOut, set.tmpMaxOut);

		hasHit = intersection || inside;

		if (isLeaf() && hasHit) {
			//			intersectionCallBack.addHit(tmpMinOut, tmpMaxOut, set.min.x, set.min.y, set.min.z, set.max.x, set.max.y, set.max.z, 255);
		}

		return intersectionCallBack;
	}

	public Vector3b getDim(Vector3b out) {
		getSet().get((short) (id + OctreeLevel.DIM), out);
		return out;
	}

	public byte getDimX() {
		return getSet().getX((short) (id + OctreeLevel.DIM));
	}

	public byte getDimY() {
		return getSet().getY((short) (id + OctreeLevel.DIM));
	}

	public byte getDimZ() {
		return getSet().getZ((short) (id + OctreeLevel.DIM));
	}

	public Vector3b getEnd(OctreeVariableSet set, Vector3b out) {
		set.get((short) (id + OctreeLevel.END), out);
		return out;
	}

	public Vector3f getEnd(OctreeVariableSet set, Vector3f out) {
		set.get((short) (id + OctreeLevel.END), out);
		return out;
	}

	public Vector3b getEnd(Vector3b out) {
		getSet().get((short) (id + OctreeLevel.END), out);
		return out;
	}

	public Vector3f getEnd(Vector3f out) {
		getSet().get((short) (id + OctreeLevel.END), out);
		return out;
	}

	public byte getEndX() {
		return getSet().getX((short) (id + OctreeLevel.END));
	}

	public byte getEndY() {
		return getSet().getY((short) (id + OctreeLevel.END));
	}

	public byte getEndZ() {
		return getSet().getZ((short) (id + OctreeLevel.END));
	}

	public Vector3b getHalfDim(Vector3b out) {
		getSet().get((short) (id + OctreeLevel.HALF), out);
		return out;
	}

	public byte getHalfDimX() {
		return getSet().getX((short) (id + OctreeLevel.HALF));
	}

	public byte getHalfDimY() {
		return getSet().getY((short) (id + OctreeLevel.HALF));
	}

	public byte getHalfDimZ() {
		return getSet().getZ((short) (id + OctreeLevel.HALF));
	}

	public short getId() {
		return id;
	}

	public int getMaxLevel() {
		return getSet().maxLevel;
	}

	public OctreeVariableSet getSet() {
		return Octree.get(onServer());
	}

	public Vector3b getStart(OctreeVariableSet set, Vector3b out) {
		set.get((short) (id + OctreeLevel.START), out);
		return out;
	}

	public Vector3f getStart(OctreeVariableSet set, Vector3f out) {
		set.get((short) (id + OctreeLevel.START), out);
		return out;
	}

	public Vector3b getStart(Vector3b out) {
		getSet().get((short) (id + OctreeLevel.START), out);
		return out;
	}

	public Vector3f getStart(Vector3f out) {
		getSet().get((short) (id + OctreeLevel.START), out);
		return out;
	}

	public byte getStartX() {
		return getSet().getX((short) (id + OctreeLevel.START));
	}

	public byte getStartY() {
		return getSet().getY((short) (id + OctreeLevel.START));
	}

	public byte getStartZ() {
		return getSet().getZ((short) (id + OctreeLevel.START));
	}

	public void insert(byte x, byte y, byte z, TreeCache treeCache, int level) {
		assert (between(x, y, z)) : "not in range: " + x + ", " + y + ", " + z + ": [" + getStartX() + " - " + getEndX() + "], half: " + getHalfDimX();
		//		assert(elementCount < maxSize):elementCount+" / "+maxSize+", lvl: "+getLevel()+", dim: "+getDim()+", start: "+getStart()+", end "+getEnd()+"; <--- "+x+", "+y+", "+z;
		cnt++;

	}

	public void insertCached(TreeCache treeCache, int level) {
		cnt++;
	}

	public boolean isEmpty() {
		return cnt == 0;
	}

	/**
	 * @return the hasHit
	 */
	public boolean isHasHit() {
		return hasHit;
	}

	/**
	 * @param hasHit the hasHit to set
	 */
	public void setHasHit(boolean hasHit) {
		this.hasHit = hasHit;
	}

	protected boolean isLeaf() {
		return true;
	}

	protected boolean onServer() {
		return onServer;
	}

	public void reset() {
		cnt = 0;

	}

	//	public void resetHits() {
	//		setHasHit(false);
	//	}

	private void transformAabb(OctreeVariableSet set, Vector3f localAabbMin, Vector3f localAabbMax, Matrix3f absoluteMat, float margin, Transform trans, Vector3f aabbMinOut, Vector3f aabbMaxOut) {

		Vector3f localCenter = set.localCenter;
		localCenter.add(localAabbMin, OctreeVariableSet.localCentersAdd[lvl]);

		Vector3f center = set.center;
		center.set(localCenter);
		trans.transform(center);

		Vector3f extent = set.extend;
		//		Vector3f tmp = set.tmpAB;

		Vector3f v1 = OctreeVariableSet.localHalfExtends[lvl];

		//		absoluteMat.getRow(0, tmp);
		//		tmp.set(absoluteMat.m00, absoluteMat.m01, absoluteMat.m02);
		//		extent.x = tmp.dot(OctreeVariableSet.localHalfExtends[lvl]);
		extent.x = (absoluteMat.m00 * v1.x + absoluteMat.m01 * v1.y + absoluteMat.m02 * v1.z);

		//		absoluteMat.getRow(1, tmp);
		//		tmp.set(absoluteMat.m10, absoluteMat.m11, absoluteMat.m12);
		//		extent.y = tmp.dot(OctreeVariableSet.localHalfExtends[lvl]);
		extent.y = (absoluteMat.m10 * v1.x + absoluteMat.m11 * v1.y + absoluteMat.m12 * v1.z);

		//		absoluteMat.getRow(2, tmp);
		//		tmp.set(absoluteMat.m20, absoluteMat.m21, absoluteMat.m22);
		//		extent.z = tmp.dot(OctreeVariableSet.localHalfExtends[lvl]);
		extent.z = (absoluteMat.m20 * v1.x + absoluteMat.m21 * v1.y + absoluteMat.m22 * v1.z);
		;

		aabbMinOut.sub(center, extent);
		aabbMaxOut.add(center, extent);
	}

}
