package org.schema.game.client.view.cubes.occlusion;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.SegmentBufferOctree;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;

public class OcclusionOld {

	public static final float COLOR_PERM = 15;
	public static final byte COLOR_PERM_BYTE = 15;
	public static final byte COLOR_LIGHT_PERM_BYTE = 13;
	private static final int cacheRange = 8;
	private static final int cacheFak = 4;
	private static final int cacheRangeMul = (cacheRange + 1);
	private static final int cacheRange_X_cacheRange = cacheRangeMul
			* cacheRangeMul;
	private static final int[] relativeIndexBySide = new int[]{
			getContainIndex(-1, -1, 0), -getContainIndex(-1, -1, 0),
			getContainIndex(-1, 0, -1), -getContainIndex(-1, 0, -1),
			getContainIndex(0, -1, -1), -getContainIndex(0, -1, -1),

	};
	public static int minusOneToSeventeenIndices[] = new int[18 * 18 * 18];
	public static boolean minusOneToSeventeenValid[] = new boolean[18 * 18 * 18];
	public static int minusOneToSeventeenInfoIndexDiv3[] = new int[18 * 18 * 18];
	public static int minusOneToSeventeenOGIndex[] = new int[18 * 18 * 18];
	public static boolean normalValid[] = new boolean[18 * 18 * 18];
	public static boolean allInside[] = new boolean[18 * 18 * 18];
	public static Vector3i dbPos = new Vector3i(1, 9, 9);

	static {

		int i = 0;
		for (byte z = -1; z < 17; z++) {
			for (byte y = -1; y < 17; y++) {
				for (byte x = -1; x < 17; x++) {
					minusOneToSeventeenIndices[i] = getContainIndex(x, y, z);
					minusOneToSeventeenValid[i] = SegmentData.valid(x, y, z);
					minusOneToSeventeenOGIndex[i] = getOGIndex(x, y, z);
					normalValid[i] = normalValid(x, y, z);
					allInside[i] = x < SegmentData.SEG_MINUS_ONE
							&& y < SegmentData.SEG_MINUS_ONE
							&& z < SegmentData.SEG_MINUS_ONE && x >= 1
							&& y >= 1 && z >= 1;

					if (minusOneToSeventeenValid[i]) {
						minusOneToSeventeenInfoIndexDiv3[i] = SegmentData
								.getInfoIndex(x, y, z) / 3;
					}
					i++;
				}
			}
		}
	}

	final short[] contain = new short[18 * 18 * 18];
	// private float[] occlusion;
	private final Sample sample;
	private final int ray_count = EngineSettings.LIGHT_RAY_COUNT.getInt(); // 128;
	private final NormalizerOld normalizer;
	private final Vector3i testObj = new Vector3i(-16, 32, 32);
	private final Vector3b helperPos = new Vector3b();
	private final Vector3b helperPosGlobal = new Vector3b();
	private final short[] containAct = new short[18 * 18 * 18];
	private final float[] occlusion = new float[18 * 18 * 18 * 6];
	private final float[] gather = new float[18 * 18 * 18 * 6];
	private final float[] light = new float[18 * 18 * 18 * 6 * 4];
	private final byte[] ambience = new byte[18 * 18 * 18 * 6];
	private final byte[] airBlocksWithNeighbors = new byte[18 * 18 * 18 * 3];
	private final byte[] affectedBlocksFromAirBlocks = new byte[18 * 18 * 18 * 4];
	private final Vector3i posTmp = new Vector3i();
	private final Vector3i outSegPos = new Vector3i();
	private final SegmentRetrieveCallback[] cacheByPos = new SegmentRetrieveCallback[(cacheRange + 1)
			* (cacheRange + 1) * (cacheRange + 1)]; // 0,1,2,3
	private final int[] cacheMissesByPos = new int[(cacheRange + 1) * (cacheRange + 1) * (cacheRange + 1)];
	private final Vector3i absSegPos = new Vector3i();
	/**
	 * A dummy is used if the adjacent sector is out of bounds
	 */
	private final Segment dummy = new RemoteSegment(null);
	private final SegmentRetrieveCallback callback = new SegmentRetrieveCallback();
	private final SegmentRetrieveCallback callbackRay = new SegmentRetrieveCallback();
	private final Vector3i callBackPos = new Vector3i();
	int debugOG = -1;
	private SegmentData data;
	private CubeMeshBufferContainer container;
	private int airBlocksWithNeighborsPointer = 0;
	private boolean failed = false;
	private SegmentRetrieveCallback nCallback = new SegmentRetrieveCallback();
	private SegmentRetrieveCallback nCallbackTest = new SegmentRetrieveCallback();
	public OcclusionOld() {

		for (int i = 0; i < cacheByPos.length; i++) {
			cacheByPos[i] = new SegmentRetrieveCallback();
		}

		normalizer = new NormalizerOld();
		sample = new Sample(ray_count);
		sample.initRays();

	}

	private static final float getArray(int ogIndex, float[] in, int index) {
		return in[ogIndex + index];
	}

	// private float[] getOcclusion(int ogIndex, float[] out){
	// return get(ogIndex, occlusion, out);
	// }
	static final int getContainIndex(int x, int y, int z) {
		// plus one: -1 to 17
		return ((z + 1) * 324 + (y + 1) * 18 + (x + 1));
	}

	private static final int getOGIndex(int x, int y, int z) {
		return getContainIndex(x, y, z) * 6;
	}

	public static final boolean normalValid(byte x, byte y, byte z) {
		return (z < 16) && (y < 16) && (x < 16) && (z >= 0) && (y >= 0)
				&& (x >= 0);
	}

	public static final boolean normalValid(int x, int y, int z) {
		return (z < 16) && (y < 16) && (x < 16) && (z >= 0) && (y >= 0)
				&& (x >= 0);
	}

	public static final boolean ogValid(byte x, byte y, byte z) {
		return (z < 17) && (y < 17) && (x < 17) && (z >= -1) && (y >= -1)
				&& (x >= -1);
	}

	// /**
	// * -2, -1, 0 1, 2
	// * +2 +2, +2 +2 +2
	// * -> 0, 1, 2, 3, 4
	// *
	// *
	// * @param in
	// * @return
	// */
	// private int getCacheIndex(int in){
	// return ByteUtil.div16(in) + cacheFak;
	// }

	public static final boolean ogValid(int x, int y, int z) {
		return (z < 17) && (y < 17) && (x < 17) && (z >= -1) && (y >= -1)
				&& (x >= -1);
	}

	public static void main(String[] sdf) {
		for (byte z = 0; z < 16; z++) {
			for (byte y = 0; y < 16; y++) {
				for (byte x = 0; x < 16; x++) {
					System.err.println("OK: " + ((x | y | z) & 0xf0));
					assert (((x | y | z) & 0xf0) == 0) : x + ", " + y + ", " + z;
					;
				}
			}
		}
	}

	// public static final int RIGHT = 0;
	// public static final int LEFT = 1;
	// public static final int TOP = 2;
	// public static final int BOTTOM = 3;
	// public static final int FRONT = 4;
	// public static final int BACK = 5;
	public static final boolean valid(int x, int y, int z) {
		return (z < SegmentData.SEG) && (y < SegmentData.SEG) && (x < SegmentData.SEG)
				&& (z >= 0) && (y >= 0) && (x >= 0);
	}

