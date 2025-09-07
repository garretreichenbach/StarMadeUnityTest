package org.schema.game.common.controller.elements.beam.tractorbeam;

import java.util.Collection;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.Salvager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.BeamHandler;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

public class TractorBeamHandler extends BeamHandler {

	private static final float SPEED_MULT_PLAYER = 2.0f;

	private Vector3f linearVelocityTmp = new Vector3f();

	private float timeTracker;

	private Transform tmp = new Transform();

	private boolean wasRightDown;

	public enum TractorMode {

		HOLD(en -> {
			return Lng.str("Hold");
		}), PUSH(en -> {
			return Lng.str("Push");
		}), PULL(en -> {
			return Lng.str("Pull");
		});

		private final Translatable nm;

		private TractorMode(Translatable nm) {
			this.nm = nm;
		}

		public String getName() {
			return nm.getName(this);
		}
	}

	public TractorBeamHandler(Salvager harvester, BeamHandlerContainer<?> owner) {
		super((SegmentController) harvester, owner);
	}

	@Override
	public boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position) {
		return !controller.equals(getBeamShooter());
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
		if (getBeamShooter().isOnServer()) {
			if (hittingBeam.currentHit != null) {
				SegmentController segmentController = hittingBeam.currentHit.getSegmentController();
				if (!(segmentController.railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBodySegmentController)) {
					return 0;
				}
				segmentController.calcWorldTransformRelative(getBeamShooter().getSectorId(), getBeamShooter().getSector(new Vector3i()));
				tmp.set(getBeamShooter().getWorldTransformInverse());
				tmp.mul(segmentController.getClientTransform());
				Vector3f m = new Vector3f();
				TractorMode mode = getMode(hittingBeam);
				switch(mode) {
					case HOLD:
						break;
					case PUSH:
						{
							Vector3f dir = new Vector3f(hittingBeam.initalRelativeTranform.origin);
							if (dir.lengthSquared() > 0) {
								dir.normalize();
								hittingBeam.initalRelativeTranform.origin.add(dir);
							}
						}
						break;
					case PULL:
						{
							Vector3f dir = new Vector3f(hittingBeam.initalRelativeTranform.origin);
							if (dir.lengthSquared() > 6) {
								dir.normalize();
								hittingBeam.initalRelativeTranform.origin.sub(dir);
							}
						}
						break;
					default:
						break;
				}
				m.sub(hittingBeam.initalRelativeTranform.origin, tmp.origin);
				getBeamShooter().getWorldTransform().basis.transform(m);
				moveTo(timer, hittingBeam, m, segmentController);
			}
		}
		// System.err.println("HITTING :::: "+getShootingEntity().getState()+"; "+hits);
		return hits;
	}

	private TractorMode getMode(BeamState hittingBeam) {
		TractorElementManager tr = ((ShipManagerContainer) ((ManagedSegmentController<?>) getBeamShooter()).getManagerContainer()).getTractorBeam().getElementManager();
		TractorBeamCollectionManager col = tr.getCollectionManagersMap().get(hittingBeam.weaponId);
		if (col != null) {
			return col.getTractorMode();
		} else {
			assert (false) : "tractor beam not found " + hittingBeam.weaponId;
			System.err.println(getState() + " ERROR: Tractor Mode not found for " + getBeamShooter() + "; " + hittingBeam.weaponId);
			return TractorMode.HOLD;
		}
	}

	public void moveTo(Timer timer, BeamState hittingBeam, final Vector3f toDir, SegmentController entity) {
		if (entity.getDockingController().isDocked() || entity.railController.isDockedOrDirty()) {
			return;
		}
		if (!(entity.getPhysicsDataContainer().getObject() instanceof RigidBody)) {
			System.err.println("[TRACTORBEAM] cant move: " + entity);
			return;
		}
		float thrusters = Math.min(Math.max(1, entity.getMass() * TractorElementManager.FORCE_TO_MASS_MAX), hittingBeam.getPower());
		// one udpate for every 30 ms
		float updateFrequency = ThrusterElementManager.getUpdateFrequency();
		float dist = toDir.length();
		Vector3f dir = new Vector3f(toDir);
		dir.normalize();
		RigidBody body = (RigidBody) entity.getPhysicsDataContainer().getObject();
		timeTracker += timer.getDelta();
		// make sure it's save against long lags
		timeTracker = Math.min(updateFrequency * 100f, timeTracker);
		while (timeTracker >= updateFrequency) {
			timeTracker -= updateFrequency;
			float nThrust = thrusters;
			if (dist > 0.05) {
				body.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(879);
				body.getLinearVelocity(linearVelocityTmp);
				Vector3f linearVelocity = body.getLinearVelocity(new Vector3f());
				Vector3f a = new Vector3f(linearVelocityTmp);
				a.normalize();
				if (Vector3fTools.diffLength(a, dir) > 1.5f) {
					// break down current speed to avoid sidewards acceleration (flying in cycles around target)
					linearVelocity.scale(0.1f);
					body.setLinearVelocity(linearVelocity);
				// thrust is full since we want to correct course
				} else if (Vector3fTools.diffLength(a, dir) > 1.0f) {
					// break down current speed to avoid sidewards acceleration (flying in cycles around target)
					linearVelocity.scale(0.4f);
					body.setLinearVelocity(linearVelocity);
				// thrust is full since we want to correct course
				} else if (Vector3fTools.diffLength(a, dir) > 0.3f) {
					// break down current speed to avoid sidewards acceleration (flying in cycles around target)
					linearVelocity.scale(0.7f);
					body.setLinearVelocity(linearVelocity);
				} else if (dist < 2) {
					if (linearVelocity.length() > 3) {
						linearVelocity.scale(0.8f);
						body.setLinearVelocity(linearVelocity);
					}
					nThrust *= 0.5;
				} else if (dist < 1) {
					if (linearVelocity.length() > 2) {
						linearVelocity.scale(0.6f);
						body.setLinearVelocity(linearVelocity);
					}
					nThrust *= 0.3;
				} else if (dist < 0.5f) {
					linearVelocity.scale(0.1f);
					body.setLinearVelocity(linearVelocity);
					nThrust *= 0.1;
				} else {
					// AI slightly faster to keep up
					nThrust *= 1.01f;
				}
				Vector3f dirApplied = new Vector3f(dir);
				dirApplied.normalize();
				float speedMult = FactionManager.isNPCFaction(entity.getFactionId()) ? ((GameStateInterface) entity.getState()).getGameState().getNPCFleetSpeedLoaded() : SPEED_MULT_PLAYER;
				ThrusterElementManager.applyThrustForce(dirApplied, nThrust, body, entity, speedMult, linearVelocity, true);
			} else {
				// STOP
				if (dist < 0.05) {
					Vector3f linearVelocity = body.getLinearVelocity(new Vector3f());
					linearVelocity.scale(0.3f);
					if (linearVelocity.length() < 1) {
						linearVelocity.set(0, 0, 0);
					}
					body.setLinearVelocity(linearVelocity);
				}
			}
		}
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
