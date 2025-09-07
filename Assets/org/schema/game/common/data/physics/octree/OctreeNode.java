package org.schema.game.common.data.physics.octree;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.physics.BoxShapeExt;
import org.schema.game.common.data.world.Segment;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;

public class OctreeNode extends OctreeLeaf {

	private OctreeLeaf[] children;

	public OctreeNode(int index, final byte level, final int maxLevel, boolean set) {
		super(index, level, maxLevel, set);
	}

	public OctreeNode(final Vector3b start, final Vector3b end, int index, final byte level, final int maxLevel, boolean onServer) {
		super(start, end, index, level, maxLevel, onServer);

	}

	@Override
	public void delete(byte x, byte y, byte z, TreeCache treeCache, int level) {
		super.delete(x, y, z, treeCache, level);

		if (y >= getStartX() && y < getEndY() - getHalfDimY()) {
			//BOTTOM 4
			if (x >= getStartX() && x < getEndX() - getHalfDimX()) {
				if (z >= getStartZ() && z < getEndZ() - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 0;
					// 0,0,0
					children[0].delete(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 3;
					// 0,0,1
					children[3].delete(x, y, z, treeCache, level + 1);
				}
			} else {
				if (z >= getStartZ() && z < getEndZ() - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 1;
					//1,0,0
					children[1].delete(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 2;
					//1,0,1
					children[2].delete(x, y, z, treeCache, level + 1);
				}
			}
		} else {
			//TOP 4
			if (x >= getStartX() && x < getEndX() - getHalfDimX()) {
				if (z >= getStartZ() && z < getEndZ() - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 4;
					// 0,1,0
					children[4].delete(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 7;
					// 0,1,1
					children[7].delete(x, y, z, treeCache, level + 1);
				}
			} else {
				if (z >= getStartZ() && z < getEndZ() - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 5;
					//1,1,0
					children[5].delete(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 6;
					//1,1,1
					children[6].delete(x, y, z, treeCache, level + 1);
				}
			}
		}
	}

	@Override
	public void deleteCached(TreeCache treeCache, int level) {
		super.deleteCached(treeCache, level + 1);
		children[treeCache.lvlToIndex[level]].deleteCached(treeCache, level + 1);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.physics.octree.OctreeLeaf#drawOctree()
	 */
	@Override
	public void drawOctree(Vector3f offset, boolean force) {
		if (isHasHit()) {
			super.drawOctree(offset, force);
			for (int i = 0; i < children.length; i++) {
				children[i].drawOctree(offset, false);
			}
		}
	}

	@Override
	public IntersectionCallback findIntersecting(
			OctreeVariableSet set, IntersectionCallback intersectionCall,
			Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin, Vector3f otherMin, Vector3f otherMax, float scale, boolean cached) {

		intersectionCall = doIntersecting(set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, cached);

		if (isHasHit()) {
			for (int i = 0; i < children.length; i++) {
				if (!children[i].isEmpty()) {
					intersectionCall = children[i].findIntersecting(set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, cached);
				} else {
					children[i].setHasHit(false);
				}
			}
		}
		return intersectionCall;

	}

	@Override
	public IntersectionCallback findIntersectingCast(
			IntersectionCallback intersectionCallBack, Transform selfTrans, BoxShapeExt boxShape, ConvexShape convexShape, float margin, Segment segment,
			Transform fromA, Transform toA, float scale) {

		intersectionCallBack = super.findIntersectingCast(intersectionCallBack, selfTrans, boxShape, convexShape, margin, segment, fromA, toA, scale);
		//		System.err.println("NODE HIT "+hasHit);
		if (isHasHit()) {
			//			System.err.println("NODE HIT, testing childs of lvl "+(getLevel()+1));
			for (int i = 0; i < children.length; i++) {
				intersectionCallBack = children[i].findIntersectingCast(intersectionCallBack, selfTrans, boxShape, convexShape, margin, segment, fromA, toA, scale);
			}
		}
		return intersectionCallBack;
	}

	@Override
	public IntersectionCallback findIntersectingRay(
			OctreeVariableSet set, IntersectionCallback intersectionCallBack, Transform selfTrans, Matrix3f absoluteMat,
			float margin, Segment segment, Vector3f fromA, Vector3f toA, float scale) {

		intersectionCallBack = super.findIntersectingRay(set, intersectionCallBack, selfTrans, absoluteMat, margin, segment, fromA, toA, scale);
		//		System.err.println("NODE HIT "+hasHit);
		if (isHasHit()) {
			//			System.err.println("NODE HIT, testing childs of lvl "+(getLevel()+1));
			for (int i = 0; i < children.length; i++) {
				if (!children[i].isEmpty()) {
					intersectionCallBack = children[i].findIntersectingRay(set, intersectionCallBack, selfTrans, absoluteMat, margin, segment, fromA, toA, scale);
				} else {
					children[i].setHasHit(false);
				}
			}
		}
		return intersectionCallBack;
	}

	@Override
	public void insert(byte x, byte y, byte z, TreeCache treeCache, int level) {
		super.insert(x, y, z, treeCache, level + 1);

		byte startX = getStartX();
		byte startY = getStartY();
		byte startZ = getStartZ();

		byte endX = getEndX();
		byte endY = getEndY();
		byte endZ = getEndZ();

		if (y >= startY && y < endY - getHalfDimY()) {
			//			System.err.println("bottom: "+x+", "+y+", "+z+": ["+getStart()+" - "+getEnd()+"], half: "+getHalfDim());
			//BOTTOM 4
			if (x >= startX && x < endX - getHalfDimX()) {
				if (z >= startZ && z < endZ - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 0;
					// 0,0,0
					children[0].insert(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 3;
					// 0,0,1
					children[3].insert(x, y, z, treeCache, level + 1);
				}
			} else {
				if (z >= startZ && z < endZ - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 1;
					//1,0,0
					children[1].insert(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 2;
					//1,0,1
					children[2].insert(x, y, z, treeCache, level + 1);
				}
			}
		} else {
			//			System.err.println("top: "+x+", "+y+", "+z+": ["+getStart()+" - "+getEnd()+"], half: "+getHalfDim());
			//TOP 4
			if (x >= startX && x < endX - getHalfDimX()) {
				if (z >= startZ && z < endZ - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 4;
					// 0,1,0
					children[4].insert(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 7;
					// 0,1,1
					children[7].insert(x, y, z, treeCache, level + 1);
				}
			} else {
				if (z >= startZ && z < endZ - getHalfDimZ()) {
					treeCache.lvlToIndex[level] = 5;
					//1,1,0
					children[5].insert(x, y, z, treeCache, level + 1);
				} else {
					treeCache.lvlToIndex[level] = 6;
					//1,1,1
					children[6].insert(x, y, z, treeCache, level + 1);
				}
			}
		}
	}

	@Override
	public void insertCached(TreeCache treeCache, int level) {
		super.insertCached(treeCache, level + 1);
		children[treeCache.lvlToIndex[level]].insertCached(treeCache, level + 1);
	}

	@Override
	protected boolean isLeaf() {
		return false;
	}

	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < children.length; i++) {
			children[i].reset();
		}

	}

	/**
	 * @return the children
	 */
	public OctreeLeaf[] getChildren() {
		return children;
	}

	/**
	 * Splits this node in 8 subtrees
	 */
	public int split(int index, int level) {

		int a = 1;
		children = new OctreeLeaf[8];

		if (getSet().first) {
			Vector3b startAA = getStart(new Vector3b());
			Vector3b endAA = getHalfDim(new Vector3b());
			endAA.add(startAA);

			Vector3b startAB = new Vector3b(startAA);
			Vector3b endAB = new Vector3b(endAA);
			startAB.add(getHalfDimX(), (byte) 0, (byte) 0);
			endAB.add(getHalfDimX(), (byte) 0, (byte) 0);

			Vector3b startAC = new Vector3b(startAA);
			Vector3b endAC = new Vector3b(endAA);
			startAC.add(getHalfDimX(), (byte) 0, getHalfDimZ());
			endAC.add(getHalfDimX(), (byte) 0, getHalfDimZ());

			Vector3b startAD = new Vector3b(startAA);
			Vector3b endAD = new Vector3b(endAA);
			startAD.add((byte) 0, (byte) 0, getHalfDimZ());
			endAD.add((byte) 0, (byte) 0, getHalfDimZ());

			Vector3b startBA = new Vector3b(startAA);
			Vector3b endBA = new Vector3b(endAA);
			startBA.add((byte) 0, getHalfDimY(), (byte) 0);
			endBA.add((byte) 0, getHalfDimY(), (byte) 0);

			Vector3b startBB = new Vector3b(startAA);
			Vector3b endBB = new Vector3b(endAA);
			startBB.add(getHalfDimX(), getHalfDimY(), (byte) 0);
			endBB.add(getHalfDimX(), getHalfDimY(), (byte) 0);

			Vector3b startBC = new Vector3b(startAA);
			Vector3b endBC = new Vector3b(endAA);
			startBC.add(getHalfDimX(), getHalfDimY(), getHalfDimZ());
			endBC.add(getHalfDimX(), getHalfDimY(), getHalfDimZ());

			Vector3b startBD = new Vector3b(startAA);
			Vector3b endBD = new Vector3b(endAA);
			startBD.add((byte) 0, getHalfDimY(), getHalfDimZ());
			endBD.add((byte) 0, getHalfDimY(), getHalfDimZ());

			//			System.err.println("DOING SPLIT FOR LEVEL: "+getLevel()+" MAXLEVEL "+getMaxLevel()+" index "+index);

			if (level < getMaxLevel()) {
				children[0] = new OctreeNode(startAA, endAA, index * 8 + 0, (byte) (level + 1), getMaxLevel(), onServer());
				children[1] = new OctreeNode(startAB, endAB, index * 8 + 1, (byte) (level + 1), getMaxLevel(), onServer());
				children[2] = new OctreeNode(startAC, endAC, index * 8 + 2, (byte) (level + 1), getMaxLevel(), onServer());
				children[3] = new OctreeNode(startAD, endAD, index * 8 + 3, (byte) (level + 1), getMaxLevel(), onServer());
				children[4] = new OctreeNode(startBA, endBA, index * 8 + 4, (byte) (level + 1), getMaxLevel(), onServer());
				children[5] = new OctreeNode(startBB, endBB, index * 8 + 5, (byte) (level + 1), getMaxLevel(), onServer());
				children[6] = new OctreeNode(startBC, endBC, index * 8 + 6, (byte) (level + 1), getMaxLevel(), onServer());
				children[7] = new OctreeNode(startBD, endBD, index * 8 + 7, (byte) (level + 1), getMaxLevel(), onServer());
				for (int i = 0; i < children.length; i++) {
					a += ((OctreeNode) children[i]).split(index * 8 + i, level + 1);
				}
			} else {
				children[0] = new OctreeLeaf(startAA, endAA, index * 8 + 0, (byte) (level + 1), getMaxLevel(), onServer());
				children[1] = new OctreeLeaf(startAB, endAB, index * 8 + 1, (byte) (level + 1), getMaxLevel(), onServer());
				children[2] = new OctreeLeaf(startAC, endAC, index * 8 + 2, (byte) (level + 1), getMaxLevel(), onServer());
				children[3] = new OctreeLeaf(startAD, endAD, index * 8 + 3, (byte) (level + 1), getMaxLevel(), onServer());
				children[4] = new OctreeLeaf(startBA, endBA, index * 8 + 4, (byte) (level + 1), getMaxLevel(), onServer());
				children[5] = new OctreeLeaf(startBB, endBB, index * 8 + 5, (byte) (level + 1), getMaxLevel(), onServer());
				children[6] = new OctreeLeaf(startBC, endBC, index * 8 + 6, (byte) (level + 1), getMaxLevel(), onServer());
				children[7] = new OctreeLeaf(startBD, endBD, index * 8 + 7, (byte) (level + 1), getMaxLevel(), onServer());
				a += 8;
			}
		} else {
			if (level < getMaxLevel()) {
				children[0] = new OctreeNode(index * 8 + 0, (byte) (level + 1), getMaxLevel(), onServer());
				children[1] = new OctreeNode(index * 8 + 1, (byte) (level + 1), getMaxLevel(), onServer());
				children[2] = new OctreeNode(index * 8 + 2, (byte) (level + 1), getMaxLevel(), onServer());
				children[3] = new OctreeNode(index * 8 + 3, (byte) (level + 1), getMaxLevel(), onServer());
				children[4] = new OctreeNode(index * 8 + 4, (byte) (level + 1), getMaxLevel(), onServer());
				children[5] = new OctreeNode(index * 8 + 5, (byte) (level + 1), getMaxLevel(), onServer());
				children[6] = new OctreeNode(index * 8 + 6, (byte) (level + 1), getMaxLevel(), onServer());
				children[7] = new OctreeNode(index * 8 + 7, (byte) (level + 1), getMaxLevel(), onServer());
				for (int i = 0; i < children.length; i++) {
					a += ((OctreeNode) children[i]).split(index * 8 + i, level + 1);
				}
			} else {
				children[0] = new OctreeLeaf(index * 8 + 0, (byte) (level + 1), getMaxLevel(), onServer());
				children[1] = new OctreeLeaf(index * 8 + 1, (byte) (level + 1), getMaxLevel(), onServer());
				children[2] = new OctreeLeaf(index * 8 + 2, (byte) (level + 1), getMaxLevel(), onServer());
				children[3] = new OctreeLeaf(index * 8 + 3, (byte) (level + 1), getMaxLevel(), onServer());
				children[4] = new OctreeLeaf(index * 8 + 4, (byte) (level + 1), getMaxLevel(), onServer());
				children[5] = new OctreeLeaf(index * 8 + 5, (byte) (level + 1), getMaxLevel(), onServer());
				children[6] = new OctreeLeaf(index * 8 + 6, (byte) (level + 1), getMaxLevel(), onServer());
				children[7] = new OctreeLeaf(index * 8 + 7, (byte) (level + 1), getMaxLevel(), onServer());
				a += 8;
			}
		}
		return a;
	}

}