	private final void addGather(int index, float value) {
		gather[index] = (Math.min(1f, gather[index] + value));

	}

	// private float[] get(int ogIndex, float[] in, float[] out){
	//
	// for(int i = 0; i < 6; i++){
	// out[i] = in[ogIndex+i];
	// }
	// return out;
	// }
	private final void addOcclusion(int index, float value) {

		occlusion[index] = (occlusion[index] + value);
	}

	private void applyAndNormalize() {
		boolean isVisible = false;
		int i = 0;
		for (byte z = -1; z < 17; z++) {
			for (byte y = -1; y < 17; y++) {
				for (byte x = -1; x < 17; x++) {

					helperPosGlobal.set(x, y, z);
					int containIndex = minusOneToSeventeenIndices[i];// getContainIndex(helperPosGlobal.x,
					// helperPosGlobal.y,
					// helperPosGlobal.z);
					// assert(getContainIndex(helperPosGlobal.x,
					// helperPosGlobal.y, helperPosGlobal.z) ==
					// minusOneToSeventeenIndices[i]);
					short type = contain[containIndex];
					if (type != 0) {
						int index = 0;
						if (minusOneToSeventeenValid[i]
								&& data.containsFast(index = SegmentData
								.getInfoIndex(x, y, z))) {

							ElementInformation info = ElementKeyMap
									.getInfo(FastMath.abs(type));
							byte vis = getVisibilityMask(containIndex,
									helperPosGlobal, info);
							container.setVis(
									minusOneToSeventeenInfoIndexDiv3[i], vis);

							isVisible = isVisible || vis > 0;
						}

					}
					if (type <= 0) {
						// type <= 0 == air or transparent. apply lighting to
						// solid blocks from air blocks

						boolean needsCheckValid = !normalValid[i];
						// assert(getContainIndex(helperPosGlobal.x,
						// helperPosGlobal.y, helperPosGlobal.z) ==
						// minusOneToSeventeenIndices[i]);
						int containIndexAir = minusOneToSeventeenIndices[i];// getContainIndex(helperPosGlobal.x,
						// helperPosGlobal.y,
						// helperPosGlobal.z);

						for (int sideId = 0; sideId < 6; sideId++) {
							//
							setLightFromAirBlock(helperPosGlobal,
									containIndexAir,
									Element.DIRECTIONSb[sideId], sideId,
									Element.OPPOSITE_SIDE[sideId],
									needsCheckValid);
							//
						}

					}
					i++;
				}

			}
		}
		if (isVisible) {
			((DrawableRemoteSegment) data.getSegment())
					.setHasVisibleElements(true);
			int infoIndex = 0;
			for (byte z = 0; z < 16; z++) {
				for (byte y = 0; y < 16; y++) {
					for (byte x = 0; x < 16; x++) {
						posTmp.set(x, y, z);
						normalize(posTmp, data, infoIndex);
						//						for(int si = 0; si < 6; si++){
						//							testNormalizeFill(posTmp, data, si);
						//						}
						infoIndex += 3;
					}
				}
			}
		} else {
			((DrawableRemoteSegment) data.getSegment())
					.setHasVisibleElements(false);
		}
	}

	public void compute(SegmentData data, CubeMeshBufferContainer container) {
		this.data = data;
		this.container = container;
		failed = false;

		occlude();

		if (failed) {
			((DrawableRemoteSegment) data.getSegment()).occlusionFailed = true;
			((DrawableRemoteSegment) data.getSegment()).occlusionFailTime = System
					.currentTimeMillis();
		}
	}

	public final short get(byte x, byte y, byte z) {
		return data.getType(x, y, z);
	}

	/**
	 * @return the container
	 */
	public CubeMeshBufferContainer getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(CubeMeshBufferContainer container) {
		this.container = container;
	}

	float getLight(Vector3i pos, int sideId, int coordinate) {
		int index = 0;
		if (ogValid(pos.x, pos.y, pos.z)
				&& contain[index = getContainIndex(pos.x, pos.y, pos.z)] != 0) {
			// index*6 == ogIndex
			return light[index * 6 * 4 + sideId * 4 + coordinate];
		}
		return -1;
	}

	//	public boolean getLightVec(boolean ownPos, Vector4i pos, Vector3f out, int blockStyle, byte orientation, boolean active, AlgorithmParameters p) {
	//
	//
	//		int sideId = pos.w;
	//		int index = 0;
	//
	//		Vector3i dir = Element.DIRECTIONSi[sideId];
	//		int x = pos.x;
	//		int y = pos.y;
	//		int z = pos.z;
	//
	//		if(!ownPos){
	//			short type = 0;
	//			if(blockStyle != 0 && blockStyle != 3){
	//				Vector3i oppsite = Element.DIRECTIONSi[Element.getOpposite(sideId)];
	//				if(p.sid != sideId){
	//
	//
	//
	//					type = contain[index = getContainIndex(x+oppsite.x, y+oppsite.y, z+oppsite.z)];
	//
	//					if(type != 0){
	//						if(getContainIndex(x, y, z) != 0){
	//							return false;
	//						}
	//						int lIndex = (index * 6) * 3 + sideId * 3;
	//						out.x = light[lIndex + 0];
	//						out.y = light[lIndex + 1];
	//						out.z = light[lIndex + 2];
	//						return true;
	//					}
	//				}else{
	//					type = contain[index = getContainIndex(x+dir.x, y+dir.y, z+dir.z)];
	//					if(type != 0){
	//						ElementInformation info = ElementKeyMap.getInfo((short)Math.abs(type));
	//						int lIndex = (index * 6) * 3 + sideId * 3;
	//						out.x = light[lIndex + 0];
	//						out.y = light[lIndex + 1];
	//						out.z = light[lIndex + 2];
	//						return true;
	//					}
	//				}
	//			}else if((type = contain[index = getContainIndex(x+dir.x, y+dir.y, z+dir.z)]) != 0){
	//				out.x = 0.0f;
	//				out.y = 0.0f;
	//				out.z = 0.0f;
	//				return true;
	//
	//			}
	//		}
	//		short type = 0;
	//		if ((type = contain[index = getContainIndex(x, y, z)]) != 0) {
	//			if(!ownPos && blockStyle != 0 && blockStyle != 3 && ElementKeyMap.getInfo((short)Math.abs(type)).getBlockStyle() != blockStyle){
	//				return false;
	//			}
	//			int lIndex = (index * 6) * 3 + sideId * 3;
	//			out.x = light[lIndex + 0];
	//			out.y = light[lIndex + 1];
	//			out.z = light[lIndex + 2];
	//			return true;
	//		}
	//		return false;
	//	}
	public boolean getLightVec(boolean ownPos, boolean moved, Vector4i ownPosV, Vector4i pos, Vector4f out, int blockStyle, byte orientation, boolean active, AlgorithmParameters p) {

		int sideId = pos.w;
		int index = 0;

		int x = pos.x;
		int y = pos.y;
		int z = pos.z;
		{
			short type = 0;
			if (moved && (blockStyle == 0 || p.normalMode == 0)) {
				//			//FIX for missing shading on stairs
				out.x = 0.0f;
				out.y = 0.0f;
				out.z = 0.0f;
				out.w = 0.0f;
				return true;
			}
		}

		short type = 0;
		if ((type = contain[index = getContainIndex(x, y, z)]) != 0) {
			if (moved && !allInside[index] && p.normalMode != 0 && blockStyle != 0 && blockStyle != 3) {
				//FIXME: put the light of blocks on the edge 'applyFromAir' to the
				//other side the angle to use it instead of calling false
				if (
						(sideId == Element.RIGHT && x > 15) || (sideId == Element.LEFT && x < 0) ||
								(sideId == Element.TOP && y > 15) || (sideId == Element.BOTTOM && y < 0) ||
								(sideId == Element.FRONT && z > 15) || (sideId == Element.BACK && z < 0)) {
					return false;
				}
			}
			int lIndex = (index * 6) * 4 + sideId * 4;
			out.x = light[lIndex + 0];
			out.y = light[lIndex + 1];
			out.z = light[lIndex + 2];
			out.w = light[lIndex + 3];
			return true;
		}
		return false;
	}

