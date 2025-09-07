package org.schema.game.client.view.cubes.shapes.sprite;

import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShape;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.collision.shapes.ConvexShape;

@BlockShape(name = "SpriteRight")
public class SpriteRight extends SpriteShapeAlgorythm {

	private final int[] sidesAngled = new int[]{Element.TOP};
	private final int[] sidesToTest = new int[]{};

	public SpriteRight() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#findYZ(int)
	 */
	@Override
	protected int findYZ(int s) {
		return findIndex(SpriteLeft.class, s);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#findXZ(int)
	 */
	@Override
	protected int findXZ(int s) {
		return findIndex(getClass(), s);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#findXY(int)
	 */
	@Override
	protected int findXY(int s) {
		return findIndex(getClass(), s);
	}

	@Override
	public boolean isPhysical() {
		return false;
	}

	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;//(short) ((i)%4);
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
		switch(sideId) {
			case (Element.BOTTOM) -> {
				if(p.vID == 0) {
				} else if(p.vID == 1) {
				} else if(p.vID == 2) {
					p.sid = Element.TOP;
					p.vID = (short) 1;
				} else if(p.vID == 3) {
					p.sid = Element.TOP;
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.TOP) -> {
				if(p.vID == 0) {
				} else if(p.vID == 1) {
				} else if(p.vID == 2) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 1;
				} else if(p.vID == 3) {
					p.sid = Element.BOTTOM;
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.RIGHT) -> {
				p.vID = 0;
				break;
			}
			case (Element.LEFT) -> {
				p.vID = 0;
				break;
			}
			case (Element.FRONT) -> {
				if(p.vID == 0) {
				} else if(p.vID == 1) {
				} else if(p.vID == 2) {
					p.sid = Element.BACK;
					p.vID = (short) 1;
				} else if(p.vID == 3) {
					p.sid = Element.BACK;
					p.vID = (short) 0;
				}
				break;
			}
			case (Element.BACK) -> {
				if(p.vID == 0) {
				} else if(p.vID == 1) {
				} else if(p.vID == 2) {
					p.sid = Element.FRONT;
					p.vID = (short) 1;
				} else if(p.vID == 3) {
					p.sid = Element.FRONT;
					p.vID = (short) 0;
				}
				break;
			}
		}
	}

	@Override
	protected ConvexShape getShape() {
		assert false;
		throw new IllegalArgumentException();
	}

	@Override
	public int[] getSidesToCheckForVis() {
		return sidesToTest;
	}

	@Override
	public int[] getSidesAngled() {
		return sidesAngled;
	}
	@Override
	public byte getPrimaryOrientation() {
		return Element.RIGHT;
	}
}
