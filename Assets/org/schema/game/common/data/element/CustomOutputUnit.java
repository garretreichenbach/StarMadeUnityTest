package org.schema.game.common.data.element;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class CustomOutputUnit<E extends CustomOutputUnit<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableElementManager<E, CM, EM>> extends FiringUnit<E, CM, EM> {
	private final ShootContainer shootContainer = new ShootContainer();
	private final Vector3i output = new Vector3i(0, 0, 0);
	private final Vector3i controlledFrom = new Vector3i();
	private final Vector3i centeralizedControlledFromPos = new Vector3i();

	/**
	 * @return the output
	 */
	public Vector3i getOutput() {
		return output;
	}

	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		super.calculateExtraDataAfterCreationThreaded(updateSignture, totalCollectionSet);
		SegmentPiece tmp = new SegmentPiece();
		boolean found = false;


		for(long index : getNeighboringCollection()) {
			SegmentPiece p = getSegmentController().getSegmentBuffer().getPointUnsave(index, tmp);
			if(p != null && p.isActive()) {
				getPosFromIndex(index, output);
				found = true;
				break;
			}
		}
		if(!found) {
			getSignificator(output);
		}
	}

	public void setMainPiece(SegmentPiece piece, boolean active) {
//		System.err.println(getSegmentController() + "; " + getSegmentController().getState() + " SET NEW MAIN PIECE: " + piece.getAbsolutePos(new Vector3i()) + "; ACTIVE: " + active);
		if(active) {
			if(piece.getSegment().getSegmentController().isOnServer()) {

				SegmentPiece other = new SegmentPiece();
				//set all other members of the unit inactive
				Vector3i pos = new Vector3i();
				for(long index : getNeighboringCollection()) {
					getPosFromIndex(index, pos);
					if(!piece.equalsPos(pos)) {

						piece.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(pos, other);
						if(other.isActive()) {
							//only send active blocks (to inactive)
							Vector4i d = new Vector4i();
							d.set(pos.x, pos.y, pos.z, -2); //setinactive

							((SendableSegmentController) getSegmentController()).getBlockActivationBuffer().enqueue(getDeactivation(pos.x, pos.y, pos.z, false, false));
						}
					}
				}
			}
			System.err.println(getSegmentController() + "; " + getSegmentController().getState() + " NEW OUTPUT SET: " + piece.getAbsolutePos(new Vector3i()) + "; ACTIVE: " + active);
			output.set(piece.getAbsolutePos(new Vector3i()));
		}
	}

	@Override
	public void fire(ControllerStateInterface unit, Timer timer) {
		super.fire(unit, timer);
		Vector3i v = output;

		shootContainer.weapontOutputWorldPos.set(v.x - SegmentData.SEG_HALF, v.y - SegmentData.SEG_HALF, v.z - SegmentData.SEG_HALF);
		if(getSegmentController().isOnServer()) {
			getSegmentController().getWorldTransform().transform(shootContainer.weapontOutputWorldPos);
		} else {
			getSegmentController().getWorldTransformOnClient().transform(shootContainer.weapontOutputWorldPos);
		}
		if(unit.getParameter(new Vector3i()).equals(Ship.core)) {
			//overwrite with current cockpit if entered from core
			unit.getControlledFrom(shootContainer.controlledFromOrig);
		} else {
			unit.getParameter(shootContainer.controlledFromOrig);
		}
		centeralizedControlledFromPos.set(shootContainer.controlledFromOrig);

		shootContainer.camPos.set(getSegmentController().getAbsoluteElementWorldPositionShifted(centeralizedControlledFromPos, shootContainer.tmpCampPos));

		unit.addCockpitOffset(shootContainer.camPos);

		doShot(unit, timer, shootContainer);

	}

	public abstract void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer);
}

