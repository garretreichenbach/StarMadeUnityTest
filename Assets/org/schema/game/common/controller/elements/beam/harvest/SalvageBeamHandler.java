package org.schema.game.common.controller.elements.beam.harvest;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SalvageBeamHitListener;
import com.bulletphysics.collision.dispatch.CollisionObject;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.Salvage;
import org.schema.game.common.controller.Salvager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.BeamHandler;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Collection;

public class SalvageBeamHandler extends BeamHandler {

	public SalvageBeamHandler(Salvager harvester, BeamHandlerContainer<?> owner) {
		super((SegmentController) harvester, owner);
	}

	public static int getRadius(float power) {
		int rad = (int) (FastMath.log(power / 200) / FastMath.log(2)) + 1;
		rad = Math.max(1, Math.min(rad, SalvageElementManager.SALVAGE_BEAM_SCALAR_RADIUS_CAP));
		return rad;
	}

	@Override
	public boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position) {
		return !controller.equals(getBeamShooter()) && controller instanceof Salvage && ((Salvage) controller).isSalvagableFor((Salvager) getBeamShooter(), cannotHitReason, position);
	}

	@Override
	public boolean ignoreBlock(short type) {
		ElementInformation f = ElementKeyMap.getInfoFast(type);
		return f.isDrawnOnlyInBuildMode() && !f.hasLod();
	}

	@Override
	public float getBeamTimeoutInSecs() {
		return BEAM_TIMEOUT_IN_SECS;
	}

	@Override
	public float getBeamToHitInSecs(BeamState beamState) {
		return beamState.getTickRate();
	}

	@Override
	protected boolean isHitsCubesOnly() {
		return true;
	}

	@Override
	protected boolean canHit(CollisionObject obj) {
		return !(obj.getUserPointer() instanceof StabilizerPath);
	}

	@Override
	public int onBeamHit(BeamState hittingBeam, int hits, BeamHandlerContainer<SegmentController> container, SegmentPiece hitPiece, Vector3f from, Vector3f to, Timer timer, Collection<Segment> updatedSegments) {
		((Salvager) getBeamShooter()).handleSalvage(hittingBeam, hits, container, from, hitPiece, timer, updatedSegments);
		((Salvage) hitPiece.getSegmentController()).handleBeingSalvaged(hittingBeam, container, to, hitPiece, hits);

		int r = getRadius(hittingBeam.getPower());
		int powerThreshold = 200;
		if(hittingBeam.getPower() >= powerThreshold) {
			int origX = hitPiece.getAbsolutePosX();
			int origY = hitPiece.getAbsolutePosY();
			int origZ = hitPiece.getAbsolutePosZ();
			for(int x = -r; x < r; x++) {
				for(int y = -r; y < r; y++) {
					for(int z = -r; z < r; z++) {
						if(x * x + y * y + z * z < r * r) {
							SegmentPiece pointUnsave = hitPiece.getSegmentController().getSegmentBuffer().getPointUnsave(origX + x, origY + y, origZ + z);
							if(pointUnsave != null) {
								((Salvager) getBeamShooter()).handleSalvage(hittingBeam, hits, container, from, pointUnsave, timer, updatedSegments);
								pointUnsave.setType((short) 0);
							}
						}
					}
				}
			}
		}

		//INSERTED CODE
		for(SalvageBeamHitListener listener : FastListenerCommon.salvageBeamHitListeners) {
			listener.handle(this, hittingBeam, hits, container, hitPiece, from, to, timer, updatedSegments);
		}
		///
		return hits;
	}

	@Override
	protected boolean onBeamHitNonCube(BeamState con, int hits, BeamHandlerContainer<SegmentController> owner, Vector3f from, Vector3f to, CubeRayCastResult cubeResult, Timer timer, Collection<Segment> updatedSegments) {
		return false;
	}

	@Override
	protected boolean ignoreNonPhysical(BeamState con) {
		return false;
	}

	@Override
	public Vector4f getDefaultColor(BeamState beamState) {
		Vector4f clr = getColorRange(BeamColors.BLUE);
		return clr;
	}

}