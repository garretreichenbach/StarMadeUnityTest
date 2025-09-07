package org.schema.game.common.controller.elements.railbeam;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.rails.DockingFailReason;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.BeamHandler;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Collection;

public class RailBeamHandler extends BeamHandler implements BeamHandlerContainer<SegmentController> {

	private static final float TIME_BEAM_TO_HIT_ONE_PIECE_IN_SECS = 0.2f;

	private final RailBeamElementManager railBeamElementManager;

	public RailBeamHandler(SegmentController c, RailBeamElementManager dockingBeamElementManager) {
		super(c, null);
		this.railBeamElementManager = dockingBeamElementManager;
	}

	@Override
	public boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position) {
		boolean canHit = !controller.equals(getBeamShooter()) && (controller instanceof Ship || controller instanceof SpaceStation || controller instanceof Planet || controller instanceof PlanetIco || controller instanceof ShopSpaceStation);
		if (!canHit) {
			cannotHitReason[0] = Lng.str("Must aim at docking module");
		}
		return canHit;
	}

	@Override
	public float getBeamTimeoutInSecs() {
		return BEAM_TIMEOUT_IN_SECS;
	}

	@Override
	public float getBeamToHitInSecs(BeamState beamState) {
		return TIME_BEAM_TO_HIT_ONE_PIECE_IN_SECS;
	}

	@Override
	public int onBeamHit(BeamState hittingBeam, int hits, BeamHandlerContainer<SegmentController> hittingContainer, SegmentPiece hitP, Vector3f from, Vector3f to, Timer timer, Collection<Segment> updatedSegments) {
		final SegmentPiece hitPiece = new SegmentPiece(hitP);
		hitPiece.refresh();
		if (hitPiece.getType() == Element.TYPE_NONE) {
			return 0;
		}
		ElementInformation info = ElementKeyMap.getInfo(hitPiece.getType());
		int beamHitsForReal = 1;
		if (beamHitsForReal > 0) {
			SegmentController targetSegmentController = hitPiece.getSegmentController();
			if (getBeamShooter().isClientOwnObject()) {
				final SegmentPiece fromPiece = getBeamShooter().getSegmentBuffer().getPointUnsave(hittingBeam.identifyerSig);
				if (fromPiece != null && fromPiece.getType() == ElementKeyMap.RAIL_BLOCK_DOCKER) {
					if (info.isRailTrack() || info.isRailTurret() || info.isRailShipyardCore()) {
						System.err.println("[CLIENT][RAIL][BEAM] HITTING RAIL " + fromPiece + " -> " + hitPiece);
						// SegmentPiece fromPiece = getBeamShooter().getSegmentBuffer().getPointUnsave(11,8,11, false);
						if (!hitPiece.getSegmentController().isVirtualBlueprint()) {
							if (!fromPiece.getSegmentController().railController.isInAnyRailRelationWith(hitPiece.getSegmentController())) {
								DockingFailReason r = new DockingFailReason();
								if (getBeamShooter().railController.isOkToDockClientCheck(fromPiece, hitPiece, r)) {
									getBeamShooter().railController.connectClient(fromPiece, hitPiece);
								} else {
									r.popupClient(getBeamShooter());
								}
							}
						} else {
							PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), Lng.str("Dock on Design?"), Lng.str("Do you really want to dock on a design?\nThe blocks on this ship will be lost.")) {

								@Override
								public void onDeactivate() {
								}

								@Override
								public boolean isOccluded() {
									return false;
								}

								@Override
								public void pressedOK() {
									DockingFailReason r = new DockingFailReason();
									if (getBeamShooter().railController.isOkToDockClientCheck(fromPiece, hitPiece, r)) {
										getBeamShooter().railController.connectClient(fromPiece, hitPiece);
									} else {
										r.popupClient(getBeamShooter());
									}
									deactivate();
								}
							};
							c.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(915);
						}
					}
				}
			}
		}
		return beamHitsForReal;
	}

	@Override
	protected boolean onBeamHitNonCube(BeamState con, int hits, BeamHandlerContainer<SegmentController> owner, Vector3f from, Vector3f to, CubeRayCastResult cubeResult, Timer timer, Collection<Segment> updatedSegments) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.AbstractBeamHandler#ignoreNonPhysical()
	 */
	@Override
	protected boolean ignoreNonPhysical(BeamState con) {
		return con.beamButton.contains(KeyboardMappings.SHIP_PRIMARY_FIRE);
	}

	@Override
	public Vector4f getDefaultColor(BeamState beamState) {
		Vector4f clr = getColorRange(BeamColors.WHITE);
		return clr;
	}

	/**
	 * @return the dockingBeamElementManager
	 */
	public RailBeamElementManager getDockingBeamElementManager() {
		return railBeamElementManager;
	}

	@Override
	public RailBeamHandler getHandler() {
		return this;
	}

	@Override
	public StateInterface getState() {
		return railBeamElementManager.getSegmentController().getState();
	}

	@Override
	public void sendHitConfirm(byte damageType) {
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
		return true;
	}

	@Override
	public int getFactionId() {
		return railBeamElementManager.getSegmentController().getFactionId();
	}

	@Override
	public String getName() {
		return Lng.str("Rail Beam");
	}

	@Override
	public boolean ignoreBlock(short type) {
		ElementInformation f = ElementKeyMap.getInfoFast(type);
		return f.isDrawnOnlyInBuildMode() && !f.hasLod();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return railBeamElementManager.getSegmentController().getOwnerState();
	}
}
