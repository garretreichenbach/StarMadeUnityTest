package org.schema.game.common.data.world;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.data.element.Element;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.SimplePosElement;

public class StaticObjectIntersection {
	public static final int TEST_LENGTH = 20;
	private static final float margin = Element.BLOCK_SIZE * 0.01f;
	Vector3b nearest = null;
	int side = -1;
	private Vector3f nearestIntersect = null;
	private Vector3f nMin = null;
	private Vector3f nMax = null;
	private boolean fromCameraViewer;

	public void getIntersect(Vector3f fromRay, Vector3f toRay,
	                         SegmentData segmentData) {
		for (byte z = 0; z < SegmentData.SEG; z++) {
			for (byte y = 0; y < SegmentData.SEG; y++) {
				for (byte x = 0; x < SegmentData.SEG; x++) {
					int index = SegmentData.getInfoIndex(x, y, z);
					if (!segmentData.contains(index)) {
						continue;
					}
					Vector3f currentTestPos = new Vector3f(x
							* Element.BLOCK_SIZE, y * Element.BLOCK_SIZE, z
							* Element.BLOCK_SIZE);
					Vector3f min = new Vector3f(currentTestPos);
					Vector3f max = new Vector3f(currentTestPos);

					min.x -= (Element.BLOCK_SIZE / 2f);
					min.y -= (Element.BLOCK_SIZE / 2f);
					min.z -= (Element.BLOCK_SIZE / 2f);

					max.x += (Element.BLOCK_SIZE / 2f);
					max.y += (Element.BLOCK_SIZE / 2f);
					max.z += (Element.BLOCK_SIZE / 2f);

					min.x -= SegmentData.SEG / 2 * Element.BLOCK_SIZE;
					min.y -= SegmentData.SEG / 2 * Element.BLOCK_SIZE;
					min.z -= SegmentData.SEG / 2 * Element.BLOCK_SIZE;

					max.x -= SegmentData.SEG / 2 * Element.BLOCK_SIZE;
					max.y -= SegmentData.SEG / 2 * Element.BLOCK_SIZE;
					max.z -= SegmentData.SEG / 2 * Element.BLOCK_SIZE;

					// B1.x += (float)Element.HALF_SIZE/2;
					// B1.y += (float)Element.HALF_SIZE/2;
					// B1.z += (float)Element.HALF_SIZE/2;

					// B2.x -= (float)Element.HALF_SIZE/2;
					// B2.y -= (float)Element.HALF_SIZE/2;
					// B2.z -= (float)Element.HALF_SIZE/2;

					float minDirLen = 0;
					if (fromCameraViewer) {
						minDirLen = Controller.getCamera().getCameraOffset();
					}
					Vector3f intersect = Vector3fTools.intersectsRayAABB(fromRay,
							toRay, min, max, minDirLen, TEST_LENGTH);
					if (intersect != null) {
						if (this.nearestIntersect == null
								|| Vector3fTools.sub(intersect, fromRay)
								.length() < Vector3fTools.sub(
								this.nearestIntersect, fromRay)
								.length()) {
							System.err.println("intersection: " + intersect);

							this.nearestIntersect = intersect;
							this.nMin = min;
							this.nMax = max;
							if (nearest == null) {
								nearest = new Vector3b();
							}
							this.nearest
									.set((byte) (currentTestPos.x / Element.BLOCK_SIZE),
											(byte) (currentTestPos.y / Element.BLOCK_SIZE),
											(byte) (currentTestPos.z / Element.BLOCK_SIZE));
							System.err.println("nearest is: " + nearest
									+ "; nearest intersection: "
									+ nearestIntersect);
						}
					}
				}
			}
		}

		if (this.nearest != null) {
			if (this.nearestIntersect.x >= this.nMax.x - margin) {
				this.side = SimplePosElement.RIGHT;
			} else if (this.nearestIntersect.y >= this.nMax.y - margin) {
				this.side = SimplePosElement.TOP;
			} else if (this.nearestIntersect.z >= this.nMax.z - margin) {
				this.side = SimplePosElement.FRONT;
			} else if (this.nearestIntersect.x <= this.nMin.x + margin) {
				this.side = SimplePosElement.LEFT;
			} else if (this.nearestIntersect.y <= this.nMin.y + margin) {
				this.side = SimplePosElement.BOTTOM;
			} else if (this.nearestIntersect.z <= this.nMin.z + margin) {
				this.side = SimplePosElement.BACK;
			}
		}
	}

	public boolean isIntersection() {
		return side >= 0;
	}
}