	private void getNeighboring(SegmentData data, Vector3b helperPosGlobal,
	                            Segment segment, Segment rootSeg, Vector3i outSegPos,
	                            Vector3i callbackPos, SegmentRetrieveCallback callback) {
		if (((helperPosGlobal.x | helperPosGlobal.y | helperPosGlobal.z) & 0xf0) == 0) {

			callback.pos.set(data.getSegment().pos);
			if (segment == null) {
				callback.segment = null;
				callback.state = SegmentBufferOctree.EMPTY;
			} else {
				callback.segment = segment;
				callback.pos.set(segment.pos);
				callback.state = 1;
				assert (callback.segment != null);
			}
			return;
		}

		int x = helperPosGlobal.x >> 4;
		int y = helperPosGlobal.y >> 4;
		int z = helperPosGlobal.z >> 4;
		if (segment != null) {
			absSegPos.x = segment.absPos.x + x;
			absSegPos.y = segment.absPos.y + y;
			absSegPos.z = segment.absPos.z + z;

			outSegPos.x = segment.pos.x + x * 16;
			outSegPos.y = segment.pos.y + y * 16;
			outSegPos.z = segment.pos.z + z * 16;

			//			//REMOVE AFTER CONFIRMED BUG FREE
			//			int xDif = absSegPos.x - rootSeg.absPos.x + cacheFak;
			//			int yDif = absSegPos.y - rootSeg.absPos.y + cacheFak;
			//			int zDif = absSegPos.z - rootSeg.absPos.z + cacheFak;
			//
			//			int index = zDif * cacheRange_X_cacheRange + yDif * cacheRangeMul
			//					+ xDif;
			//
			//			assert (index < cacheByPos.length && index >= 0) : index + "/"
			//					+ cacheByPos.length + ": " + xDif + ", " + yDif + ", "
			//					+ zDif + ": " + absSegPos + " -> " + rootSeg.absPos + "; "
			//					+ cacheFak + ";  callback = " + callbackPos + "; "
			//					+ segment + " Ray: " + helperPosGlobal;

		} else {
			absSegPos.x = ByteUtil.divUSeg(callbackPos.x) + x;
			absSegPos.y = ByteUtil.divUSeg(callbackPos.y) + y;
			absSegPos.z = ByteUtil.divUSeg(callbackPos.z) + z;

			outSegPos.x = callbackPos.x + x * 16;
			outSegPos.y = callbackPos.y + y * 16;
			outSegPos.z = callbackPos.z + z * 16;

			//			//REMOVE AFTER CONFIRMED BUG FREE
			//			int xDif = absSegPos.x - rootSeg.absPos.x + cacheFak;
			//			int yDif = absSegPos.y - rootSeg.absPos.y + cacheFak;
			//			int zDif = absSegPos.z - rootSeg.absPos.z + cacheFak;
			//
			//			int index = zDif * cacheRange_X_cacheRange + yDif * cacheRangeMul
			//					+ xDif;
			//
			//			assert (index < cacheByPos.length && index >= 0) : index + "/"
			//					+ cacheByPos.length + ": " + xDif + ", " + yDif + ", "
			//					+ zDif + ": " + absSegPos + " -> " + rootSeg.absPos + "; "
			//					+ cacheFak + ";  callback = " + callbackPos;

		}
		helperPosGlobal.x = (byte) (helperPosGlobal.x & 0xF);
		helperPosGlobal.y = (byte) (helperPosGlobal.y & 0xF);
		helperPosGlobal.z = (byte) (helperPosGlobal.z & 0xF);

		loadFromChache(outSegPos, absSegPos, rootSeg, callback);

	}

	public final SegmentController getSegmentController() {
		return data.getSegmentController();
	}

	public final byte getVisibilityMask(int containIndex, Vector3b pos,
	                                    ElementInformation info) {

		// containing in data

		// bit mask for all sides visible
		byte mask = 63;
		// for all six sides of the cube
		boolean selfBlend = contain[containIndex] < 0;
		if (info.blockStyle.solidBlockStyle) {
			int infoIndex = SegmentData.getInfoIndex(pos);
			BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(
					info.getBlockStyle(), data.getOrientation(infoIndex));
			int[] sides = algo.getSidesToCheckForVis();

			for (int i = 0; i < sides.length; i++) {
				if (hasNeighbor(sides[i], containIndex, selfBlend)) {
					// this side is not visible. subtract from mask
					mask -= Element.SIDE_FLAG[sides[i]];
				}
			}
			// mask = 63;
		} else if (info.getBlockStyle() == BlockStyle.SPRITE) {
			// mask -= (Element.FLAG_BOTTOM + Element.FLAG_TOP);
			byte orientation = data.getOrientation(SegmentData
					.getInfoIndex(pos));
			if (orientation == Element.TOP || orientation == Element.BOTTOM) {
				mask = 51;
			} else if (orientation == Element.FRONT
					|| orientation == Element.BACK) {
				mask = 63 - (Element.FLAG_FRONT + Element.FLAG_BACK);
			} else {
				mask = 63 - (Element.FLAG_LEFT + Element.FLAG_RIGHT);
			}
		} else {
			for (int i = 0; i < 6; i++) {
				if (hasNeighbor(i, containIndex, selfBlend)) {
					// this side is not visible. subtract from mask
					mask -= Element.SIDE_FLAG[i];
				}
			}
		}

		return mask;
	}

	public final byte getVisibilityMask(Vector3b pos, ElementInformation info) {
		return getVisibilityMask(getContainIndex(pos.x, pos.y, pos.z), pos,
				info);
	}

	private boolean hasNeighbor(int sideId, final int containIndex,
	                            boolean isBlendedBlock) {
		// Vector3i d = Element.DIRECTIONSi[sideId];
		// int x = pos.x + d.x;
		// int y = pos.y + d.y;
		// int z = pos.z + d.z;
		// System.err.println("SID: "+relativeIndexBySide[sideId]+" : "+Element.getSideString(sideId));
		short neighborType = contain[containIndex + relativeIndexBySide[sideId]]; // getContainIndex(x,
		// y,
		// z)
		if (neighborType != 0) {
			ElementInformation info = ElementKeyMap.getInfo(FastMath
					.abs(neighborType));
			if (info == null) {
				// fixme remove when ok
				throw new NullPointerException("ERROR: info null: " + info
						+ "; type: " + FastMath.abs(neighborType));
			}
			if (isVisMaskException(info, neighborType, sideId, containIndex)) {
				return false;
			}
			/*
			 * if the block we are searching neighbors for is blended itself,
			 * don't care, if neighbors are blended itself, otherwise don't hide
			 * side of a solid object, if it's neighbor is blended
			 */
			if (isBlendedBlock) {
				return neighborType < 0; // only accept as neighbor is neighbor
				// is also blended
			}
			if (neighborType < 0) {
				return false; // only accept as neighbor is neighbor is also
				// blended
			}

			return true;

			// assert(b
			// ||(!(data.containsUnblended(x,y,z)))):x+", "+y+", "+z+" conO: "+b+": but data: "+data.contains(x,y,z)+"; "+data+"; "+data.getType(x,y,z);
			// // doesNotContain -> dataAlsoDoesNotContain

		}
		return false;
	}

