package org.schema.game.client.view.cubes.shapes.wedge;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "WedgeTopLeft")
public class WedgeTopLeft extends WedgeShapeAlgorithm {

	private final int[] sidesAngled = new int[]{Element.TOP};
	private final int[] sidesToTest = new int[]{Element.BOTTOM, Element.LEFT};
	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;

	public WedgeTopLeft() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, 0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}
	@Override
	public Vector3i[] getAngledSideVerts() {
		return new Vector3i[]{
			new Vector3i( 1, -1, -1),
			new Vector3i(-1,  1, -1),
			new Vector3i(-1,  1,  1),
			new Vector3i( 1, -1,  1),
		};
	}
	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#getWedgeOrientation()
	 */
	@Override
	public byte[] getWedgeOrientation() {
		return new byte[]{Element.RIGHT, Element.TOP};
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#getWedgeGravityValidDir(byte)
	 */
	@Override
	public byte getWedgeGravityValidDir(byte gravityDir) {
		if (gravityDir == Element.BOTTOM) {
			return Element.RIGHT;
		}
		if (gravityDir == Element.RIGHT) {
			return Element.TOP;
		}
		return -1;
	}

	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.TOP) -> {
				p.normalMode = (Element.TOP + 1) * 64 + (Element.RIGHT + 1) * 8;
				/**
				 * wedge top Back
				 */
				if(p.vID == 0) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 3;
				} else if(p.vID == 3) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.LEFT) -> {
				break;
			}
			case (Element.RIGHT) -> {
				p.vID = 0;
				break;
			}
			case (Element.BACK) -> {
				if(p.vID == 3) {
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.FRONT) -> {
				if(p.vID == 0) {
					p.vID = (short) 3;
				}
				break;
			}
		}
	}
	private int[] openToAirNone = new int[]{Element.BACK, Element.FRONT};
	@Override
	public int[] getSidesOpenToAir() {
		return openToAirNone;
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
