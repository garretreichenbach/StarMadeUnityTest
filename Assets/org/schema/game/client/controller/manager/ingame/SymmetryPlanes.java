package org.schema.game.client.controller.manager.ingame;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;

public class SymmetryPlanes {

	public static final int MODE_NONE = 0;
	public static final int MODE_XY = 1;
	public static final int MODE_XZ = 2;
	public static final int MODE_YZ = 4;
	public static final int MODE_COPY = 8;
	public static final int MODE_PASTE = 16;
	public static final int MODE_HELPER_PLACE = 32;
	private final Vector3i xyPlane = new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
	private final Vector3i xzPlane = new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
	private final Vector3i yzPlane = new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
	private boolean xyPlaneEnabled;
	private boolean xzPlaneEnabled;
	private boolean yzPlaneEnabled;
	private int xyExtraDist = 0;
	private int xzExtraDist = 0;
	private int yzExtraDist = 0;
	private boolean mirrorCubeShapes = true;
	private boolean mirrorNonCubicShapes = true;
	private int placeMode;
	private int extraDist = 0;

	public SymmetryPlanes() {
		super();
	}

	public int getXyExtraDist() {
		return xyExtraDist;
	}

	public void setXyExtraDist(int xyExtraDist) {
		this.xyExtraDist = xyExtraDist;
	}

	public int getXzExtraDist() {
		return xzExtraDist;
	}

	public void setXzExtraDist(int xzExtraDist) {
		this.xzExtraDist = xzExtraDist;
	}

	public int getYzExtraDist() {
		return yzExtraDist;
	}

	public void setYzExtraDist(int yzExtraDist) {
		this.yzExtraDist = yzExtraDist;
	}

	/**
	 * @return the extraDist
	 */
	public int getExtraDist() {
		return extraDist;
	}

	/**
	 * @param extraDist the extraDist to set
	 */
	public void setExtraDist(int extraDist) {
		this.extraDist = extraDist;
	}

	public int getMirrorOrientation(short type, int elementOrientation, boolean xy, boolean xz, boolean yz) {
		if (type != Element.TYPE_NONE) {

			ElementInformation info = ElementKeyMap.getInfo(type);

			if (info.getBlockStyle() != BlockStyle.NORMAL) {

//				if (!active) {
//					elementOrientation += SegmentData.MAX_ORIENT;
//				}

				int beforeMapped = elementOrientation;

				if (mirrorNonCubicShapes && xy) {
					int before = elementOrientation;
					elementOrientation = BlockShapeAlgorithm.xyMappings[info.blockStyle.id - 1][elementOrientation%24];
					//					System.err.println("MIRRORED ORIENTATION XY: "+before+"("+beforeMapped+")"+" -> "+elementOrientation);
				}
				if (mirrorNonCubicShapes && xz) {
					int before = elementOrientation;
					elementOrientation = BlockShapeAlgorithm.xzMappings[info.blockStyle.id - 1][elementOrientation%24];
					//					System.err.println("MIRRORED ORIENTATION XZ: "+before+"("+beforeMapped+")"+" -> "+elementOrientation);
				}
				if (mirrorNonCubicShapes && yz) {
					int before = elementOrientation;
					elementOrientation = BlockShapeAlgorithm.yzMappings[info.blockStyle.id - 1][elementOrientation%24];
					//					System.err.println("MIRRORED ORIENTATION YZ: "+before+"("+beforeMapped+")"+" -> "+elementOrientation);
				}

//				if (!active) {
//					elementOrientation -= SegmentData.MAX_ORIENT;
//				}
			} else {
				if (mirrorCubeShapes && type != ElementKeyMap.CARGO_SPACE && (ElementKeyMap.getInfo(type).getIndividualSides() > 0 || ElementKeyMap.getInfo(type).isOrientatable())) {
					if (xy && (elementOrientation == Element.FRONT || elementOrientation == Element.BACK)) {
						elementOrientation = Element.getOpposite(elementOrientation);
					}
					if (xz && (elementOrientation == Element.TOP || elementOrientation == Element.BOTTOM)) {
						elementOrientation = Element.getOpposite(elementOrientation);
					}
					if (yz && (elementOrientation == Element.RIGHT || elementOrientation == Element.LEFT)) {
						elementOrientation = Element.getOpposite(elementOrientation);
					}
				}
				assert (elementOrientation <= 6);
			}
		}

		return elementOrientation;
	}

	/**
	 * @return the placeMode
	 */
	public int getPlaceMode() {
		return placeMode;
	}

	/**
	 * @param placeMode the placeMode to set
	 */
	public void setPlaceMode(int placeMode) {
		this.placeMode = placeMode;
	}

	/**
	 * @return the xyPlane
	 */
	public Vector3i getXyPlane() {
		return xyPlane;
	}

	/**
	 * @return the xzPlane
	 */
	public Vector3i getXzPlane() {
		return xzPlane;
	}

	/**
	 * @return the yzPlane
	 */
	public Vector3i getYzPlane() {
		return yzPlane;
	}

	/**
	 * @return the xyPlaneEnabled
	 */
	public boolean isXyPlaneEnabled() {
		return xyPlaneEnabled;
	}

	/**
	 * @param xyPlaneEnabled the xyPlaneEnabled to set
	 */
	public void setXyPlaneEnabled(boolean xyPlaneEnabled) {
		this.xyPlaneEnabled = xyPlaneEnabled;
	}

	/**
	 * @return the xzPlaneEnabled
	 */
	public boolean isXzPlaneEnabled() {
		return xzPlaneEnabled;
	}

	/**
	 * @param xzPlaneEnabled the xzPlaneEnabled to set
	 */
	public void setXzPlaneEnabled(boolean xzPlaneEnabled) {
		this.xzPlaneEnabled = xzPlaneEnabled;
	}

	/**
	 * @return the yzPlaneEnabled
	 */
	public boolean isYzPlaneEnabled() {
		return yzPlaneEnabled;
	}

	/**
	 * @param yzPlaneEnabled the yzPlaneEnabled to set
	 */
	public void setYzPlaneEnabled(boolean yzPlaneEnabled) {
		this.yzPlaneEnabled = yzPlaneEnabled;
	}

	/**
	 * @return the mirrorCubeShapes
	 */
	public boolean isMirrorCubeShapes() {
		return mirrorCubeShapes;
	}

	/**
	 * @param mirrorCubeShapes the mirrorCubeShapes to set
	 */
	public void setMirrorCubeShapes(boolean mirrorCubeShapes) {
		this.mirrorCubeShapes = mirrorCubeShapes;
	}

	public boolean isMirrorNonCubicShapes() {
		return mirrorNonCubicShapes;
	}

	public void setMirrorNonCubicShapes(boolean mirrorNonCubicShapes) {
		this.mirrorNonCubicShapes = mirrorNonCubicShapes;
	}
	
	

}
