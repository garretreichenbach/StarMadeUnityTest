package org.schema.game.client.view.cubes.occlusion;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.SegmentData;
@Deprecated
public class NormalizerOld {
	private static final int[] mappings = new int[]{
			4, 5, 6,
			6, 7, 0,
			0, 1, 2,
			2, 3, 4
	};
	private static float[] multTable = new float[]{1f, 0.5f, 1f / 3f, 0.25f};
	final AlgorithmParameters po = new AlgorithmParameters();
	private final Vector4i[] p = new Vector4i[8];
	private final Vector4f avgLight[] = new Vector4f[4];
	private final Vector4f s[] = new Vector4f[4];
	private final Vector4i sPos[] = new Vector4i[4];
	private final Vector4f lightSum = new Vector4f();
	public short type;
	public boolean active;
	public byte orientation;
	public BlockStyle blockStyle;
	private Vector4i posAndSide = new Vector4i();

	public NormalizerOld() {
		for (int i = 0; i < 8; i++) {
			p[i] = new Vector4i();
		}
		for (int i = 0; i < 4; i++) {
			s[i] = new Vector4f();
			avgLight[i] = new Vector4f();
			sPos[i] = new Vector4i();
		}
	}

	private final void calcAverage(short inVid, Vector4i pos, Vector4i[] p, int sideId, Occlusion o) {
		BlockShapeAlgorithm algo = null;
		short m = (short) (inVid * 3);
		if (blockStyle != BlockStyle.NORMAL && blockStyle != BlockStyle.WEDGE) {
			algo = BlockShapeAlgorithm.getAlgo(blockStyle, orientation);
			if (algo.hasExtraSide() && algo.getSidesAngled().length > 0 && algo.getSidesAngled()[0] == sideId) {
				algo.createSide(6, inVid, po);
			} else {
				algo.createSide(sideId, inVid, po);
			}

			if (sideId == po.sid) {
				m = (short) (po.vID * 3);
			}
		}
		avgLight[inVid].set(getAverageLight(inVid, pos, p[mappings[m]], p[mappings[m + 1]], p[mappings[m + 2]], sideId, o, algo));//top right
	}

