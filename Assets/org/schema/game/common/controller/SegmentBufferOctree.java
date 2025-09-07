package org.schema.game.common.controller;

import java.util.Arrays;

import javax.vecmath.Vector3f;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.network.ServerInfo;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class SegmentBufferOctree {

	public static final short NOTHING = -2;
	public static final short EMPTY = -1;
	//Dimension is 16 at the moment
	private static final int MAX_FILL = SegmentBufferManager.DIMENSION * SegmentBufferManager.DIMENSION * SegmentBufferManager.DIMENSION;
	private static final int LEVELS[] = new int[]{8, 4, 2};
	//for each octree node there are 256 permutations of filled branches
	private static final short[][][][] iteration = new short[LEVELS.length][][][];
	private static final int LEVELS_SUB[] = new int[]{8, 8 * 8, 8 * 8 * 8}; //584 = 8, 64, 512

	/*
	 * this static code
	 * creates an initial map of all possible
	 * octree node configurations.
	 *
	 * A system using it can iterate the octree avoiding
	 * empty nodes completely (without even the need of iterating
	 * over them or any testing)
	 *
	 * (looks ugly, but works. 4 dim arrays...)
	 */
	static {
		//create the number of octreNodes for each level
		for (int lvl = 0; lvl < LEVELS.length; lvl++) {
			iteration[lvl] = new short[LEVELS_SUB[lvl]][][];
		}

		int lPow = 2;
		int lPowBack = 8;
		Vector3i p = new Vector3i();
		Vector3i r = new Vector3i();
		//for each level create all possible configurations for every
		//coordinate
		for (int lvl = 0; lvl < LEVELS.length; lvl++, lPow *= 2, lPowBack /= 2) {
			int aLvl = lPow;
			for (int z = 0; z < aLvl; z++) {
				for (int y = 0; y < aLvl; y++) {
					for (int x = 0; x < aLvl; x++) {
						p.set(x, y, z);
						int octreeIndex = z * (aLvl * aLvl) + y * aLvl + x;
						iteration[lvl][octreeIndex] = new short[256][];
						for (int perm = 0; perm < 256; perm++) {
							int size = 0;
							for (byte zC = 0; zC < 2; zC++) {
								for (byte yC = 0; yC < 2; yC++) {
									for (byte xC = 0; xC < 2; xC++) {
										int i = (zC) * 4 + (yC) * 2 + (xC);
										int mask = (1 << i);
										if ((perm & mask) == mask) {
											size++;
										}
									}
								}
							}
							iteration[lvl][octreeIndex][perm] = new short[size];
							int c = 0;
							for (byte zC = 0; zC < 2; zC++) {
								for (byte yC = 0; yC < 2; yC++) {
									for (byte xC = 0; xC < 2; xC++) {
										int i = (zC) * 4 + (yC) * 2 + (xC);
										int mask = (1 << i);
										if ((perm & mask) == mask) {
											int prev = lPow / 2;
											int next = lPow * 2;

											//absolute position
											int xx = ((x * LEVELS[lvl]) + (xC * (LEVELS[lvl] / 2))) * SegmentData.SEG;
											int yy = ((y * LEVELS[lvl]) + (yC * (LEVELS[lvl] / 2))) * SegmentData.SEG;
											int zz = ((z * LEVELS[lvl]) + (zC * (LEVELS[lvl] / 2))) * SegmentData.SEG;

											//WARNING, this line takes a lot of time
											//											assert(octreeIndex == getOctreeIndex(xx, yy, zz, lvl, r)):octreeIndex+" -> "+getOctreeIndex(xx, yy, zz, lvl, r);

											int lx = (ByteUtil.divUSeg(xx)) / (LEVELS[lvl] / 2);
											int ly = (ByteUtil.divUSeg(yy)) / (LEVELS[lvl] / 2);
											int lz = (ByteUtil.divUSeg(zz)) / (LEVELS[lvl] / 2);

											int aLvlN = LEVELS[(LEVELS.length - 1) - lvl] * 2;

											//											short mappedOctreeIndex =  (short) (lz*(SegmentBufferManager.DIMENSIONxDIMENSION) + ly * SegmentBufferManager.DIMENSION + lx);
											short mappedOctreeIndex = (short) (lz * (aLvlN * aLvlN) + ly * aLvlN + lx);


											iteration[lvl][octreeIndex][perm][c] = mappedOctreeIndex;

											c++;
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	final SegmentBuffer buffer;
	private byte[][] tree = new byte[LEVELS_SUB.length][];
	private short[] indices = new short[MAX_FILL];
	private long[] lastChanged = new long[MAX_FILL];
	//an initial fill rate, should be adapted according to header of region file read
	private Segment[] backing = new Segment[32];
	private short pointer = 0;
	private long lastDraw;

	public SegmentBufferOctree(SegmentBuffer buffer) {
		Arrays.fill(indices, NOTHING);
		this.buffer = buffer;
		for (int i = 0; i < LEVELS_SUB.length; i++) {
			tree[i] = new byte[LEVELS_SUB[i]];
		}
	}

	public static boolean testAabbAgainstAabb2(
			Vector3i aabbMin1, Vector3i aabbMax1,
			Vector3i aabbMin2, Vector3i aabbMax2) {
		boolean overlap = true;
		overlap = (aabbMin1.x > aabbMax2.x || aabbMax1.x < aabbMin2.x) ? false : overlap;
		overlap = (aabbMin1.z > aabbMax2.z || aabbMax1.z < aabbMin2.z) ? false : overlap;
		overlap = (aabbMin1.y > aabbMax2.y || aabbMax1.y < aabbMin2.y) ? false : overlap;
		return overlap;
	}

	public static boolean testAabbAgainstAabb2(
			int aabbMin1x, int aabbMin1y, int aabbMin1z,
			int aabbMax1x, int aabbMax1y, int aabbMax1z,
			int aabbMin2x, int aabbMin2y, int aabbMin2z,
			int aabbMax2x, int aabbMax2y, int aabbMax2z) {
		boolean overlap = true;
		overlap = (aabbMin1x > aabbMax2x || aabbMax1x < aabbMin2x) ? false : overlap;
		overlap = (aabbMin1z > aabbMax2z || aabbMax1z < aabbMin2z) ? false : overlap;
		overlap = (aabbMin1y > aabbMax2y || aabbMax1y < aabbMin2y) ? false : overlap;
		return overlap;
	}

	private static boolean intersects(int lvl,
	                                  int startX, int startY, int startZ,
	                                  int fromX, int fromY, int fromZ,
	                                  int toX, int toY, int toZ, SegmentBuffer buffer) {
		int endX = startX + (LEVELS[lvl] / 2) * SegmentData.SEG;
		int endY = startY + (LEVELS[lvl] / 2) * SegmentData.SEG;
		int endZ = startZ + (LEVELS[lvl] / 2) * SegmentData.SEG;

		//		if(lvl == 0 && !buffer.getSegmentController().isOnServer() && ((GameClientState)buffer.getSegmentController().getState()).getCurrentSectorId() == buffer.getSegmentController().getSectorId()){
		//			System.err.println("TESTING: "+startX+", "+startY+", "+startZ+" -> "+endX+", "+endY+", "+endZ+" TO "+fromX+", "+fromY+", "+fromZ+" -> "+toX+", "+toY+", "+toZ);
		//		}

		boolean overlaps = testAabbAgainstAabb2(
				startX, startY, startZ,
				endX, endY, endZ,
				fromX, fromY, fromZ,
				toX, toY, toZ);
		return overlaps;
	}

	private static int getOctreeIndex(int x, int y, int z, int lvl, Vector3i regionStart) {
		int lx = (ByteUtil.divUSeg(x) - regionStart.x) / (LEVELS[lvl]);
		int ly = (ByteUtil.divUSeg(y) - regionStart.y) / (LEVELS[lvl]);
		int lz = (ByteUtil.divUSeg(z) - regionStart.z) / (LEVELS[lvl]);

		int aLvl = LEVELS[(LEVELS.length - 1) - lvl];

		assert (lx >= 0 && ly >= 0 && lz >= 0) : lx + ", " + ly + ", " + lz + "; " + aLvl + "; " + x + ". " + y + ", " + z + " - " + regionStart;
		assert (lx < aLvl && ly < aLvl && lz < aLvl) : lx + ", " + ly + ", " + lz + "; lvl: " + lvl + "; aLvl " + aLvl + "; " + x + ". " + y + ", " + z + " - " + regionStart;

		return lz * (aLvl * aLvl) + ly * aLvl + lx;
	}

	private static int getOctreeIndex(Vector3i pos, int lvl, Vector3i regionStart) {
		return getOctreeIndex(pos.x, pos.y, pos.z, lvl, regionStart);
	}

	private static final int getLocalIndex(int xA, int yA, int zA, int lvl, Vector3i regionStart) {
		int x = (ByteUtil.divUSeg(xA) - regionStart.x) / (LEVELS[lvl] / 2);
		int y = (ByteUtil.divUSeg(yA) - regionStart.y) / (LEVELS[lvl] / 2);
		int z = (ByteUtil.divUSeg(zA) - regionStart.z) / (LEVELS[lvl] / 2);
		return (z % 2) * 4 + (y % 2) * 2 + (x % 2);
	}

	public void clear() {
		clearArrays();
	}

	/**
	 * this method places an array in
	 * the linear array and assigns the position
	 * of the data
	 * to the index array at the index of the segment
	 *
	 * @param seg
	 */
	private void placeInArray(Segment seg) {
		short index = (short) SegmentBuffer.getIndex(seg.pos.x, seg.pos.y, seg.pos.z, buffer.getRegionStart());
		if (!seg.isEmpty()) {
			assert (indices[index] < 0);

			//grow
			if (backing.length == pointer) {
				int newSize = backing.length * 2;
				backing = Arrays.copyOf(backing, newSize);
			}
			backing[pointer] = seg;
			indices[index] = pointer;
			pointer++;
		} else {
			indices[index] = EMPTY;
		}
	}

	private void replaceInArray(Segment seg) {
		short index = (short) SegmentBuffer.getIndex(seg.pos.x, seg.pos.y, seg.pos.z, buffer.getRegionStart());
		backing[indices[index]] = seg;
	}

	private boolean existsinArray(Segment seg) {
		short index = (short) SegmentBuffer.getIndex(seg.pos.x, seg.pos.y, seg.pos.z, buffer.getRegionStart());
		return indices[index] != NOTHING;
	}

	private boolean existsinArray(int posX, int posY, int posZ) {
		short index = (short) SegmentBuffer.getIndex(posX, posY, posZ, buffer.getRegionStart());
		//		if(buffer.getSegmentController().isOnServer() && buffer.getSegmentController() instanceof Planet){
		//			System.err.println(buffer.getSegmentController()+" EXISTS: "+posX+", "+posY+", "+posZ+": "+indices[index]);
		//		}
		return indices[index] != NOTHING;
	}

	private boolean isEmptyinArray(Segment seg) {
		short index = (short) SegmentBuffer.getIndex(seg.pos.x, seg.pos.y, seg.pos.z, buffer.getRegionStart());
		return indices[index] == EMPTY;
	}

	private void clearArrays() {
		Arrays.fill(indices, NOTHING);
		Arrays.fill(backing, null);
		for (int i = 0; i < LEVELS_SUB.length; i++) {
			Arrays.fill(tree[i], (byte) 0);
		}
		Arrays.fill(lastChanged, 0L);
		pointer = 0;

	}

	private void removeFromArray(Segment seg, short newState) {
		removeFromArray(seg.pos.x, seg.pos.y, seg.pos.z, newState);
	}

	/**
	 * this will dynamically remove
	 * a segment from the linear array
	 * if a segment from the middle of the array
	 * is removed it will get replaced by the value
	 * that is currently at the end of the array,
	 * ans the array pointer is decremented.
	 * <p/>
	 * Should the array pointer wander below a certain threshold
	 * the array will dynamically shrink
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	private void removeFromArray(int x, int y, int z, short newState) {
		final short index = (short) SegmentBuffer.getIndex(x, y, z, buffer.getRegionStart());
		removeFromArray(index, newState);
	}

	private void removeFromArray(short index, short newState) {
		short largest = (short) (pointer - 1);
		short current = indices[index];
		indices[index] = newState;
		lastChanged[index] = 0;
		if (current >= 0) {
			//if segment was not empty
			backing[current] = null;

			if (largest > 0 && current != largest) {
				final short indexLargest = (short) SegmentBuffer.getIndex(
						backing[largest].pos.x,
						backing[largest].pos.y,
						backing[largest].pos.z, buffer.getRegionStart());

				backing[current] = backing[largest];
				backing[largest] = null;
				indices[indexLargest] = current;
			}
			pointer--;

			//shrink
			if (backing.length > 32 && backing.length / 2 == pointer) {
				int newSize = backing.length / 2;
				backing = Arrays.copyOf(backing, newSize);
			}
		}
	}

	public void setEmpty(int x, int y, int z) {
		removeFromArray(x, y, z, EMPTY);
		updateTree(LEVELS.length - 1, x, y, z, true);
	}

	public int getSegmentState(int x, int y, int z) {
		short index = (short) SegmentBuffer.getIndex(x, y, z, buffer.getRegionStart());
		return indices[index];
	}

	public boolean isEmptyTree(Vector3i pos) {
		return isEmptyTree(0, pos);
	}

	public boolean iterateOverNonEmptyElement(
			SegmentBufferIteratorInterface iteratorImpl) {
		for (int i = 0; i < pointer; i++) {
			boolean handle = iteratorImpl.handle(backing[i], lastChanged[(short) SegmentBuffer.getIndex(backing[i].pos.x, backing[i].pos.y, backing[i].pos.z, buffer.getRegionStart())]);
			if (!handle) {
				return handle;
			}
		}
		return true;
	}

	public boolean iterateOverEveryElement(SegmentBufferIteratorEmptyInterface iteratorImpl) {
		int i = 0;
		for (int z = 0; z < SegmentBufferManager.DIMENSION; z++) {
			for (int y = 0; y < SegmentBufferManager.DIMENSION; y++) {
				for (int x = 0; x < SegmentBufferManager.DIMENSION; x++) {
					short s = indices[i];
					boolean handle = true;
					//					assert(indices[i] == NOTHING || lastChanged[i] > 0):indices[i]+" -> "+x+", "+y+", "+z+" on "+buffer.getSegmentController();
					if (s >= 0) {
						handle = iteratorImpl.handle(backing[s], lastChanged[i]);
					} else if (s == EMPTY) {
						handle = iteratorImpl.handleEmpty(
								x * SegmentData.SEG + buffer.getRegionStartBlock().x, 
								y * SegmentData.SEG + buffer.getRegionStartBlock().y, 
								z * SegmentData.SEG + buffer.getRegionStartBlock().z, lastChanged[i]);
					}
					i++;

					if (!handle) {
						assert (false);
						return handle;
					}
				}
			}
		}
		return true;
	}

	public void draw() {
		draw(0);
	}

	public boolean iterateOverNonEmptyElementRange(SegmentBufferIteratorInterface iteratorImpl, Vector3i from, Vector3i to) {
		return iterateOverNonEmptyElementRange(iteratorImpl,
				from.x, from.y, from.z,
				to.x, to.y, to.z);
	}

	public boolean iterateOverNonEmptyElementRange(SegmentBufferIteratorInterface iteratorImpl,
	                                               int fX, int fY, int fZ,
	                                               int tX, int tY, int tZ
	) {

		int fromX = fX - buffer.getRegionStartBlock().x;
		int fromY = fY - buffer.getRegionStartBlock().y;
		int fromZ = fZ - buffer.getRegionStartBlock().z;
		int toX = tX - buffer.getRegionStartBlock().x;
		int toY = tY - buffer.getRegionStartBlock().y;
		int toZ = tZ - buffer.getRegionStartBlock().z;

		int lvl = 0;
		for (int octreeIndex = 0; octreeIndex < LEVELS_SUB[lvl]; octreeIndex++) {
			int c = (tree[lvl][octreeIndex] & 0xFF);

			assert (lvl < iteration.length) : lvl + " -> " + iteration.length;

			assert (octreeIndex < iteration[lvl].length) : octreeIndex + " -> " + iteration[lvl].length;

			assert (c < iteration[lvl][octreeIndex].length) : c + " -> " + iteration[lvl][octreeIndex].length;

			short[] iterations = iteration[lvl][octreeIndex][c];

			assert (c != 255 || iterations.length == 8) : iterations.length;

			if (isEmptyTree(lvl, octreeIndex)) {
				continue;
			}
			for (int i = 0; i < iterations.length; i++) {
				short iteration = iterations[i];

				int o = iteration;

				int aLvl = LEVELS[(LEVELS.length - 1) - (lvl)];
				int fac = (aLvl * 2);

				int startZ = o / (fac * fac);
				o -= startZ * (fac * fac);

				int startY = o / fac;
				o -= startY * fac;

				int startX = o;

				startX *= SegmentData.SEG * (LEVELS[lvl] / 2);
				startY *= SegmentData.SEG * (LEVELS[lvl] / 2);
				startZ *= SegmentData.SEG * (LEVELS[lvl] / 2);

				

				if (intersects(0, startX, startY, startZ,
						fromX,
						fromY,
						fromZ,
						toX,
						toY,
						toZ, buffer)) {

					boolean term = iterateOverNonEmptyElementRangeRecursive(lvl + 1, fromX, fromY, fromZ, toX, toY, toZ, iterations[i], iteratorImpl);
					if (!term) {
						//iteration has been canceled
						return term;
					}
				}

			}
		}

		return true;
	}

	private boolean iterateOverNonEmptyElementRangeRecursive(int lvl,
	                                                         int fromX, int fromY, int fromZ,
	                                                         int toX, int toY, int toZ,
	                                                         int octreeIndex, SegmentBufferIteratorInterface iteratorImpl) {

		short[] iterations = iteration[lvl][octreeIndex][(tree[lvl][octreeIndex] & 0xFF)];

		for (int i = 0; i < iterations.length; i++) {
			short iteration = iterations[i];

			int o = iteration;

			int aLvl = LEVELS[(LEVELS.length - 1) - (lvl)];
			int fac = (aLvl * 2);

			int startZ = o / (fac * fac);
			o -= startZ * (fac * fac);

			int startY = o / fac;
			o -= startY * fac;

			int startX = o;

			startX *= SegmentData.SEG * (LEVELS[lvl] / 2);
			startY *= SegmentData.SEG * (LEVELS[lvl] / 2);
			startZ *= SegmentData.SEG * (LEVELS[lvl] / 2);


			if (intersects(lvl,
					startX,
					startY,
					startZ,
					fromX,
					fromY,
					fromZ,
					toX,
					toY,
					toZ, buffer)) {
				boolean handle;
				if (lvl == LEVELS.length - 1) {
					Segment fromRelativeCords = getFromRelativeCords(startX, startY, startZ);
					assert (fromRelativeCords != null) : startX + ", " + startY + ", " + startZ;
					handle = iteratorImpl.handle(fromRelativeCords, lastChanged[(short) SegmentBuffer.getIndexAbsolute(startX, startY, startZ)]);
				} else {
					handle = iterateOverNonEmptyElementRangeRecursive(lvl + 1, fromX, fromY, fromZ, toX, toY, toZ, iterations[i], iteratorImpl);
				}
				if (!handle) {
					return handle;
				}
			}
		}
		return true;
	}

	private boolean isEmptyTree(int lvl, Vector3i pos) {
		int octreeIndex = getOctreeIndex(pos, lvl, buffer.getRegionStart());
		boolean empty = isEmptyTree(lvl, octreeIndex);
		if (!empty) {
			if (lvl == LEVELS.length - 1) {
				return empty;
			} else {
				return isEmptyTree(lvl + 1, pos);
			}
		}
		return true;
	}

	public void remove(int x, int y, int z) {
		removeFromArray(x, y, z, NOTHING);
		updateTree(LEVELS.length - 1, x, y, z, true);
	}

	public void remove(Segment seg) {
		removeFromArray(seg, NOTHING);
		updateTree(LEVELS.length - 1, seg.pos.x, seg.pos.y, seg.pos.z, true);
	}


	public void getSegment(int x, int y, int z,
	                       SegmentRetrieveCallback callback) {
		getSegment(x, y, z, callback, buffer.getRegionStart());
	}

	private void getSegment(int x, int y, int z,
	                        SegmentRetrieveCallback callback, Vector3i regionStart) {
		short index = (short) SegmentBuffer.getIndex(x, y, z, regionStart);
		short valueIndex = indices[index];
		callback.pos.set(x, y, z);
		callback.abspos.set(x >> 4, y >> 4, z >> 4);

		if (valueIndex < 0) {
			callback.state = valueIndex;
			callback.segment = null;
		} else {
			callback.state = 1;
			callback.segment = backing[valueIndex];
			assert (backing[valueIndex].pos.equals(x, y, z)) : backing[valueIndex].pos + "; " + x + ", " + y + ", " + z;
		}
		assert (callback.segment == null || callback.pos.equals(callback.segment.pos)) : callback.pos + "; " + callback.segment.pos;
	}

	public Segment getSegment(int x, int y, int z, Vector3i regionStart) {

		short index = (short) SegmentBuffer.getIndex(x, y, z, regionStart);
		short valueIndex = indices[index];
		if (valueIndex < 0) {
			if (valueIndex == EMPTY) {
				Segment emptySegment;
				if (buffer.getSegmentController().isOnServer()) {
					emptySegment = new RemoteSegment(buffer.getSegmentController());
				} else {
					emptySegment = new DrawableRemoteSegment(buffer.getSegmentController());
				}

				emptySegment.setPos(x, y, z);
				//				try{
				//					throw new IllegalArgumentException("Requested empty segment (not an error, but should not happen for more performance)");
				//				}catch(Exception e){
				//					e.printStackTrace();
				//				}
				return emptySegment;
			}
			return null;
		} else {
			return backing[valueIndex];
		}
	}

	private Segment getFromRelativeCords(int xA, int yA, int zA) {

		short index = (short) SegmentBuffer.getIndexAbsolute(xA, yA, zA);
		if (indices[index] < 0) {
			return null;
		} else {
			return backing[indices[index]];
		}
	}

	public void insert(Segment seg) {
		if (!existsinArray(seg)) {
			placeInArray(seg);

		} else if (isEmptyinArray(seg) == seg.isEmpty()) {
			//update segment bug don't update octree
			if (!seg.isEmpty()) {
				replaceInArray(seg);
			}
		} else {
			//update needed
			if (seg.isEmpty()) {
				//update octree: segment became empty
				removeFromArray(seg, NOTHING); //remove physical
				placeInArray(seg); //will place empty flag in array
			} else {
				//update octree: segment became non-empty
				placeInArray(seg); //will overwrite empty
			}
		}
		boolean wasEmpty = isEmptyTree(seg.pos);
		updateTree(LEVELS.length - 1, seg.pos.x, seg.pos.y, seg.pos.z, seg.isEmpty());
		assert (!wasEmpty || (seg.isEmpty() || !isEmptyTree(seg.pos))) : seg.pos + "; " + (seg.isEmpty() ? "EMPTY" : "NON-EMPTY") + "; " + wasEmpty + "; " + seg.isEmpty() + " -> " + isEmptyTree(seg.pos);
	}

	private void draw(int lvl) {
		assert (false);
		if (System.currentTimeMillis() - lastDraw > 700) {
			for (int octreeIndex = 0; octreeIndex < LEVELS_SUB[lvl]; octreeIndex++) {
				int c = (tree[lvl][octreeIndex] & 0xFF);

				assert (lvl < iteration.length) : lvl + " -> " + iteration.length;

				assert (octreeIndex < iteration[lvl].length) : octreeIndex + " -> " + iteration[lvl].length;

				assert (c < iteration[lvl][octreeIndex].length) : c + " -> " + iteration[lvl][octreeIndex].length;

				short[] iterations = iteration[lvl][octreeIndex][c];

				assert (c != 255 || iterations.length == 8) : iterations.length;

				if (isEmptyTree(lvl, octreeIndex)) {
					continue;
				}
				for (int i = 0; i < iterations.length; i++) {
					short iteration = iterations[i];

					int o = iteration;

					int aLvl = LEVELS[(LEVELS.length - 1) - (lvl)];
					int fac = (aLvl * 2);

					int startZ = o / (fac * fac);
					o -= startZ * (fac * fac);

					int startY = o / fac;
					o -= startY * fac;

					int startX = o;

					startX *= SegmentData.SEG * (LEVELS[lvl] / 2);
					startY *= SegmentData.SEG * (LEVELS[lvl] / 2);
					startZ *= SegmentData.SEG * (LEVELS[lvl] / 2);

					Vector3f start = new Vector3f(buffer.getRegionStartBlock().x - SegmentData.SEG_HALF, buffer.getRegionStartBlock().y - SegmentData.SEG_HALF, buffer.getRegionStartBlock().z - SegmentData.SEG_HALF);
					Vector3f end = new Vector3f(start);

					start.x += startX;
					start.y += startY;
					start.z += startZ;

					int endX = startX + ((LEVELS[lvl] / 2) * SegmentData.SEG);
					int endY = startY + ((LEVELS[lvl] / 2) * SegmentData.SEG);
					int endZ = startZ + ((LEVELS[lvl] / 2) * SegmentData.SEG);

					end.x += endX;
					end.y += endY;
					end.z += endZ;

					DebugBox b = new DebugBox(start, end, buffer.getSegmentController().getWorldTransform(), lvl == 0 ? 1 : 0, lvl == 1 ? 1 : 0, lvl == 2 ? 1 : 0, 1);
					DebugDrawer.boxes.add(b);
				}
			}
			if (lvl < LEVELS.length - 1) {
				draw(lvl + 1);
			}
			lastDraw = System.currentTimeMillis();
		}
	}

	/**
	 * this method will recursively update all nodes of the octree
	 * from the bottom up.
	 * <p/>
	 * The mask will be created according to the value of the lower
	 * update
	 * <p/>
	 * e.g. if there is only one element in the octree and it gets removed (set to empty)
	 * -> removed from finest granularity (4x4x4 blocks), the information that that block
	 * is now completely empty is recursively read by the next upper level (8x8x8), and so on
	 *
	 * @param lvl
	 * @param x
	 * @param y
	 * @param z
	 * @param empty
	 */
	private void updateTree(int lvl, int x, int y, int z, boolean empty) {

		int octreeIndex = getOctreeIndex(x, y, z, lvl, buffer.getRegionStart());

		boolean wasEmpty = isEmptyTree(lvl, octreeIndex);

		updateOcree(lvl, x, y, z, octreeIndex, empty);

		boolean nowEmpty = isEmptyTree(lvl, octreeIndex);

		//		if(lvl == 0 && buffer.getRegionStart().length() == 0 && !buffer.getSegmentController().isOnServer() && ((GameClientState)buffer.getSegmentController().getState()).getCurrentSectorId() == buffer.getSegmentController().getSectorId()){
		//			System.err.println("LVL "+lvl+" INSERT: "+(empty ? "EMPTY" : "SOLID")+" INTO "+x+", "+y+", "+z+" -> "+octreeIndex+": "+wasEmpty+" -> "+nowEmpty);
		//		}
		//

		assert (empty || !nowEmpty);

		if ((lvl - 1) >= 0) {
			//recusively update if leaf state changed
			updateTree(lvl - 1, x, y, z, nowEmpty);
		}
	}

	private void updateOcree(int lvl, int x, int y, int z, int octreeIndex, boolean empty) {
		int localIndex = getLocalIndex(x, y, z, lvl, buffer.getRegionStart());
		//from 0 to 7;
		byte mask = (byte) (1 << localIndex);
		if (empty) {
			tree[lvl][octreeIndex] &= ~mask;
		} else {
			tree[lvl][octreeIndex] |= mask;
		}
	}

	private boolean isEmptyTree(int lvl, int octreeIndex) {
		return tree[lvl][octreeIndex] == (0 & 0xFF);
	}

	public boolean contains(Vector3i key) {
		return existsinArray(key.x, key.y, key.z);
	}

	public boolean contains(int x, int y, int z) {
		return existsinArray(x, y, z);
	}

	public long getLastChanged(Vector3i pos) {
		return lastChanged[(short) SegmentBuffer.getIndex(pos.x, pos.y, pos.z, buffer.getRegionStart())];
	}

	public void setLastChanged(Vector3i pos, long ts) {
		if(ts > ServerInfo.curtime + 100000L){
			try {
				throw new Exception("Tried to set last Changed to future "+ServerInfo.curtime+"; "+lastChanged);
			} catch (Exception e) {
				e.printStackTrace();
				assert(false);
			}
		}
		lastChanged[(short) SegmentBuffer.getIndex(pos.x, pos.y, pos.z, buffer.getRegionStart())] = ts;
	}

	public EWAHCompressedBitmap applyBitMap(EWAHCompressedBitmap bitMap) {
		int i = 0;

		EWAHCompressedBitmap bm = new EWAHCompressedBitmap((SegmentBufferManager.DIMENSION * SegmentBufferManager.DIMENSION * SegmentBufferManager.DIMENSION) / 64);
		for (int z = 0; z < SegmentBufferManager.DIMENSION; z++) {
			for (int y = 0; y < SegmentBufferManager.DIMENSION; y++) {
				for (int x = 0; x < SegmentBufferManager.DIMENSION; x++) {
					short s = indices[i];
					if (s < 0) {
						//set true on nothing or empty, so when comparing, the bits that have been set by the header but are not yet loaded
						//for the octree are kept while all the newly added segments will fail

						//segments that were not empty in header and are now empty are returned as "nonemtpy", which leads to a normal
						//request of those segemnts (should be very rare and very few)
						bm.set(i);
						//						System.err.println(buffer.getRegionStart()+" INCLUDING HANDLING "+i+" -> "+x+", "+y+", "+z);
					}

					i++;
				}
			}
		}
		assert (bitMap != null);
		return bitMap.and(bm);
	}

	public boolean iterateOverUnloaded(
			SegmentBufferIteratorEmptyInterface iteratorImpl) {
		int i = 0;
		for (int z = 0; z < SegmentBufferManager.DIMENSION; z++) {
			for (int y = 0; y < SegmentBufferManager.DIMENSION; y++) {
				for (int x = 0; x < SegmentBufferManager.DIMENSION; x++) {
					short s = indices[i];
					boolean handle = true;
					if (s != EMPTY) {
						handle = iteratorImpl.handleEmpty(
								x * SegmentData.SEG + buffer.getRegionStartBlock().x, 
								y * SegmentData.SEG + buffer.getRegionStartBlock().y, 
								z * SegmentData.SEG + buffer.getRegionStartBlock().z, lastChanged[i]);
					}
					if (!handle) {
						return handle;
					}

					i++;
				}
			}
		}
		return true;
	}

}
