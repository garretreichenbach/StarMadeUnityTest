package org.schema.game.client.view.cubes.shapes.pentahedron.topbottom;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "PentaBottomBackRight")
public class PentaBottomBackRight extends PentaShapeAlgorithm {

	private final int[] sidesAngled = new int[]{Element.BOTTOM};
	private final int[] sidesToTest = new int[]{};
	@Override
	public byte[] getWedgeOrientation() {
		return new byte[]{Element.BOTTOM, Element.RIGHT, Element.FRONT};
	}
	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;

	public PentaBottomBackRight() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, 0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, 0.5f));
		points.add(new Vector3f(0.5f, 0.5f, 0.5f));
		points.add(new Vector3f(0.5f, 0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}
	@Override
	public Vector3i[] getAngledSideVerts() {
		return new Vector3i[]{
				new Vector3i(1, 1, 1),
				new Vector3i(1, -1, -1),
				new Vector3i(-1, -1, 1),
		};
	}
	@Override
	public int getDoubleVertex() {
		return 1+doubleAdd;
	}
	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.BOTTOM) -> {
				if(p.vID == 0) {
					p.vID = (short) 1;
				}
				break;
			}
			case (Element.LEFT) -> {
				break;
			}
			case (Element.RIGHT) -> {
				if(p.vID == 0) {
					p.vID = (short) 1;
				}
				break;
			}
			case (Element.FRONT) -> {
				if(p.vID == 3) {
					p.vID = (short) 2;
				}
				break;
			}
			case (Element.BACK) -> {
				break;
			}
			case (6) -> {
				//			p.normalMode = (Element.BOTTOM+1)*64 + (Element.LEFT+1)*8 + (Element.BACK+1);
				p.normalMode = (Element.BOTTOM + 1) * 64 + (Element.RIGHT + 1) * 8 + (Element.FRONT + 1);
				//extra face
				p.sid = Element.BOTTOM;
				if(p.vID == 0) {
					p.sid = Element.TOP;
					p.vID = (short) 3;
				} else if(p.vID == 2) {
					p.vID = (short) 1;
				} else if(p.vID == 3) {
				} else if(p.vID == 1) {
				}
				//			p.ext = extOrderMap[p.sid][i];
				break;
			}
		}

	}

	@Override
	public int getSixthSideOrientation() {
		return Element.BOTTOM;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#hasExtraSide()
	 */
	@Override
	public boolean hasExtraSide() {
		return true;
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
