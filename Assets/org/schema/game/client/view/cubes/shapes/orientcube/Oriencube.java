package org.schema.game.client.view.cubes.shapes.orientcube;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteShapeAlgorythm;
import org.schema.game.common.data.element.Element;
import org.schema.schine.graphicsengine.core.GlUtil;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;

public abstract class Oriencube extends BlockShapeAlgorithm implements IconInterface {

	public int orientationArrayIndex;
	Vector3f finderVertexA = new Vector3f();
	Vector3f finderVertexB = new Vector3f();
	Vector3f finderVertex = new Vector3f();
	private final Transform basicTransform;
	private SpriteShapeAlgorythm repSprite;

	public Oriencube() {
		super();

		finderVertexA.set(Element.DIRECTIONSf[Element.switchLeftRight(getOrientCubePrimaryOrientation())]);
		finderVertexB.set(Element.DIRECTIONSf[Element.switchLeftRight(getOrientCubeSecondaryOrientation())]);
		finderVertexA.scale(1.5f);
		finderVertexB.scale(0.5f);

		finderVertex.add(finderVertexA, finderVertexB);
		
		basicTransform = new Transform();
		getPrimaryTransform(new Vector3f(), 0, basicTransform);
		Transform secondaryTransform = getSecondaryTransform(new Transform());
		
		basicTransform.mul(secondaryTransform);
	}
	@Override
	protected Vector3i[] getSideByNormal(int sideId, int normal) {
		return super.getSideByNormal(sideId%6, sideId%6);
	}
	@Override
	public boolean isAngled(int sideId) {
		return false;
	}
	public abstract byte getOrientCubePrimaryOrientation();