	public boolean isTestObject() {
		return data.getSegment().pos.equals(testObj);
	}

	private final boolean isVisMaskException(ElementInformation info,
	                                         short containIndexType, int sideId, int containIndex) {
		if (ElementKeyMap.isInvisible(info.getId())) {
			return true;
		}
		if (info.blockStyle.solidBlockStyle) {
			// int infoIndex = Math.abs(containIndexType);

			short actAndOrientation = containAct[containIndex
					+ relativeIndexBySide[sideId]];
			boolean active = actAndOrientation >= 64;
			actAndOrientation -= active ? 64 : 0;

			assert (BlockShapeAlgorithm.isValid(info.getBlockStyle(),
					(byte) actAndOrientation)) : info.getName()
					+ " on " + actAndOrientation + " with act: " + active
					+ " -> " + (actAndOrientation + (active ? 0 : 16));
			BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(
					info.getBlockStyle(), (byte) actAndOrientation);
			// if(info.getName().equals("Purple Hull Wedge")){
			// int algoIndex = (actAndOrientation + (active ? 0 :
			// 16))%(BlockShapeAlgorithm.algorithms[info.getBlockStyle()
			// -1].length-1) ;
			// System.err.println("ALGO "+algo+" --> "+algoIndex);
			// }
			int[] sides = algo.getSidesToCheckForVis();
			assert (sides != null) : algo;
			int opposite = Element.getOpposite(sideId);
			for (int i = 0; i < sides.length; i++) {
				if (opposite == sides[i]) {
					return false;
				}
			}
			return true;
		} else {
			return info.getBlockStyle() == BlockStyle.SPRITE;
//					|| (ElementInformation.isVisException(info, containIndexType));
		}
	}

	private void loadFromChache(Vector3i segPos, Vector3i absSegPos,
	                            Segment rootSeg, SegmentRetrieveCallback callback) {
		int xDif = absSegPos.x - rootSeg.absPos.x + cacheFak;
		int yDif = absSegPos.y - rootSeg.absPos.y + cacheFak;
		int zDif = absSegPos.z - rootSeg.absPos.z + cacheFak;

		int index = zDif * cacheRange_X_cacheRange + yDif * cacheRangeMul
				+ xDif;

		assert (index < cacheByPos.length && index >= 0) : index + "/"
				+ cacheByPos.length + ": " + xDif + ", " + yDif + ", " + zDif
				+ ": " + absSegPos + " -> " + rootSeg.absPos + "; " + cacheFak;
		if (cacheByPos[index].state == SegmentBufferOctree.NOTHING) {
			if (cacheMissesByPos[index] > 2) {
				//segemnt could not be loaded. declare empty for now and report fail back

				if (!failed && data.getSegmentController().isInboundAbs(segPos)) {
					failed = true;
					if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {

						DebugBox b = new DebugBox(
								new Vector3f(
										segPos.x - 8 - 0.1f,
										segPos.y - 8 - 0.1f,
										segPos.z - 8),
								new Vector3f(
										segPos.x + 8 - 0.1f,
										segPos.y + 8 + 0.1f,
										segPos.z + 8 + 0.1f),
								getSegmentController()
										.getWorldTransformOnClient(), 0, 1, 0, 1);
						DebugDrawer.boxes.add(b);
					}
				}
				cacheByPos[index].pos.set(segPos);
				cacheByPos[index].state = SegmentBufferOctree.NOTHING;
				cacheByPos[index].segment = null;

				//we never filled callback with getSegmentBuffer().get()
				//so we set it manually
				callback.pos.set(segPos);
				callback.state = SegmentBufferOctree.NOTHING;
				callback.segment = null;
			} else {
				data.getSegmentController().getSegmentBuffer()
						.get(segPos, callback);

				assert (callback.segment == null || callback.segment.pos
						.equals(callback.pos)) : callback.segment.pos + "; "
						+ callback.pos;

				cacheByPos[index].pos.set(callback.pos);

				if (callback.state == 1) {
					cacheByPos[index].state = callback.state;
					cacheByPos[index].segment = callback.segment;
					assert (callback.state == 1);
				} else if (callback.state == SegmentBufferOctree.EMPTY) {

					// EMPTY
					cacheByPos[index].state = SegmentBufferOctree.EMPTY;
					cacheByPos[index].segment = null;
				} else {
					cacheMissesByPos[index]++;
					// nothing
					cacheByPos[index].state = SegmentBufferOctree.NOTHING;
					cacheByPos[index].segment = null;
				}
				assert (callback.segment == null || callback.segment.pos
						.equals(callback.pos)) : callback.segment.pos + "; "
						+ callback.pos;
			}
		} else {
			callback.pos.set(cacheByPos[index].pos);

			if (cacheByPos[index].state == 1) {
				callback.state = 1;
				callback.segment = cacheByPos[index].segment;
				assert (callback.segment != null);
			} else if (cacheByPos[index].state == SegmentBufferOctree.EMPTY) {
				callback.state = SegmentBufferOctree.EMPTY;
				callback.segment = null;
			} else {
				assert (false);
			}

			assert (callback.segment == null || callback.segment.pos
					.equals(callback.pos)) : callback.segment.pos + "; "
					+ callback.pos;
		}
	}

