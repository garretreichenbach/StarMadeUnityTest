package org.schema.game.client.view.cubes.shapes.spike;

import javax.vecmath.Vector3f;

import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.util.ObjectArrayList;

@BlockShape(name = "SpikeIcon")
public class SpikeIcon extends SpikeShapeAlgorithm {

	private org.schema.game.common.data.physics.ConvexHullShapeExt shape;

	public SpikeIcon() {
		super();
		ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

		points.add(new Vector3f(-0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, 0.5f));
		points.add(new Vector3f(0.5f, -0.5f, -0.5f));
		points.add(new Vector3f(-0.5f, 0.5f, 0.5f));

		shape = new org.schema.game.common.data.physics.ConvexHullShapeExt(points);
	}

	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.TOP) -> {
				/**
				 * spike top
				 */
				if(p.vID == 0) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 0;
				} else if(p.vID == 1) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 3;
				} else if(p.vID == 2) {
				} else if(p.vID == 3) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 1;
				}
				break;
			}
			case (Element.LEFT) -> {
				//			dex = 0;
				if(p.vID == 1) {
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.RIGHT) -> {
				p.vID = 0;
				break;
			}
			case (Element.FRONT) -> {
				//			dex = 0;
				if(p.vID == 0) {
					p.vID = (short) 1;
				}
				break;
			}
			case (Element.BACK) -> {
				p.vID = 0;
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
				return null;
	}

	@Override
	public int[] getSidesAngled() {
		return null;
	}

	@Override
	protected int[][] getNormals() {
				return null;
	}
}