	public abstract byte getOrientCubeSecondaryOrientation();
	@Override
	protected int getAngledSideLightRepresentitive(int sideId, int normal) {
		return sideId%6;
	}
	private Oriencube getMirror(int mirrorAxisA, int mirrorAxisB) {
		int primary = getOrientCubePrimaryOrientation();
		int secondary = getOrientCubeSecondaryOrientation();
		//mirror primary if plane cuts it
		if (primary == mirrorAxisA || primary == mirrorAxisB) {
			primary = Element.getOpposite(primary);
			//if primary was mirroed, there is nothing else to do
		} else if (secondary == mirrorAxisA || secondary == mirrorAxisB) {
			secondary = Element.getOpposite(secondary);
		}

		return getOrientcube(primary, secondary);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#findYZ(int)
	 */
	@Override
	protected int findYZ(int orientation) {
		return getMirror(Element.RIGHT, Element.LEFT).orientationArrayIndex;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#findXZ(int)
	 */
	@Override
	protected int findXZ(int orientation) {
		return getMirror(Element.TOP, Element.BOTTOM).orientationArrayIndex;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm#findXY(int)
	 */
	@Override
	protected int findXY(int orientation) {
		return getMirror(Element.FRONT, Element.BACK).orientationArrayIndex;
	}

	@Override
	public int findRot(BlockShapeAlgorithm[] row, Matrix3f rot) {

		if (rotBuffer.containsKey(rot)) {
			return rotBuffer.get(rot);
		}
		for (int i = 0; i < row.length; i++) {
			BlockShapeAlgorithm blockShapeAlgorithm = row[i];
			if (!(blockShapeAlgorithm instanceof Oriencube) || this == blockShapeAlgorithm) {
				continue;
			}

			boolean matches = true;
			//						System.err.println("CHECKING "+own+" -> "+h);
			Vector3f ownV = new Vector3f(finderVertex);
			//searching for a flipped x
//				ownV.z = -ownV.z;
			rot.transform(ownV);

			//correct rounding errors from rotation
			if (ownV.x > 1.4f) {
				ownV.x = 1.5f;
			} else if (ownV.x > 0.4f) {
				ownV.x = 0.5f;
			} else if (ownV.x < -1.4f) {
				ownV.x = -1.5f;
			} else if (ownV.x < -0.4f) {
				ownV.x = -0.5f;
			} else {
				ownV.x = 0;
			}

			if (ownV.y > 1.4f) {
				ownV.y = 1.5f;
			} else if (ownV.y > 0.4f) {
				ownV.y = 0.5f;
			} else if (ownV.y < -1.4f) {
				ownV.y = -1.5f;
			} else if (ownV.y < -0.4f) {
				ownV.y = -0.5f;
			} else {
				ownV.y = 0;
			}

			if (ownV.z > 1.4f) {
				ownV.z = 1.5f;
			} else if (ownV.z > 0.4f) {
				ownV.z = 0.5f;
			} else if (ownV.z < -1.4f) {
				ownV.z = -1.5f;
			} else if (ownV.z < -0.4f) {
				ownV.z = -0.5f;
			} else {
				ownV.z = 0;
			}
			boolean found = false;
			Vector3f otherV = new Vector3f(((Oriencube) blockShapeAlgorithm).finderVertex);

//			System.err.println("COMPARING: "+this+" "+ownV+"    with    "+blockShapeAlgorithm+" "+otherV);
			if (otherV.equals(ownV)) {
				rotBuffer.put(new Matrix3f(rot), i);
				return i;
			}
		}
		throw new IllegalArgumentException(this.toString());

	}

	@Override
	public void createSide(int sideId, short i, AlgorithmParameters p) {
		p.vID = i;
		p.sid = sideId;
		p.normalMode = 0;
		p.ext = extOrderMap[sideId][i];
	}

	@Override
	protected ConvexShape getShape() {
		throw new NullPointerException();
	}

	@Override
	public boolean hasValidShape() {
		return false;
	}

	@Override
	public int[] getSidesToCheckForVis() {
		throw new NullPointerException();
	}

	@Override
	public int[] getSidesAngled() {
		throw new NullPointerException();
	}

	public abstract Oriencube getMirrorAlgo();

	public abstract Transform getPrimaryTransform(Vector3f blockPosLocal, int move, Transform out);

	public Transform getSecondaryTransform(Transform out) {
		out.setIdentity();
		return out;
	}

	//	public abstract Transform getPrimaryReflectTransform(Vector3f blockPosLocal,
//			Transform transform);
	public byte getOrientCubePrimaryOrientationSwitchedLeftRight() {
		byte o = getOrientCubePrimaryOrientation();
		if (o == Element.LEFT || o == Element.RIGHT) {
			o = (byte) Element.getOpposite(o);
		}

		return o;
	}

	public Matrix3f getOrientationMatrix(Matrix3f matrix3f) {
		Vector3f upR = new Vector3f(Element.DIRECTIONSf[getOrientCubePrimaryOrientation()]);
		Vector3f forwardR = new Vector3f(Element.DIRECTIONSf[getOrientCubeSecondaryOrientation()]);
		Vector3f rightr = new Vector3f();
		rightr.cross(upR, forwardR);
		rightr.normalize();
		
		matrix3f.setIdentity();
		
		GlUtil.setRightVector(rightr, matrix3f);
		GlUtil.setUpVector(upR, matrix3f);
		GlUtil.setForwardVector(forwardR, matrix3f);
		return matrix3f;
	}
	public Matrix3f getOrientationMatrixSwitched(Matrix3f matrix3f) {
		Vector3f upR = new Vector3f(Element.DIRECTIONSf[Element.switchLeftRight(getOrientCubePrimaryOrientation())]);
		Vector3f forwardR = new Vector3f(Element.DIRECTIONSf[Element.switchLeftRight(getOrientCubeSecondaryOrientation())]);
		Vector3f rightr = new Vector3f();
		rightr.cross(upR, forwardR);
		rightr.normalize();
		
		matrix3f.setIdentity();
		
		GlUtil.setRightVector(rightr, matrix3f);
		GlUtil.setUpVector(upR, matrix3f);
		GlUtil.setForwardVector(forwardR, matrix3f);
		return matrix3f;
	}
	public Transform getBasicTransform() {
		return basicTransform;
	}
	
	/**
	 * returns sprite that has the same primary
	 * @return
	 */
	public SpriteShapeAlgorythm getSpriteAlgoRepresentitive(){
		return repSprite;
	}
	@Override
	protected void onInit() {
		super.onInit();
		for(BlockShapeAlgorithm b : BlockShapeAlgorithm.algorithms[2]){
			SpriteShapeAlgorythm s = (SpriteShapeAlgorythm)b;
			
			if(s.getPrimaryOrientation() == this.getOrientCubePrimaryOrientation()){
				repSprite = s;
			}
		}
	}
	
}