	private boolean neighbors(Vector3b relPos, Segment rootSegment,
	                          SegmentRetrieveCallback startCallback) {

		byte inX = relPos.x;
		byte inY = relPos.y;
		byte inZ = relPos.z;

		if (SegmentData.allNeighborsInside(inX, inY, inZ)) {
			if (callback.state < 0) {
				return false;
			} else {
				for (int i = 0; i < 6; i++) {
					byte x = (byte) ByteUtil.modUSeg(inX
							+ Element.DIRECTIONSb[i].x);
					byte y = (byte) ByteUtil.modUSeg(inY
							+ Element.DIRECTIONSb[i].y);
					byte z = (byte) ByteUtil.modUSeg(inZ
							+ Element.DIRECTIONSb[i].z);
					if (callback.segment.getSegmentData().containsUnsave(x, y,
							z)) {
						return true;
					}
				}
			}
		} else {
			for (int i = 0; i < 6; i++) {

				int x = startCallback.pos.x
						+ ByteUtil.divUSeg(relPos.x + Element.DIRECTIONSb[i].x)
						* 16;
				int y = startCallback.pos.y
						+ ByteUtil.divUSeg(relPos.y + Element.DIRECTIONSb[i].y)
						* 16;
				int z = startCallback.pos.z
						+ ByteUtil.divUSeg(relPos.z + Element.DIRECTIONSb[i].z)
						* 16;

				if (startCallback.pos.equals(x, y, z)) {
					if (startCallback.state == 1) {

						byte xC = (byte) ByteUtil.modUSeg(relPos.x
								+ Element.DIRECTIONSb[i].x);
						byte yC = (byte) ByteUtil.modUSeg(relPos.y
								+ Element.DIRECTIONSb[i].y);
						byte zC = (byte) ByteUtil.modUSeg(relPos.z
								+ Element.DIRECTIONSb[i].z);

						if (startCallback.segment.getSegmentData()
								.containsUnsave(xC, yC, zC)) {
							return true;
						}
					} else {
						// Target Segment is empty
					}
				} else {

					int xDif = ByteUtil.divUSeg(x - rootSegment.pos.x)
							+ cacheFak;
					int yDif = ByteUtil.divUSeg(y - rootSegment.pos.y)
							+ cacheFak;
					int zDif = ByteUtil.divUSeg(z - rootSegment.pos.z)
							+ cacheFak;

					int index = zDif * cacheRange_X_cacheRange + yDif
							* cacheRangeMul + xDif;

					// data.getSegmentController().getSegmentBuffer().get(x,y,z,
					// nCallback);
					if (index < 0 || index >= cacheByPos.length) {
						failed = true;
						throw new ArrayIndexOutOfBoundsException(
								"Error on index " + index + "; Diff: " + xDif
										+ ", " + yDif + ", " + zDif
										+ "; segmentPos: " + rootSegment.pos
										+ "; " + x + ", " + y + ", " + z);
					}
					if (cacheByPos[index].state == SegmentBufferOctree.NOTHING) {

						if (cacheMissesByPos[index] > 2) {
							//too many cache misses. report failure and declare segment empty for now
							if (!failed && data.getSegmentController().isInboundAbs(x, y, z)) {
								failed = true;
								if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {

									DebugBox b = new DebugBox(
											new Vector3f(
													x - 8 - 0.1f,
													y - 8 - 0.1f,
													z - 8),
											new Vector3f(
													x + 8 - 0.1f,
													y + 8 + 0.1f,
													z + 8 + 0.1f),
											getSegmentController()
													.getWorldTransformOnClient(), 0, 0, 1, 1);
									DebugDrawer.boxes.add(b);
								}
							}
							cacheByPos[index].pos.set(x, y, z);
							cacheByPos[index].state = SegmentBufferOctree.NOTHING;
							cacheByPos[index].segment = null;

							//we never filled callback with getSegmentBuffer().get()
							//so we set it manually
							nCallback.pos.set(x, y, z);
							nCallback.state = SegmentBufferOctree.NOTHING;
							nCallback.segment = null;

						} else {

							data.getSegmentController().getSegmentBuffer()
									.get(x, y, z, nCallback);
							cacheByPos[index].pos.set(nCallback.pos);
							if (nCallback.state == 1) {
								// FILLED
								cacheByPos[index].state = nCallback.state;
								cacheByPos[index].segment = nCallback.segment;
							} else if (nCallback.state == SegmentBufferOctree.EMPTY) {

								// EMPTY
								cacheByPos[index].state = SegmentBufferOctree.EMPTY;
								cacheByPos[index].segment = null;
							} else {
								cacheMissesByPos[index]++;
								// nothing
								cacheByPos[index].state = SegmentBufferOctree.NOTHING;
								cacheByPos[index].segment = null;
							}

							//						if (nCallbackTest.state != nCallback.state
							//								|| !nCallbackTest.pos.equals(nCallback.pos)) {
							//							setFailed(true);
							//							return false;
							//						}
							// if(!nCallbackTest.pos.equals(nCallback.pos) ||
							// nCallbackTest.state != nCallback.state)
							// assert (nCallbackTest.pos.equals(nCallback.pos));
							// assert (nCallbackTest.state == nCallback.state);
							// assert (nCallbackTest.segment == nCallback.segment);
						}
					} else {
						nCallback.pos.set(cacheByPos[index].pos);
						if (cacheByPos[index].state == 1) {
							nCallback.state = 1;
							nCallback.segment = cacheByPos[index].segment;
							assert (nCallback.segment != null);
						} else if (cacheByPos[index].state == SegmentBufferOctree.EMPTY) {
							nCallback.state = SegmentBufferOctree.EMPTY;
							nCallback.segment = null;
						} else {
							// assert (false);
						}

						//						data.getSegmentController().getSegmentBuffer().get(x, y, z, nCallbackTest);

						//						if (nCallbackTest.state != nCallback.state
						//								|| !nCallbackTest.pos.equals(nCallback.pos)) {
						//							setFailed(true);
						//							return false;
						//						}
						// assert (nCallbackTest.pos.equals(nCallback.pos)) :
						// nCallbackTest.pos
						// + " -> should be "
						// + nCallback.pos
						// + "; "
						// + xDif
						// + ", "
						// + yDif
						// + ", "
						// + zDif
						// + "; "
						// + nCallback.state
						// + ", "
						// + nCallbackTest.state;
						// assert (nCallbackTest.state == nCallback.state);
						// assert (nCallbackTest.segment == nCallback.segment);
					}

					if (nCallback.state == 1) {
						byte xC = (byte) ByteUtil.modUSeg(relPos.x
								+ Element.DIRECTIONSb[i].x);
						byte yC = (byte) ByteUtil.modUSeg(relPos.y
								+ Element.DIRECTIONSb[i].y);
						byte zC = (byte) ByteUtil.modUSeg(relPos.z
								+ Element.DIRECTIONSb[i].z);
						// assert (nCallback != null);
						// assert (nCallback.segment != null);
						// assert (nCallback.segment.getSegmentData() != null);

						if (nCallback.segment.getSegmentData().containsUnsave(
								xC, yC, zC)) {
							return true;
						}
					} else {
						// Target Segment is empty
					}
				}
			}
		}

		return false;
		// return data.getSegmentController().hasNeighborElements(callback2,
		// relPos.x, relPos.y, relPos.z, outSegPos);
		// return data.getSegmentController().hasNeighborElements(callback2,
		// relPos.x, relPos.y, relPos.z, outSegPos);
	}

