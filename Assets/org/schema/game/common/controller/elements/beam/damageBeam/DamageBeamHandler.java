package org.schema.game.common.controller.elements.beam.damageBeam;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerCharacter;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.damage.beam.DamageBeamHittable;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.BeamHandler;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.physics.RigidBodySimple;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Collection;

public class DamageBeamHandler extends BeamHandler {

	public DamageBeamHandler(SegmentController s,
	                         BeamHandlerContainer<?> owner) {
		super(s, owner);
	}

	@Override
	public boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position) {
		return !controller.equals(getBeamShooter());
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
	public boolean ignoreBlock(short type) {
		ElementInformation f = ElementKeyMap.getInfoFast(type);
		return f.isDrawnOnlyInBuildMode() && !f.hasLod();
	}
	protected boolean isDamagingMines(BeamState bstate) {
		return true;
	}
	@Override
	public int onBeamHit(BeamState hittingBeam, int hits, BeamHandlerContainer<SegmentController> container, SegmentPiece hitPiece, Vector3f from,
	                     Vector3f to, Timer timer, Collection<Segment> updatedSegments) {
		if(!hitPiece.getSegmentController().canBeDamagedBy(hittingBeam.getHandler().getBeamShooter(), DamageDealerType.BEAM)) {
			return 0;
		}
		
		DamageBeamHitHandler ddb = ((DamageBeamHittable) hitPiece.getSegmentController()).getDamageBeamHitHandler();
		int beamHits = ((DamageBeamHitHandlerSegmentController)ddb).onBeamDamage(hittingBeam, hits, container, hitPiece, from,
                to, timer, updatedSegments);
		ddb.reset();
		hitPiece.refresh();
		return beamHits;
	}

	@Override
	protected boolean onBeamHitNonCube(BeamState hittingBeam, int hits, BeamHandlerContainer<SegmentController> container, Vector3f from,
	                                Vector3f to, CubeRayCastResult cubeResult, Timer timer, Collection<Segment> updatedSegments) {
		GameTransformable t = null;
		if (cubeResult.collisionObject instanceof RigidBodySimple) {
			t = ((RigidBodySimple) cubeResult.collisionObject).getSimpleTransformableSendableObject();
		} else if (cubeResult.collisionObject instanceof PairCachingGhostObjectAlignable) {
			t = ((PairCachingGhostObjectAlignable) cubeResult.collisionObject).getObj();
		}
		if(t instanceof AbstractCharacter<?>) {
			int beamHits = ((DamageBeamHitHandlerCharacter)((DamageBeamHittable)t).getDamageBeamHitHandler()).onBeamDamage((AbstractCharacter<?>)t, hittingBeam, hits, cubeResult, timer);
			return beamHits > 0;
//		}else if(t instanceof PlanetIcoCore) {
//			int beamHits = ((DamageBeamHitHandlerPlanetCore)((DamageBeamHittable)t).getDamageBeamHitHandler()).onBeamDamage((PlanetIcoCore) t, hittingBeam, hits, cubeResult, timer );
//			return beamHits > 0;
		}else {
			try {
				throw new RuntimeException("Not Beam Hit implementation for hitting "+t);	
			}catch(Exception r) {
				r.printStackTrace();
			}
			return false;
		}
		
//		if (t != null && t instanceof DamageBeamHittable) {
//			((DamageBeamHittable) t).handleBeamDamage(hittingBeam, hits, container, from, to, cubeResult, false, timer);
//		}
	}

	@Override
	protected boolean isConsiderZeroHpPhysical() {
		return false;
	}

	@Override
	protected boolean ignoreNonPhysical(BeamState con) {
		return false;
	}

	@Override
	public Vector4f getDefaultColor(BeamState beamState) {
		Vector4f clr = getColorRange(BeamColors.RED);
		return clr;
	}
	@Override
	public void onArmorBlockKilled(BeamState hitState, float armorValue) {
	}
}
