package org.schema.game.common.controller.elements.dockingBeam;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
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

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Collection;

public class ActivationBeamHandler extends BeamHandler implements BeamHandlerContainer {
	private static final float TIME_BEAM_TO_HIT_ONE_PIECE_IN_SECS = 0.2f;
	private final ActivationBeamElementManager dockingBeamElementManager;
	private long lastActivate;

	public ActivationBeamHandler(SegmentController c, ActivationBeamElementManager dockingBeamElementManager) {
		super(c, null);
		this.dockingBeamElementManager = dockingBeamElementManager;
	}

	@Override
	public boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position) {
		boolean canHit = !controller.equals(getBeamShooter()) &&
				(controller instanceof Ship || controller instanceof SpaceStation || controller instanceof Planet || controller instanceof PlanetIco || controller instanceof FloatingRockManaged || controller instanceof ShopSpaceStation);
		if (!canHit) {
			cannotHitReason[0] = Lng.str("Cannot activate this block");
		}
		return canHit;
	}

	@Override
	public float getBeamTimeoutInSecs() {
		return BEAM_TIMEOUT_IN_SECS;
	}

	@Override
	public boolean affectsTargetBlock() {
		return true;
	}

	@Override
	public float getBeamToHitInSecs(BeamState beamState) {
		return TIME_BEAM_TO_HIT_ONE_PIECE_IN_SECS;
	}

	@Override
	public int onBeamHit(BeamState hittingBeam, int hits, BeamHandlerContainer<SegmentController> hittingContainer, SegmentPiece segmentPiece, Vector3f from,
	                     Vector3f to, Timer timer, Collection<Segment> updatedSegments) {

		segmentPiece.refresh();
		if (segmentPiece.getType() == Element.TYPE_NONE) {
			return 0;
		}
		ElementInformation info = ElementKeyMap.getInfo(segmentPiece.getType());
		int beamHitsForReal = 1;

		if (beamHitsForReal > 0) {
			SegmentController targetSegmentController = segmentPiece.getSegmentController();

			if (!getBeamShooter().isOnServer() && segmentPiece.getType() == ElementKeyMap.SHIPYARD_CORE_POSITION) {
				SegmentPiece ownCore = getBeamShooter().getSegmentBuffer().getPointUnsave(Ship.core);
				if(ownCore != null & ownCore.getType() == ElementKeyMap.CORE_ID && !segmentPiece.getSegmentController().isVirtualBlueprint()){
					getBeamShooter().railController.connectClient(ownCore, segmentPiece);
				}
			}else if (info.getBlockStyle() == BlockStyle.NORMAL24) {

			} else {

				boolean activate = hittingBeam.beamButton.contains(KeyboardMappings.SHIP_PRIMARY_FIRE);

				//we are hitting something else
				if (info.canActivate()) {
					if (!getBeamShooter().isOnServer() &&
							((GameClientState) getBeamShooter().getState()).getController()
									.allowedToActivate(segmentPiece)) {

						((GameClientState) getBeamShooter().getState()).getController()
								.popupInfoTextMessage(Lng.str("Left click to activate/open.\nRight click to deactivate/close."), 0);
						if (activate != segmentPiece.isActive()) {
							if (getState().getUpdateTime() - this.lastActivate > 10) {

								if (segmentPiece.getType() == ElementKeyMap.LIGHT_ID ||
										segmentPiece.getType() == ElementKeyMap.LIGHT_BLUE ||
										segmentPiece.getType() == ElementKeyMap.LIGHT_GREEN ||
										segmentPiece.getType() == ElementKeyMap.LIGHT_RED ||
										segmentPiece.getType() == ElementKeyMap.LIGHT_YELLOW ||
												ElementKeyMap.isBeacon(segmentPiece.getType()) ||
										(ElementKeyMap.isValidType(segmentPiece.getType()) && ElementKeyMap.getInfo(segmentPiece.getType()).isDoor()) ||
										(ElementKeyMap.isValidType(segmentPiece.getType()) && ElementKeyMap.getInfo(segmentPiece.getType()).isSignal())
										) {

									if (segmentPiece.getSegment().getSegmentController().mayActivateOnThis(getBeamShooter(), segmentPiece)) {

										segmentPiece.getSegment().getSegmentController().sendBlockActivation(ElementCollection.getEncodeActivation(segmentPiece, true, activate, false));
										this.lastActivate = System.currentTimeMillis();
									} else {
										((GameClientState) getBeamShooter().getState()).getController()
												.popupAlertTextMessage(Lng.str("Cannot activate block!\nAccess denied by Faction!"), 0);
									}
								}
							}
						}
					}
				} else {
					if (getBeamShooter().isClientOwnObject()) {
						((GameClientState) getBeamShooter().getState()).getController()
								.popupAlertTextMessage(Lng.str("Cannot activate this block! Use on activatable blocks like doors or shipyard ancors!"), 0);
					}
				}

			}

		}

		return beamHitsForReal;
	}

	@Override
	protected boolean onBeamHitNonCube(BeamState con, int hits,
	                                BeamHandlerContainer<SegmentController> owner, Vector3f from,
	                                Vector3f to, CubeRayCastResult cubeResult, Timer timer,
	                                Collection<Segment> updatedSegments) {
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
	public ActivationBeamElementManager getDockingBeamElementManager() {
		return dockingBeamElementManager;
	}

	@Override
	public ActivationBeamHandler getHandler() {
		return this;
	}

	@Override
	public StateInterface getState() {
		return dockingBeamElementManager.getSegmentController().getState();
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
		return dockingBeamElementManager.getSegmentController().getFactionId();
	}

	@Override
	public String getName() {
		return "Docking Beam";
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
		return dockingBeamElementManager.getSegmentController().getOwnerState();
	}

	

	

}
