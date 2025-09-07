package org.schema.game.client.view.cubes.shapes.spike.topbottom;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.client.view.cubes.shapes.spike.SpikeShapeAlgorithm;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "SpikeTopBackLeft")
public class SpikeTopBackLeft extends SpikeShapeAlgorithm {

	
	private final int[] sidesToTest = new int[]{Element.BOTTOM};
	
	
	
	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;

	public SpikeTopBackLeft() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(0.5f, 0.5f, -0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}
	private final int[] sidesAngled = new int[]{Element.LEFT, Element.FRONT};
	@Override
	protected int[][] getNormals(){
		return new int[][]{{Element.TOP, Element.LEFT}, {Element.TOP, Element.FRONT}}; 
	}
	@Override
	public Vector3i[][] getAngledSideVerts() {
		return new Vector3i[][]{
			{
				new Vector3i(1, 1, -1),
				new Vector3i(1, 1, 1),
				new Vector3i(-1, -1, -1),
				new Vector3i(-1, -1, 1),
			},
			{
				new Vector3i(1, 1, -1),
				new Vector3i(-1, 1, -1),
				new Vector3i(-1, -1, 1),
				new Vector3i(1, -1, 1),
			},
		};
	}
	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.TOP) -> {
				p.vID = (short) 0;
				break;
			}
			case (Element.LEFT) -> {
				p.normalMode = (Element.TOP + 1) * 64 + (Element.LEFT + 1) * 8;
				if(p.vID == 1) {
					p.sid = Element.BACK;
					p.vID = (short) 3;
				} else if(p.vID == 0) {
					p.sid = Element.RIGHT;
					p.vID = (short) 2;
				}
				break;
			}
			case (Element.RIGHT) -> {
				if(p.vID == 3) {
					p.vID = (short) 2;
				}
				break;
			}
			case (Element.FRONT) -> {
				p.normalMode = (Element.TOP + 1) * 64 + (Element.FRONT + 1) * 8;
				if(p.vID == 0) {
					p.sid = Element.BACK;
					p.vID = (short) 3;
				} else if(p.vID == 1) {
					p.sid = Element.BACK;
					p.vID = (short) 3;
				} else if(p.vID == 2) {
				} else if(p.vID == 3) {
				}
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
