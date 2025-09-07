package org.schema.game.client.view.cubes.shapes.wedge;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "WedgeBottomBack")
public class WedgeBottomBack extends WedgeShapeAlgorithm {

	private final int[] sidesAngled = new int[]{Element.BOTTOM};
	private final int[] sidesToTest = new int[]{Element.TOP, Element.BACK};
	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;

	
	
	
	public WedgeBottomBack() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, 0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, 0.5f));
		points.add(new Vector3f(0.5f, 0.5f, 0.5f));
		points.add(new Vector3f(0.5f, 0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#getWedgeOrientation()
	 */
	@Override
	public byte[] getWedgeOrientation() {
		return new byte[]{Element.FRONT, Element.BOTTOM};
	}

	@Override
	public byte getWedgeGravityValidDir(byte gravityDir) {
		if (gravityDir == Element.TOP) {
			return Element.FRONT;
		}
		if (gravityDir == Element.FRONT) {
			return Element.BOTTOM;
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
			case (Element.BOTTOM) -> {
				p.normalMode = (Element.BOTTOM + 1) * 64 + (Element.FRONT + 1) * 8;
				/**
				 * wedge top Front
				 */
				if(p.vID == 1) {
					p.sid = Element.TOP;
					p.vID = (short) 2;
				} else if(p.vID == 0) {
					p.sid = Element.TOP;
					p.vID = (short) 3;
				} else if(p.vID == 2) {
				} else if(p.vID == 3) {
				}
				break;
			}
			case (Element.LEFT) -> {
				if(p.vID == 3) {
					p.vID = (short) 2;
				}
				break;
			}
			case (Element.RIGHT) -> {
				if(p.vID == 0) {
					p.vID = (short) 1;
				}
				break;
			}
			case (Element.FRONT) -> {
				p.vID = 0;
				break;
			}
		}

	}
	private int[] openToAirNone = new int[]{Element.LEFT, Element.RIGHT};
	@Override
	public int[] getSidesOpenToAir() {
		return openToAirNone;
	}
	@Override
	public Vector3i[] getAngledSideVerts() {
		return new Vector3i[]{
				new Vector3i(1, 1, 1),
				new Vector3i(-1, 1, 1),
				new Vector3i(-1, -1, -1),
				new Vector3i(1, -1, -1),
		};
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
