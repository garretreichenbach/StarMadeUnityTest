package org.schema.game.common.controller.pathfinding;

import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.pathfinding.SegmentPathRequest;
import org.schema.schine.graphicsengine.forms.BoundingBox;

public class SegmentPathGroundCalculator extends AbstractAStarCalculator<SegmentPathRequest> {

	private final Vector3i tmpD = new Vector3i();
	private final SegmentPiece tmp = new SegmentPiece();
	private final Vector3i tmpV = new Vector3i();
	private final Vector3i tmpP = new Vector3i();
	private final Vector3i tmpT = new Vector3i();
	private final Vector3f bbTest = new Vector3f();
	private final Vector3f bbTestOut = new Vector3f();
	private Vector3i objectSize = new Vector3i();

	public SegmentPathGroundCalculator() {
		super(true);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.pathfinding.AbstractAStarCalculator#init(org.schema.game.server.controller.pathfinding.AbstractPathRequest)
	 */
	@Override
	public void init(SegmentPathRequest cr) {
		super.init(cr);

		this.objectSize.set(cr.getCallback().getObjectSize());
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

			//check basic fit of character
			boolean fits = checkFit(point, controller);
			if (!fits) {
				return false;
			}

			//check 'from' being solid -> move over ledges to fall
			//			boolean steppingOver = !checkFree(from.x,from.y-1,from.z) && from.y == point.y;
			boolean steppingOver = isOnGround(from, controller) && from.y == point.y;
			if (steppingOver) {
				return true;
			}
			//check if we can fall
			boolean falling = from.equals(point.x, point.y + 1, point.z);
			if (falling) {
				return true;
			}

			boolean wasFalling = false;
			boolean wasSteppingOver = false;

			long fromIndex = ElementCollection.getIndex(from);
			Node node = pathMap.get(fromIndex);
			if (node != null && fromIndex != currentStart) {
				ElementCollection.getPosFromIndex(node.parent, tmpP);

				wasFalling = from.y < tmpP.y;

				wasSteppingOver = !checkFree(tmpP.x, tmpP.y - 1, tmpP.z, controller) && checkFree(from.x, from.y - 1, from.z, controller) && tmpP.y == from.y;
			}
			//check if we can go on ground or if we have to jump (straight up only)
			boolean canStand = (!falling && !wasFalling && !wasSteppingOver) && (isOnGround(point, controller) ||
					canJumpStraight(point, from, controller));
			boolean ok = (canStand);

			return ok;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected float getWeight(Vector3i before, Vector3i from, Vector3i to) {
		float extra = 0;
		if (isTurn(before, from, to)) {
			extra = 4;
		}
		if (to.y > from.y) {
			//avoid jumping
			return getDistToSearchPos(from) + 4;
		} else {
			return extra + getDistToSearchPos(from);
		}
	}

	@Override
	protected float getWeightByBestDir(Vector3i before, Vector3i from, Vector3i to, Vector3i prefferedDir) {

		//		System.err.println("OOO GROUND: "+before+" -> "+from+" -> "+to);
		tmpD.sub(to, from);
		tmpD.sub(prefferedDir);
		boolean in = isInbound(to);
		float weight = (in ? 0 : 20);
		if (!in && !isInbound(from)) {
			BoundingBox boundingBox = roam;
			bbTest.set(to.x - SegmentData.SEG_HALF, to.y - SegmentData.SEG_HALF, to.z - SegmentData.SEG_HALF);
			boundingBox.getClosestPoint(bbTest, bbTestOut);
			bbTest.set(to.x - SegmentData.SEG_HALF, to.y - SegmentData.SEG_HALF, to.z - SegmentData.SEG_HALF);
			bbTest.sub(bbTestOut);
			weight += bbTest.length();
		}
		if (from.y < to.y) {
			//extra penality for going up
			weight += 0.3f;
		}
		return tmpD.length() + weight;

	}

	/**
	 * @param point
	 * @return true if there is solid blocks beneath one of the boxDimArea
	 * @throws IOException
	 */
	private boolean isOnGround(Vector3i point, SegmentController controller) throws IOException {
		for (int x = -(objectSize.x) + 1; x < objectSize.x; x++) {
			for (int z = -(objectSize.z) + 1; z < objectSize.z; z++) {

				if (!checkFree(point.x + (x), point.y - 1, point.z + (z), controller)) {
					return true;
				}
			}
		}
		return false;
		//orig: return !checkFree(point.x,point.y-1,point.z);
	}

	private boolean canJumpStraight(Vector3i point, Vector3i from, SegmentController controller) throws IOException {
		return (!checkFree(point.x, point.y - 2, point.z, controller) && from.x == point.x && from.z == point.z) ||
				(!checkFree(point.x, point.y - 3, point.z, controller) && from.x == point.x && from.z == point.z) ||
				(!checkFree(point.x, point.y - 4, point.z, controller) && from.x == point.x && from.z == point.z) ||
				(!checkFree(point.x, point.y - 5, point.z, controller) && from.x == point.x && from.z == point.z);
	}

	private boolean checkFit(Vector3i point, SegmentController controller) throws IOException {
		if (!checkFree(point.x, point.y, point.z, controller)) {
			return false;
		}
		for (int x = -(objectSize.x) + 1; x < objectSize.x; x++) {
			for (int z = -(objectSize.z) + 1; z < objectSize.z; z++) {
				for (int y = 0; y < objectSize.y; y++) {

					if (!checkFree(point.x + (x), point.y + (y), point.z + (z), controller)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private boolean checkFree(int x, int y, int z, SegmentController controller) throws IOException {
		if (controller == null) {
			return true;
		}
		tmpT.set(x, y, z);
		boolean inbound = controller.isInbound(Segment.getSegmentIndexFromSegmentElement(x, y, z, tmpV));//controller.isInboundAbs(point);
		return !inbound ||
				(!controller.getSegmentBuffer().existsPointUnsave(tmpT) ||
			!ElementKeyMap.getInfo(controller.getSegmentBuffer().getPointUnsave(tmpT, tmp).getType()).isPhysical(false)); //autorequest true previously
	}
}
