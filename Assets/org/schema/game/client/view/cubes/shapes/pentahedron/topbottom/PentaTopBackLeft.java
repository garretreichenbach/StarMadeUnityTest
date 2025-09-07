package org.schema.game.client.view.cubes.shapes.pentahedron.topbottom;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "PentaTopBackLeft")
public class PentaTopBackLeft extends PentaShapeAlgorithm {

	private final int[] sidesAngled = new int[]{Element.TOP};
	private final int[] sidesToTest = new int[]{};
	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;
	@Override
	public byte[] getWedgeOrientation() {
		return new byte[]{Element.TOP, Element.LEFT, Element.FRONT};
	}

	public PentaTopBackLeft() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(0.5f, 0.5f, -0.5f));
		points.add(new Vector3f(0.5f, 0.5f, 0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, -0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}
	@Override
	public Vector3i[] getAngledSideVerts() {
		return new Vector3i[]{
			new Vector3i(1, 1, 1),
			new Vector3i(-1, 1, -1),
			new Vector3i(-1, -1, 1),
		};
	}
	@Override
	public int getDoubleVertex(){
		return 2+doubleAdd;
	};
	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.TOP) -> {
				if(p.vID == 2) {
					p.vID = (short) 1;
				}
				break;
			}
			case (Element.LEFT) -> {
				if(p.vID == 0) {
					p.vID = (short) 3;
				}
				break;
			}
			case (Element.RIGHT) -> {
				break;
			}
			case (Element.FRONT) -> {
				if(p.vID == 1) {
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.BACK) -> {
				break;
			}
			case (6) -> {
				p.normalMode = (Element.TOP + 1) * 64 + (Element.LEFT + 1) * 8 + (Element.FRONT + 1);
				p.sid = Element.TOP;
				if(p.vID == 2) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 1;
				} else if(p.vID == 0) {
					p.vID = (short) 3;
				}
				break;
			}
		}

	}

	@Override
	public int getSixthSideOrientation() {
		return Element.TOP;
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
