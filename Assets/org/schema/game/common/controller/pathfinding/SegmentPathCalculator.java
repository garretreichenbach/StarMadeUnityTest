package org.schema.game.common.controller.pathfinding;

import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.pathfinding.SegmentPathRequest;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class SegmentPathCalculator extends AbstractAStarCalculator<SegmentPathRequest> {

	private final SegmentPiece tmp = new SegmentPiece();
	private final Vector3i tmpV = new Vector3i();
	private final Vector3i tmpD = new Vector3i();
	private final Vector3f bbTest = new Vector3f();
	private final Vector3f bbTestOut = new Vector3f();
	private final Vector3i tfromTmp = new Vector3i();
	private final Vector3i ttoTmp = new Vector3i();
	private final Vector3i fromTmp = new Vector3i();
	private final Vector3i toTmp = new Vector3i();
	private final Vector3i dir = new Vector3i();
	Vector3i tmpT = new Vector3i();
	private Vector3i absPosDebug = new Vector3i();

	public SegmentPathCalculator() {
		super(true);

	}

	private boolean checkFree(int x, int y, int z, SegmentController controller) throws IOException {
		if (controller == null) {
			return true;
		}
		tmpT.set(x, y, z);
		boolean inbound = controller.isInbound(Segment.getSegmentIndexFromSegmentElement(x, y, z, tmpV));//controller.isInboundAbs(point);
		SegmentPiece p;
		return !inbound ||
			(!controller.getSegmentBuffer().existsPointUnsave(tmpT) || (((p = controller.getSegmentBuffer().getPointUnsave(tmpT, tmp)) != null && p.getType() != 0) &&
						!ElementKeyMap.getInfo(p.getType()).isPhysical(false)));//autorequest true previously
	}

	@Override
	public void optimizePath(LongArrayList path) {
		if (path.size() > 2) {
			long checkPoint = path.getLong(0);
			long currentPoint = path.getLong(1);
			int i = 1;
			while (i + 1 < path.size()) {
				if (isWalkable(checkPoint, path.getLong(i + 1))) {
					//					System.err.println("IS WALKABLE");
					// Make a straight path between those points:
					long temp = currentPoint;
					currentPoint = path.getLong(i + 1);
					//delete temp from the path
					Long index = path.remove(i);

				} else {

					checkPoint = path.get(i);
					currentPoint = path.get(i + 1);
					i++;
				}
			}
		}

		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			for (long index : path) {
				ElementCollection.getPosFromIndex(index, absPosDebug);
				Vector3f vv = new Vector3f(absPosDebug.x - SegmentData.SEG_HALF, absPosDebug.y - SegmentData.SEG_HALF, absPosDebug.z - SegmentData.SEG_HALF);
				if (controller != null) {
					controller.getWorldTransform().transform(vv);
				}
				DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1, 1, 0, 1));
				debugPoint.LIFETIME = 5000;
				debugPoint.size = 1.1f;
				DebugDrawer.points.add(debugPoint);
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.pathfinding.AbstractAStarCalculator#getMoveWeight()
	 */
	@Override
	protected float getMoveWeight(Vector3i before, Vector3i from, Vector3i to) {
		float extra = 0;
		if (isTurn(before, from, to)) {
			//			System.err.println("TURNNN! "+before+", "+from+", "+to);
			return 2.5f;
		}
		return super.getMoveWeight(before, from, to);
	}

	@Override
	public boolean canTravelPoint(Vector3i point, Vector3i from, SegmentController controller) {

		try {

			return checkFree(point.x, point.y, point.z, controller) && checkFree(point.x, point.y + 1, point.z, controller);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected float getWeight(Vector3i before, Vector3i from, Vector3i to) {
		//		System.err.println("OOO: "+before+" -> "+from+" -> "+to);

		return getDistToSearchPos(from);
	}

	@Override
	protected float getWeightByBestDir(Vector3i before, Vector3i from, Vector3i to, Vector3i prefferedDir) {

		//		System.err.println("OOO: "+before+" -> "+from+" -> "+to);

		tmpD.sub(to, from);
		tmpD.sub(prefferedDir);

		boolean in = isInbound(to);
		float weight = (in ? 0 : 20);

		if (!in && !isInbound(from)) {
			assert (roam.isInitialized());
			BoundingBox boundingBox = roam;
			bbTest.set(to.x - SegmentData.SEG_HALF, to.y - SegmentData.SEG_HALF, to.z - SegmentData.SEG_HALF);
			boundingBox.getClosestPoint(bbTest, bbTestOut);

			bbTest.set(to.x - SegmentData.SEG_HALF, to.y - SegmentData.SEG_HALF, to.z - SegmentData.SEG_HALF);
			bbTest.sub(bbTestOut);

			weight += bbTest.length();
		}
//		assert (weight < 100) : weight;

		float resWeight = tmpD.length() * 0.5f + weight;

//		assert (resWeight < 100) : resWeight;
		return resWeight;
	}

	private boolean checkPos(int x, int y, int z) {
		//		try {
		//			if(!checkFree(x, y, z, controller)){
		//				System.err.println("CANNOT WALK OK "+x+", "+y+", "+z);
		//				return false;
		//			}
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		for (int fz = -1; fz < 2; fz++) {
			for (int fx = -1; fx < 2; fx++) {
				int xPos = x + fx;
				int zPos = z + fz;
				try {
					if (!checkFree(xPos, y, zPos, controller)) {
						//						System.err.println("CANNOT WALK OK "+xPos+", "+y+", "+zPos);
						return false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//		System.err.println("CAN WALK OK "+x+", "+y+", "+z);
		return true;
	}

	private boolean isWalkable(long from, long to) {
		ElementCollection.getPosFromIndex(from, fromTmp);
		ElementCollection.getPosFromIndex(to, toTmp);
		if (fromTmp.y != toTmp.y) {
			return false;
		} else {
			while (fromTmp.x != toTmp.x && fromTmp.z != toTmp.z) {
				int dx = Math.abs(toTmp.x - fromTmp.x), sx = fromTmp.x < toTmp.x ? 1
						: -1;
				int dy = -Math.abs(toTmp.z - fromTmp.z), sy = fromTmp.z < toTmp.z ? 1
						: -1;
				int err = dx + dy, e2; /* error value e_xy */

				for (; ; ) { /* loop */
					boolean checkPos = checkPos(fromTmp.x, fromTmp.y, fromTmp.z);
					if (!checkPos) {
						return false;
					}
					if (fromTmp.x == toTmp.x && fromTmp.z == toTmp.z)
						break;
					e2 = 2 * err;
					if (e2 > dy) {
						err += dy;
						fromTmp.x += sx;
					} /* e_xy+e_x > 0 */
					if (e2 < dx) {
						err += dx;
						fromTmp.z += sy;
					} /* e_xy+e_y < 0 */
				}
			}

			return true;
		}
	}

}