	private void normalize(Vector3i pos, SegmentData data, int infoIndex) {
		if (contain[getContainIndex(pos.x, pos.y, pos.z)] != 0) {
			// for(int sideId = 0; sideId < 6; sideId++){
			// if((container.getVis((byte)pos.x, (byte)pos.y, (byte)pos.z ) &
			// Element.SIDE_FLAG[sideId]) == Element.SIDE_FLAG[sideId] ){
			//
			//
			// // normalizer.normalize(pos, data, sideId, this);
			// // testNormalizeFill(pos, data, sideId);
			// }
			// }

			byte vis = container.getVis(pos.x, pos.y, pos.z);
			if (vis > 0) {
				normalizer.type = data.getType(infoIndex);
				if (normalizer.type == 0) {
					return;
				}
				ElementInformation info = ElementKeyMap
						.getInfo(normalizer.type);

				normalizer.blockStyle = info.getBlockStyle();
				if (info.blockStyle.solidBlockStyle) {
					normalizer.active = data.isActive(infoIndex);
					normalizer.orientation = data.getOrientation(infoIndex);
				} else {
					normalizer.blockStyle = BlockStyle.NORMAL;
				}
				assert (false);
				//				if (vis >= Element.FLAG_LEFT) {
				//					normalizer.normalizeLeft(pos, data, Element.LEFT, getContainer());
				//					vis -= Element.FLAG_LEFT;
				//				}
				//				if (vis >= Element.FLAG_RIGHT) {
				//					normalizer.normalizeRight(pos, data, Element.RIGHT, getContainer());
				//					vis -= Element.FLAG_RIGHT;
				//				}
				//
				//				if (vis >= Element.FLAG_BOTTOM) {
				//					normalizer.normalizeBottom(pos, data, Element.BOTTOM, getContainer());
				//					vis -= Element.FLAG_BOTTOM;
				//				}
				//				if (vis >= Element.FLAG_TOP) {
				////					if(normalizer.blockStyle == 5){
				////						System.err.println("NORMALIZING GENERAL TOP OF PENTA");
				////					}
				//					normalizer.normalizeTop(pos, data, Element.TOP, getContainer());
				//					vis -= Element.FLAG_TOP;
				//				}else if(normalizer.blockStyle == 5){
				////					System.err.println("NORMALIZING TOP FOR PENTA");
				//					normalizer.normalizeTop(pos, data, Element.TOP, getContainer());
				//				}
				//
				//				if (vis >= Element.FLAG_BACK) {
				//					normalizer.normalizeBack(pos, data, Element.BACK, getContainer());
				//					vis -= Element.FLAG_BACK;
				//				}
				//				if (vis >= Element.FLAG_FRONT) {
				//					normalizer.normalizeFront(pos, data, Element.FRONT, getContainer());
				//					vis -= Element.FLAG_FRONT;
				//				}

				if (info.isLightSource() && data.isActive(infoIndex)) {
					for (int sideId = 0; sideId < 6; sideId++) {
						int subSubIndex = sideId * 4;
						int lightInfoIndex = CubeMeshBufferContainer
								.getLightInfoIndex(pos.x, pos.y, pos.z);
						for (int i = 3; i >= 0; i--) {
							byte a = container.getFinalLight(
									lightInfoIndex, subSubIndex + (i), 0);
							container.setFinalLight(
									lightInfoIndex,
									(byte) Math.min(
											OcclusionOld.COLOR_LIGHT_PERM_BYTE,
											a + 5), subSubIndex + (i), 0);

							a = container.getFinalLight(lightInfoIndex,
									subSubIndex + (i), 1);
							container.setFinalLight(
									lightInfoIndex,
									(byte) Math.min(
											OcclusionOld.COLOR_LIGHT_PERM_BYTE,
											a + 5), subSubIndex + (i), 1);

							a = container.getFinalLight(lightInfoIndex,
									subSubIndex + (i), 2);
							container.setFinalLight(
									lightInfoIndex,
									(byte) Math.min(
											OcclusionOld.COLOR_LIGHT_PERM_BYTE,
											a + 5), subSubIndex + (i), 2);

							a = container.getFinalLight(lightInfoIndex,
									subSubIndex + (i), 3);
							container.setFinalLight(
									lightInfoIndex,
									a, subSubIndex + (i), 3);
						}
					}
				}
			}

		}
	}

	private void occlude() {

		assert (container.beacons.isEmpty());

		int i = 0;
		for (byte z = -1; z < 17; z++) {
			for (byte y = -1; y < 17; y++) {
				for (byte x = -1; x < 17; x++) {
					occludeBlock(x, y, z, i);
					i++;
				}
			}
		}
		//		Arrays.fill(occlusion, 1.0f);
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {
			if (failed) {

				DebugBox b = new DebugBox(
						new Vector3f(data.getSegment().pos.x - 8 - 0.1f,
								data.getSegment().pos.y - 8 - 0.1f, data.getSegment().pos.z - 8),
						new Vector3f(data.getSegment().pos.x + 8 - 0.1f,
								data.getSegment().pos.y + 8 + 0.1f, data.getSegment().pos.z + 8 + 0.1f),
						getSegmentController().getWorldTransformOnClient(),
						1, 0, 0, 1);
				DebugDrawer.boxes.add(b);
			} else {
				DebugBox b = new DebugBox(
						new Vector3f(data.getSegment().pos.x - 8 - 0.1f,
								data.getSegment().pos.y - 8 - 0.1f, data.getSegment().pos.z - 8 - 0.1f),
						new Vector3f(data.getSegment().pos.x + 8 + 0.1f,
								data.getSegment().pos.y + 8 + 0.1f, data.getSegment().pos.z + 8 + 0.1f),
						getSegmentController().getWorldTransformOnClient(),
						1, 1, 1, 1);
				DebugDrawer.boxes.add(b);
			}
		}
		applyAndNormalize();
		//		System.err.println("-------------------------------------- "+data+": Cache: "+cacheHits+"; "+cacheMisses+" --- "+cacheRepeatMisses);
		// System.err.println("Occlusion DONE: "+data.getSegment().pos+" "+data.getSegmentController()+" Arrays: "+timeArrays+"; ResetOcclusion: "+timeResetOcclusion+"; Occ "+timeOcc+"; Norm: "+timeNorm);
	}

	// private void setArray(int ogIndex, float[] in, int index, float val){
	// in[ogIndex+index] = val;
	// }

