package org.schema.game.client.view.cubes.shapes.tetrahedron;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "TetrahedronTopBackLeft")
public class TetrahedronTopBackLeft extends TetrahedronShapeAlgorithm {

	private final int[] sidesAngled = new int[]{Element.TOP};
	private final int[] sidesToTest = new int[]{};
	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;
	@Override
	public byte[] getWedgeOrientation() {
		return new byte[]{Element.TOP, Element.LEFT, Element.FRONT};
	}
	public TetrahedronTopBackLeft() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		//		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(0.5f, 0.5f, -0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}
	@Override
	public Vector3i[] getAngledSideVerts() {
		return new Vector3i[]{
				new Vector3i(1, 1, -1),
				new Vector3i(-1, -1, -1),
				new Vector3i(1, -1, 1),
		};
	}
	@Override
	public int getDoubleVertex() {
		return 0+doubleAdd;
	}
	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.TOP) -> {
				p.normalMode = (Element.TOP + 1) * 64 + (Element.LEFT + 1) * 8 + (Element.FRONT + 1);
				if(p.vID == 1) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 2;
				} else if(p.vID == 3) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 0;
				} else if(p.vID == 2) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.BOTTOM) -> {
				if(p.vID == 1) {
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.LEFT) -> {
				p.vID = (short) 0;
				break;
			}
			case (Element.RIGHT) -> {
				if(p.vID == 3) {
					p.vID = (short) 2;
				}
				break;
			}
			case (Element.FRONT) -> {
				p.vID = (short) 0;
				break;
			}
			case (Element.BACK) -> {
				if(p.vID == 2) {
					p.vID = (short) 3;
				}
				break;
			}
		}

	}

	@Override
	protected ConvexShape getShape() {
		return shape;
	}

	@Override
	public int[] getSidesToCheckForVis() {
		return sidesToTest;
	}

	@Override
	public int[] getSidesAngled() {
		return sidesAngled;
	}

}
