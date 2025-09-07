package org.schema.game.client.view.cubes.shapes.wedge;

import javax.vecmath.Vector3f;

import org.schema.common.util.MemoryManager.MemFloatArray;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockRenderInfo;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "WedgeIcon")
public class WedgeIcon extends WedgeShapeAlgorithm {

	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;

	public WedgeIcon() {
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, 0.5f));
		points.add(new Vector3f(0.5f, 0.5f, 0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}

	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];

	}
//
//	@Override
//	public void create(int sideId, byte layer, short typeCode, byte hitPointsCode, byte animatedCode, int lightIndex, int sideOccId, int index, float segIndex, int halvedFactor, CubeMeshBufferContainer container, int resIndex) {
//
//	}

	@Override
	public void single(BlockRenderInfo ri, byte r, byte g, byte b, byte o, MemFloatArray buffer, AlgorithmParameters p) {
		for (short i = 0; i < CubeMeshBufferContainer.SQUARE_CORNERS; i++) {
			short dex = i;
			int sid = ri.sideId;
			byte ext = (byte) i;
			switch(ri.sideId) {
				case (Element.TOP) -> {
					/**
					 * wedge top Front
					 */
					if(dex == 0) {
						sid = Element.BOTTOM;
						dex = (short) 0;
					} else if(dex == 1) {
						sid = Element.BOTTOM;
						dex = (short) 3;
					} else if(dex == 2) {
						//				dex = (short) 0;
					} else if(dex == 3) {
						//				dex = (short) 1;
					}
					break;
				}
				case (Element.LEFT) -> {
					if(dex == 1) {
						dex = (short) 0;
					}
					break;
				}
				case (Element.RIGHT) -> {
					if(dex == 2) {
						dex = (short) 1;
					}
					break;
				}
				case (Element.BACK) -> {
					dex = 0;
					break;
				}
			}
			put(ri, sid, dex, ext, i, r, g, b, o, p.normalMode, p.insideMode, buffer);
		}
	}

	@Override
	protected ConvexShape getShape() {
		return shape;
	}

	@Override
	public int[] getSidesToCheckForVis() {
		return null;
	}

	@Override
	public int[] getSidesAngled() {
		return null;
	}

	@Override
	public Vector3i[] getAngledSideVerts() {
				return null;
	}

}