	private void occludeBlock(byte x, byte y, byte z, int pIndex) {

		helperPosGlobal.set(x, y, z);

		if (allInside[pIndex]) {
			callback.state = 1;
			callback.segment = data.getSegment();
			callback.pos.set(data.getSegment().pos);
			assert (callback.segment == null || callback.segment.pos
					.equals(callback.pos)) : callback.segment.pos + "; "
					+ callback.pos;
		} else {
			Segment segment = data.getSegment();
			if (segment == null) {
				System.err
						.println("[OCCLISION] ERROR: Occlusion failed: root seg null: "
								+ segment);
				throw new NullPointerException("root seg null");
				//				setFailed(true);
				//				return;
			}
			// callback.pos is not used here because sata.getSegment() is never
			// null
			getNeighboring(data, helperPosGlobal, segment, segment,
					outSegPos, callback.pos, callback);

			assert (callback.segment == null || callback.segment.pos
					.equals(callback.pos)) : callback.segment.pos + "; "
					+ callback.pos;
		}
		assert (callback.segment == null || callback.segment.pos
				.equals(callback.pos)) : callback.segment.pos + "; "
				+ callback.pos;
		int containIndex = minusOneToSeventeenIndices[pIndex];// getContainIndex(x,
		// y, z);
		contain[containIndex] = (short) 0;
		if (callback.state == SegmentBufferOctree.NOTHING) {

			if (data.getSegmentController().isInboundAbs(callback.pos)) {
				// if(!this.failed){
				// if(Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)){
				// System.err.println("LIGHTING FAILED FOR "+data.getSegment()+" on "+outSegPos+"; ");
				// }
				// // DebugBox b = new DebugBox(
				// // new Vector3f(outSegPos.x, outSegPos.y, outSegPos.z),
				// // new Vector3f(outSegPos.x+16, outSegPos.y+16,
				// outSegPos.z+16),
				// // getSegmentController().getWorldTransform(),
				// // 1, 1, 0, 1);
				// // DebugDrawer.boxes.add(b);
				// }
				this.failed = true;
				return;
			} else {
				dummy.setPos(outSegPos.x, outSegPos.y, outSegPos.z);
				callback.state = SegmentBufferOctree.EMPTY;
			}

		}
		assert (callback.segment == null || callback.segment.pos
				.equals(callback.pos)) : callback.segment.pos + "; "
				+ callback.pos;
		final Segment outGlobalSegment = callback.segment;

		final boolean empty = callback.state == SegmentBufferOctree.EMPTY;
		assert (empty || outGlobalSegment != null);

		int gloIndex;

		if (!empty
				&& outGlobalSegment.getSegmentData().containsFast(
				helperPosGlobal)) { // possible NUllPointer

			gloIndex = SegmentData.getInfoIndex(helperPosGlobal);
			short type = outGlobalSegment.getSegmentData().getType(gloIndex);

			if (ElementKeyMap.isBeacon(type)
					&& SegmentData.valid(x, y, z)) {
				// only add beacon if block is in normal range
				container.beacons.add((short) gloIndex);
			}
			boolean act = outGlobalSegment.getSegmentData().isActive(gloIndex);
			ElementInformation info = ElementKeyMap.getInfo(type);
			byte orientation = outGlobalSegment.getSegmentData()
					.getOrientation(gloIndex);

			assert (info.getBlockStyle() == BlockStyle.NORMAL || BlockShapeAlgorithm.isValid(
					info.getBlockStyle(), orientation)) : info
					.getName() + "; orient: " + orientation + "; " + act;

			if (info.isBlended() || info.isBlendBlockStyle()
					|| (ElementInformation.isBlendedSpecial(type, act))) {

				contain[containIndex] = (short) -type;
			} else {
				contain[containIndex] = type;
			}

			containAct[containIndex] = (short) (act ? (64 + orientation)
					: orientation);

			// short actAndOrientation = containAct[containIndex];
			// boolean active = actAndOrientation >= 64;
			// actAndOrientation -= active ? 64 : 0;
			// assert(info.getBlockStyle() == BlockStyle.NORMAL ||
			// BlockShapeAlgorithm.isValid(info.getBlockStyle(),
			// (byte)actAndOrientation,
			// active)):info.getName()+" on "+actAndOrientation+" with act: "+active+" -> "+(actAndOrientation
			// + (active ? 0 : 16));
		} else {
			gloIndex = SegmentData.getInfoIndex(helperPosGlobal);
		}

		byte xoff, yoff, zoff;
		boolean collided;

		short type = empty ? 0 : outGlobalSegment.getSegmentData().getType(
				gloIndex);

		assert (callback.segment == null || callback.segment.pos
				.equals(callback.pos)) : callback.segment.pos + "; "
				+ callback.pos;

		if ((empty || (type == Element.TYPE_NONE
				|| ElementKeyMap.getInfo(type).isBlended() || !ElementKeyMap
				.getInfo(type).isPhysical(
						outGlobalSegment.getSegmentData().isActive(gloIndex))))
				&& neighbors(helperPosGlobal, data.getSegment(), callback)) {

			airBlocksWithNeighbors[airBlocksWithNeighborsPointer + 0] = x;
			airBlocksWithNeighbors[airBlocksWithNeighborsPointer + 1] = y;
			airBlocksWithNeighbors[airBlocksWithNeighborsPointer + 2] = z;
			airBlocksWithNeighborsPointer += 3;

			// int lightIndex = SegmentData.getLightInfoIndex(helperPosGlobal);

			int ogIndex = minusOneToSeventeenOGIndex[pIndex];// getOGIndex(x,y,z);

			// occHelperFloat = getOcclusion(ogIndex, occHelperFloat);
			// gatherHelperFloat = getGather(ogIndex, gatherHelperFloat);

			// (5, 18, 39)
			assert (callback.segment == null || callback.segment.pos
					.equals(callback.pos)) : callback.segment.pos + "; "
					+ callback.pos;
			callBackPos.set(callback.pos);

			assert (dbPos != null);
			//			boolean draw = outGlobalSegment != null && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && getSegmentController().toString().contains("schema") &&
			//					outGlobalSegment.pos.x + x == dbPos.x && outGlobalSegment.pos.y + y == dbPos.y && outGlobalSegment.pos.z + z == dbPos.z;
			//			if(getSegmentController().toString().contains("schema") && draw){
			//				System.err.println((outGlobalSegment.pos.x + x)+"; "+(outGlobalSegment.pos.y + y)+", "+(outGlobalSegment.pos.z + z)+": "+dbPos);
			//			}

			for (int r = 0; r < sample.rays.length; r++) {
				Ray ray = sample.rays[r];
				collided = false;
				// float rayDepth = 1;

				for (int i = 0, d = 0; i < ray.points.length; i += 3, d++) {
					byte rToX = ray.points[i + 0];
					byte rToY = ray.points[i + 1];
					byte rToZ = ray.points[i + 2];
					xoff = (byte) (helperPosGlobal.x + rToX);
					yoff = (byte) (helperPosGlobal.y + rToY);
					zoff = (byte) (helperPosGlobal.z + rToZ);

					helperPos.set(xoff, yoff, zoff);

					assert (data.getSegment() != null);
					assert (callback.segment == null || callback.segment.pos
							.equals(callback.pos)) : callback.segment.pos
							+ "; " + callback.pos;
					// callback pos is needed to determine outcome for empty
					// (null) segments
					getNeighboring(data, helperPos, outGlobalSegment,
							data.getSegment(), outSegPos, callBackPos,
							callbackRay);

					if (callbackRay.state == SegmentBufferOctree.NOTHING) {
						if (!failed && data.getSegmentController().isInboundAbs(outSegPos)) {
							// mark as failed if an inbound segment is not
							// available yet
							this.failed = true;
						}
						break;
					} else {

						if (callbackRay.state == 1) {

							assert (callbackRay.segment != null);
							Segment outSegment = callbackRay.segment;
							int infoIndex = SegmentData.getInfoIndex(
									helperPos.x, helperPos.y, helperPos.z);

							short obstacleType = // Possible NullPointer
									outSegment.getSegmentData().getType(infoIndex);

							if (obstacleType != Element.TYPE_NONE) {

								ElementInformation info = ElementKeyMap
										.getInfo(obstacleType);

								boolean lightPassable = info.isBlended()
										|| !info.isPhysical(outSegment
										.getSegmentData().isActive(
												infoIndex))
										|| (info.getBlockStyle() == BlockStyle.SPRITE && info
										.isLightSource());

								if (info.isLightSource()
										&& outSegment.getSegmentData()
										.isActive(infoIndex)) {
									Vector4f ls = info.getLightSourceColor();
									// float depth = ray.depths[d];
									float f = ray.depths[d] * 2.5f * ls.w;// (1f /
									// (
									// rayDepth
									// ));
									addGather(ogIndex, ls.x * f);
									addGather(ogIndex + 1, ls.y * f);
									addGather(ogIndex + 2, ls.z * f);
								}

								if (!lightPassable) {

									collided = true;
									//									if(draw){
									//										outSegment.debugDrawPoint(helperPos.x, helperPos.y, helperPos.z, 0, 1.0f, 0.0f, 0.0f, 1, 50000);
									//									}

									break;
								}

							}

							//							if(draw){
							//								if(!collided){
							//									outSegment.debugDrawPoint(helperPos.x, helperPos.y, helperPos.z, 0, (float)(i*3) / (float)ray.points.length, 0.3f, 0.6f, 1, 50000);
							//								}
							//							}
						} else {
							// traverse the whole segment at once
						}
					}
					// rayDepth*= (1.2f);

				}
				if (!collided) {
					for (int i = 0; i < 6; i++) {
						// add rays
						addOcclusion(ogIndex + i, ray.data[i]);
					}
				}

			}

			//normalize occlusion
			for (int i = 0; i < 6; i++) {
				int index = ogIndex + i;
				float bef = occlusion[index];
				occlusion[index] = (bef * sample.dataInv[i]);
				//				assert(occlusion[index] >= 0 && occlusion[index] <= 1);
			}

		}
	}