	private final void applyNormalization(Vector4i pos, Vector4i[] p, int sideId, SegmentData data, boolean flip, Occlusion o) {
		//top right, bottom right, bottom left, top left
		for (short vID = 0; vID < CubeMeshBufferContainer.VERTICES_PER_SIDE; vID++) {
			calcAverage(vID, pos, p, sideId, o);
		}

		//		avgLight[2].set(getAverageLight((short)2, pos, p[0], p[1], p[2], sideId, o, algo));//top right
		//		avgLight[3].set(getAverageLight((short)3, pos, p[2], p[3], p[4], sideId, o, algo));//bottom right
		//		avgLight[0].set(getAverageLight((short)0, pos, p[4], p[5], p[6], sideId, o, algo));//bottom left
		//		avgLight[1].set(getAverageLight((short)1, pos, p[6], p[7], p[0], sideId, o, algo));//bottom left

		int subSubIndex = sideId * CubeMeshBufferContainer.VERTICES_PER_SIDE;
		int lightInfoIndex = CubeMeshBufferContainer.getLightInfoIndex(pos.x, pos.y, pos.z);
		if (flip) {
			for (int i = 3; i >= 0; i--) {
				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].x, 0f, 1) * Occlusion.COLOR_PERM), subSubIndex + (3 - i), 0);
				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].y, 0f, 1) * Occlusion.COLOR_PERM), subSubIndex + (3 - i), 1);
				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].z, 0f, 1) * Occlusion.COLOR_PERM), subSubIndex + (3 - i), 2);

				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].w, 0f, 1) * Occlusion.COLOR_PERM), subSubIndex + (3 - i), 3);
			}
		} else {
			for (int i = 0; i < 4; i++) {
				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].x, 0, 1) * Occlusion.COLOR_PERM), subSubIndex + i, 0);
				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].y, 0, 1) * Occlusion.COLOR_PERM), subSubIndex + i, 1);
				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].z, 0, 1) * Occlusion.COLOR_PERM), subSubIndex + i, 2);

				o.getContainer().setFinalLight(lightInfoIndex, (byte) (FastMath.clamp(avgLight[i].w, 0, 1) * Occlusion.COLOR_PERM), subSubIndex + i, 3);
			}
		}
	}

	public boolean oneDiff(Vector4i posA, Vector4i posB) {
		if (posA.x == posB.x && posA.y == posB.y) {
			return true;
		}
		if (posA.x == posB.x && posA.z == posB.z) {
			return true;
		}
		if (posA.y == posB.y && posA.z == posB.z) {
			return true;
		}

		return true;
	}

	private Vector4f getAverageLight(short vertexNum, Vector4i posA, Vector4i posB, Vector4i posC, Vector4i posD, int sideId, Occlusion o, BlockShapeAlgorithm algo) {

		int sum = 0;
		lightSum.set(0, 0, 0, 0);
		sPos[0].set(posA);
		sPos[1].set(posB);
		sPos[2].set(posC);
		sPos[3].set(posD);

		if (algo != null) {
			if (po.sid != sideId) {

				//move normalization block to compensate for moved vertex
				Vector3i v = Element.DIRECTIONSi[Element.OPPOSITE_SIDE[sideId]];

				for (short i = 1; i < 4; i++) {
					assert (Occlusion.biggerSegmentValid(sPos[i].x + v.x, sPos[i].y + v.y, sPos[i].z + v.z)) : sPos[i] + " -> " + Element.getSideString(sideId) + " | " + Element.getSideString(po.sid) + ": " + (sPos[i].x + v.x) + ", " + (sPos[i].y + v.y) + ", " + (sPos[i].z + v.z) + "; algo: " + algo.getClass().getSimpleName();
					sPos[i].x += v.x;
					sPos[i].y += v.y;
					sPos[i].z += v.z;
				}

			} else if (algo.hasExtraSide() && algo.getSixthSideOrientation() == sideId) {
				Vector3i v = Element.DIRECTIONSi[sideId];
				for (short i = 0; i < 1; i++) {
					assert (Occlusion.biggerSegmentValid(sPos[i].x + v.x, sPos[i].y + v.y, sPos[i].z + v.z)) : sPos[i] + " -> " + Element.getSideString(sideId) + " | " + Element.getSideString(po.sid) + ": " + (sPos[i].x + v.x) + ", " + (sPos[i].y + v.y) + ", " + (sPos[i].z + v.z) + "; algo: " + algo.getClass().getSimpleName();
					sPos[i].x += v.x;
					sPos[i].y += v.y;
					sPos[i].z += v.z;
				}
			}
		}

		Vector3i v = Element.DIRECTIONSi[sideId];
		for (short i = 0; i < 4; i++) {
			boolean moved = false;

			if (i > 0 && o.contain.get(Occlusion.getContainIndex(sPos[i].x + v.x, sPos[i].y + v.y, sPos[i].z + v.z)) > 0) {
				if (!Occlusion.biggerSegmentValid(sPos[i].x + v.x, sPos[i].y + v.y, sPos[i].z + v.z)) {
					//					assert(false):"occlusion invalid"+Element.getSideString(sideId)+" | "+Element.getSideString(po.sid)+": "+(sPos[i].x + v.x)+", "+(sPos[i].y + v.y)+", "+(sPos[i].z + v.z)+"; algo: "+algo.getClass().getSimpleName();
					throw new IllegalArgumentException("occlusion invalid" + Element.getSideString(sideId) + " | " + Element.getSideString(po.sid) + ": " + (sPos[i].x + v.x) + ", " + (sPos[i].y + v.y) + ", " + (sPos[i].z + v.z) + "; algo: " + algo.getClass().getSimpleName());
				}
				sPos[i].x += v.x;
				sPos[i].y += v.y;
				sPos[i].z += v.z;
				moved = true;
			}
//			if (o.getLightVec(i == 0, moved, sPos[0], sPos[i], s[i], blockStyle, orientation, active, po)) {
//				lightSum.add(s[i]);
//				sum++;
//			}
		}
		if (sum > 1) {
			lightSum.scale(multTable[sum - 1]);
		}

		return lightSum;
	}

	public void normalize(Vector3i pos, SegmentData data, int sideId, Occlusion o) {

		boolean t = sideId == 1 || sideId == 3 || sideId == 5;
		if (sideId < 2) {
			//			normalizeFrontBack(pos, data, sideId, o);

		} else if (sideId < 4) {
			if (!t) {
				normalizeTop(pos, data, sideId, o);
			} else {
				normalizeBottom(pos, data, sideId, o);
			}
		} else {
			if (!t) {
				normalizeRight(pos, data, sideId, o);
			} else {
				normalizeLeft(pos, data, sideId, o);
			}
		}
	}

	final void normalizeFront(Vector3i pos, SegmentData data, int sideId, Occlusion o) {

		posAndSide.set(pos.x, pos.y, pos.z, sideId);
		;

		p[0].set(pos.x - 1, pos.y, 		pos.z, sideId);        //bottom
		p[1].set(pos.x - 1, pos.y + 1, 	pos.z, sideId);    //bottom left
		p[2].set(pos.x, 	pos.y + 1, 	pos.z, sideId);    //left
		p[3].set(pos.x + 1, pos.y + 1, 	pos.z, sideId);    //top left
		p[4].set(pos.x + 1, pos.y, 		pos.z, sideId);    //top
		p[5].set(pos.x + 1, pos.y - 1, 	pos.z, sideId);    //top right
		p[6].set(pos.x, 	pos.y - 1, 	pos.z, sideId);    //right
		p[7].set(pos.x - 1, pos.y - 1, 	pos.z, sideId);    //bottom right

		applyNormalization(posAndSide, p, sideId, data, true, o);

	}

	final void normalizeBack(Vector3i pos, SegmentData data, int sideId, Occlusion o) {

		posAndSide.set(pos.x, pos.y, pos.z, sideId);
		;
		p[0].set(pos.x - 1, pos.y, 		pos.z, sideId);        //bottom
		p[1].set(pos.x - 1, pos.y + 1, 	pos.z, sideId);    //bottom left
		p[2].set(pos.x, 	pos.y + 1, 	pos.z, sideId);    //left
		p[3].set(pos.x + 1, pos.y + 1, 	pos.z, sideId);    //top left
		p[4].set(pos.x + 1, pos.y,		pos.z, sideId);    //top
		p[5].set(pos.x + 1, pos.y - 1, 	pos.z, sideId);    //top right
		p[6].set(pos.x, 	pos.y - 1, 	pos.z, sideId);    //right
		p[7].set(pos.x - 1, pos.y - 1, 	pos.z, sideId);    //bottom right

		applyNormalization(posAndSide, p, sideId, data, false, o);

	}

	final void normalizeLeft(Vector3i pos, SegmentData data, int sideId, Occlusion o) {

		posAndSide.set(pos.x, pos.y, pos.z, sideId);
		;

		p[0].set(pos.x, pos.y, 		pos.z - 1, 	sideId);    //right
		p[1].set(pos.x, pos.y - 1, 	pos.z - 1, 	sideId);    //bottom right
		p[2].set(pos.x, pos.y - 1, 	pos.z, 		sideId);        //bottom
		p[3].set(pos.x, pos.y - 1, 	pos.z + 1, 	sideId);    //bottom left
		p[4].set(pos.x, pos.y, 		pos.z + 1, 	sideId);    //left
		p[5].set(pos.x, pos.y + 1, 	pos.z + 1, 	sideId);    //top left
		p[6].set(pos.x, pos.y + 1, 	pos.z, 		sideId);        //top
		p[7].set(pos.x, pos.y + 1, 	pos.z - 1, 	sideId);    //top right

		applyNormalization(posAndSide, p, sideId, data, false, o);

	}

	final void normalizeRight(Vector3i pos, SegmentData data, int sideId, Occlusion o) {

		posAndSide.set(pos.x, pos.y, pos.z, sideId);
		;
		p[0].set(pos.x, pos.y, 		pos.z - 1, 	sideId);    //right
		p[1].set(pos.x, pos.y + 1, 	pos.z - 1, 	sideId);    //bottom right
		p[2].set(pos.x, pos.y + 1, 	pos.z, 		sideId);        //bottom
		p[3].set(pos.x, pos.y + 1, 	pos.z + 1, 	sideId);    //bottom left
		p[4].set(pos.x, pos.y, 		pos.z + 1, 	sideId);    //left
		p[5].set(pos.x, pos.y - 1, 	pos.z + 1, 	sideId);    //top left
		p[6].set(pos.x, pos.y - 1, 	pos.z, 		sideId);        //top
		p[7].set(pos.x, pos.y - 1, 	pos.z - 1, 	sideId);    //top right

		applyNormalization(posAndSide, p, sideId, data, false, o);

	}

	final void normalizeBottom(Vector3i pos, SegmentData data, int sideId, Occlusion o) {
		posAndSide.set(pos.x, pos.y, pos.z, sideId);
		;
		p[0].set(pos.x - 1, pos.y, pos.z, 		sideId);    //top
		p[1].set(pos.x - 1, pos.y, pos.z - 1, 	sideId);    //top right
		p[2].set(pos.x, 	pos.y, pos.z - 1, 	sideId);    //right
		p[3].set(pos.x + 1, pos.y, pos.z - 1, 	sideId);    //bottom right
		p[4].set(pos.x + 1, pos.y, pos.z, 		sideId);    //bottom
		p[5].set(pos.x + 1, pos.y, pos.z + 1, 	sideId);    //bottom left
		p[6].set(pos.x, 	pos.y, pos.z + 1, 	sideId);    //left
		p[7].set(pos.x - 1, pos.y, pos.z + 1, 	sideId);    //top left

		applyNormalization(posAndSide, p, sideId, data, false, o);

	}

	final void normalizeTop(Vector3i pos, SegmentData data, int sideId, Occlusion o) {
		posAndSide.set(pos.x, pos.y, pos.z, sideId);
		;

		p[0].set(pos.x - 1, pos.y, pos.z, 		sideId);    //top
		p[1].set(pos.x - 1, pos.y, pos.z + 1, 	sideId);    //top right
		p[2].set(pos.x, 	pos.y, pos.z + 1, 	sideId);    //right
		p[3].set(pos.x + 1, pos.y, pos.z + 1, 	sideId);    //bottom right
		p[4].set(pos.x + 1, pos.y, pos.z, 		sideId);    //bottom
		p[5].set(pos.x + 1, pos.y, pos.z - 1, 	sideId);    //bottom left
		p[6].set(pos.x, 	pos.y, pos.z - 1, 	sideId);    //left
		p[7].set(pos.x - 1, pos.y, pos.z - 1, 	sideId);    //top left

		applyNormalization(posAndSide, p, sideId, data, false, o);

	}
}