	public void reset(SegmentData segmentData,
	                  CubeMeshBufferContainer containerFromPool) {

		// private ShortBuffer contain =
		// BufferUtils.createShortBuffer(18*18*18);

		// private FloatBuffer occlusion =
		// MemoryUtil.memAllocFloat(18*18*18*6);
		// private FloatBuffer gather =
		// MemoryUtil.memAllocFloat(18*18*18*6);
		// private FloatBuffer ambience =
		// MemoryUtil.memAllocFloat(18*18*18*6);

		// private FloatBuffer light =
		// MemoryUtil.memAllocFloat(18*18*18*6*3);
		byte b = 0;
		// for(int i = 0; i < (18*18*18); i++){
		// contain.put(i,n);
		// }
		Arrays.fill(occlusion, 0);
		Arrays.fill(gather, 0);
		Arrays.fill(ambience, (byte) 0);
		Arrays.fill(airBlocksWithNeighbors, (byte) 0);
		Arrays.fill(affectedBlocksFromAirBlocks, (byte) 0);
		Arrays.fill(light, 0);
		Arrays.fill(cacheMissesByPos, 0);

		airBlocksWithNeighborsPointer = 0;
		// for(int i = 0; i < (18*18*18*6); i++){
		// occlusion.put(i,0);
		// gather.put(i,0);
		// ambience.put(i,b);
		// }
		// for(int i = 0; i < (18*18*18*6*3); i++){
		// light.put(i,0);
		// }
		for (int i = 0; i < cacheByPos.length; i++) {
			cacheByPos[i].segment = null;
			cacheByPos[i].state = SegmentBufferOctree.NOTHING;
		}
		// Arrays.fill(occlusion, 0);
		// Arrays.fill(gather, 0);
		// Arrays.fill(light, 0);
		// Arrays.fill(ambience, (byte)0);
		containerFromPool.reset();

	}

	// private float[] getGather(int ogIndex, float[] out){
	// return get(ogIndex, gather, out);
	// }
	private void setLight(int ogIndex, float R, float G, float B,
	                      float occlusion, int sideId) {
		float occ = occlusion;
		float xL = R;
		float yL = G;
		float zL = B;
		int index = ogIndex * 6 * 4 + sideId * 4;
		light[index + 0] = xL;
		light[index + 1] = yL;
		light[index + 2] = zL;
		light[index + 3] = occ;
	}

	private void setLightFromAirBlock(Vector3b origPos, int containIndexAir,
	                                  Vector3b dir, int sideId, int oppositeDir, boolean needsCheckValid) {

		/*
		 * compute a block position next to the air block
		 */
		int adjX = (origPos.x + dir.x);
		int adjY = (origPos.y + dir.y);
		int adjZ = (origPos.z + dir.z);

		if (!needsCheckValid || ogValid(adjX, adjY, adjZ)) {
			// no reclaculation of index needed
			int adjContainIndex = containIndexAir + relativeIndexBySide[sideId]; // getContainIndex(adjX,
			// adjY,
			// adjZ)
			if (contain[adjContainIndex] != 0) {

				//				int ogIndexSolid = adjContainIndex * 6; // getOGIndex(adjX,
				// adjY, adjZ);
				int ogAirIndex = containIndexAir * 6;
				float occ = 0;
				float gatR = getArray(ogAirIndex, gather, 0);
				float gatG = getArray(ogAirIndex, gather, 1);
				float gatB = getArray(ogAirIndex, gather, 2);

				occ = getArray(ogAirIndex, occlusion, sideId);

				//				if(getSegmentController().toString().contains("schema")){
				//					System.err.println("SET: "+occ);
				//					sad
				//				}

				setLight(adjContainIndex, gatR, gatG, gatB, occ, oppositeDir);
			}
		}
	}

	/**
	 * @return the failed
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * @param failed the failed to set
	 */
	public void setFailed(boolean failed) {
		// if(failed){
		// if(Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)){
		// try{
		// throw new
		// NullPointerException("ADDING: "+this+", "+this.getSegmentController());
		// }catch(Exception xx){
		// xx.printStackTrace();
		// }
		// }
		// }
		this.failed = failed;
	}

	// void add_ray(Cell cell, Ray ray){
	// cell.sides[SimplePosElement.LEFT] += ray.left;
	// cell.sides[SimplePosElement.RIGHT] += ray.right;
	// cell.sides[SimplePosElement.TOP] += ray.top;
	// cell.sides[SimplePosElement.BOTTOM] += ray.bottom;
	// cell.sides[SimplePosElement.FRONT] += ray.front;
	// cell.sides[SimplePosElement.BACK] += ray.back;
	// }
	//
	// void normalize(Cell cell, Sample sample){
	// cell.sides[SimplePosElement.LEFT] = 1 - cell.sides[SimplePosElement.LEFT]
	// / sample.left;
	// cell.sides[SimplePosElement.RIGHT] = 1 -
	// cell.sides[SimplePosElement.RIGHT] / sample.right;
	// cell.sides[SimplePosElement.TOP] = 1 - cell.sides[SimplePosElement.TOP] /
	// sample.top;
	// cell.sides[SimplePosElement.BOTTOM] = 1 -
	// cell.sides[SimplePosElement.BOTTOM] / sample.bottom;
	// cell.sides[SimplePosElement.FRONT] = 1 -
	// cell.sides[SimplePosElement.FRONT] / sample.front;
	// cell.sides[SimplePosElement.BACK] = 1 - cell.sides[SimplePosElement.BACK]
	// / sample.back;
	// }
	// public float[] getOcclusion() {
	// return occlusion;
	// }
	// void gatherBlock(int size, byte* data, Color* gathered, Sample* sample){
	// int xoff, yoff, zoff, value, r, p, obstacle;
	// Color* color;
	// Ray *ray, *rayend;
	// Offset *off, *offend;
	// float distance;
	//
	// foreach_xyz(0, size)
	// value = get(x,y,z);
	// if(value == 0 && neighbors(size, data, x, y, z)){
	// color = gathered + index(x,y,z);
	// for(ray=sample->rays, rayend=sample->rays+ray_count; ray<rayend; ray++){
	// for(off=ray->points, offend=ray->points+point_count; off<offend; off++){
	// xoff = x+off->x;
	// yoff = y+off->y;
	// zoff = z+off->z;
	// if(xoff < 0 || xoff >= size){
	// break;
	// }
	// else if(yoff < 0 || yoff >= size){
	// break;
	// }
	// else if(zoff < 0 || zoff >= size){
	// break;
	// }
	// else{
	// obstacle = get(xoff, yoff, zoff);
	// if(obstacle){
	// if(obstacle == LAVA){
	// color->r += 1.0/sqrt(off->depth);
	// color->g += 0.2/sqrt(off->depth);
	// }
	// break;
	// }
	// }
	// }
	// }
	// color->r /= ray_count;
	// color->g /= ray_count;
	// color->b /= ray_count;
	// }
	// endfor
	// }

}
