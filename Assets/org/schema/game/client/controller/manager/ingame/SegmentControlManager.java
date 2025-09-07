package org.schema.game.client.controller.manager.ingame;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.PositionControl;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class SegmentControlManager extends AbstractControlManager implements EditSegmentInterface {

	private SegmentExternalController segmentExternalController;

	private SegmentBuildController segmentBuildController;

	private Vector3i lastEntered;

	public SegmentControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	public void exit() {
		if (getEntered() != null) {
			final SegmentController segmentController = getEntered().getSegment().getSegmentController();
			if (EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() >= 0 && segmentController.getSpeedPercentServerLimitCurrent() >= EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() * 0.01f) {
				PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM_Exit", getState(), "Exit", "Do you really want to do that.\nThe current object was flying at " + StringTools.formatPointZero(segmentController.getSpeedCurrent()) + " speed\n\n(this message can be customized or\nturned off in the game option\n'Popup Detach Warning' in the ingame options menu)") {

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						if (getEntered() != null) {
							Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
							System.err.println("[SegmentConrolManager] EXIT SHIP FROM EXTRYPOINT " + absolutePos);
							getState().getController().requestControlChange((PlayerControllable) segmentController, getState().getCharacter(), absolutePos, new Vector3i(), true);
							lastEntered = getEntered().getAbsolutePos(new Vector3i());
							setEntered(null);
						}
						deactivate();
					}
				};
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(178);
			} else {
				Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
				System.err.println("[SegmentConrolManager] EXIT SHIP FROM EXTRYPOINT " + absolutePos);
				getState().getController().requestControlChange((PlayerControllable) segmentController, getState().getCharacter(), absolutePos, new Vector3i(), true);
				lastEntered = getEntered().getAbsolutePos(new Vector3i());
				setEntered(null);
				setActive(false);
			}
		}
	}

	@Override
	public Vector3i getCore() {
		return getEntered() != null ? getEntered().getAbsolutePos(new Vector3i()) : (lastEntered != null ? lastEntered : new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
	}

	/**
	 * @return the entered
	 */
	@Override
	public SegmentPiece getEntered() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getEntered();
	}

	public void setEntered(SegmentPiece entered) {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().setEntered(entered);
	}

	@Override
	public EditableSendableSegmentController getSegmentController() {
		if (getEntered() == null) {
			return null;
		}
		return (EditableSendableSegmentController) getEntered().getSegment().getSegmentController();
	}

	/**
	 * @return the segmentBuildController
	 */
	public SegmentBuildController getSegmentBuildController() {
		return segmentBuildController;
	}

	/**
	 * @return the segmentExternalController
	 */
	public SegmentExternalController getSegmentExternalController() {
		return segmentExternalController;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		if (e.isTriggered(KeyboardMappings.ENTER_SHIP)) {
			if (e.isTriggered(KeyboardMappings.ENTER_SHIP) && e.isTriggered(KeyboardMappings.ACTIVATE) && this.isActive()) {
				SegmentPiece cp = segmentBuildController.getCurrentSegmentPiece();
				if (cp != null && ElementKeyMap.isValidType(cp.getType()) && ElementKeyMap.getInfo(cp.getType()).canActivate() && !ElementKeyMap.getInfo(cp.getType()).isEnterable()) {
					System.err.println("[CLIENT] CHECKING ACTIVATE OF " + cp + " FROM INSIDE BUILD MODE");
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().checkActivate(cp);
				} else {
					exit();
				}
			} else {
				exit();
			}
		} else if (e.isTriggered(KeyboardMappings.ACTIVATE)) {
			if (segmentBuildController.isActive()) {
				SegmentPiece cp = segmentBuildController.getCurrentSegmentPiece();
				if (cp != null) {
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().checkActivate(cp);
				}
			}
		} else if (e.isTriggered(KeyboardMappings.REBOOT_SYSTEMS)) {
			if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SpaceStation) {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().popupShipRebootDialog((SegmentController) getState().getCurrentPlayerObject());
			}
		}
	// if(KeyboardMappings.getEventKeyState(e, getState())){
	// if(KeyboardMappings.getEventKey(e) == KeyboardMappings.ENTER_SHIP.getMapping()){
	// exit();
	// }
	// }
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch()
	 */
	@Override
	public void onSwitch(boolean active) {
		// 
		// try{
		// throw new NullPointerException("INSHIP: "+active+" / "+isActive());
		// }catch(NullPointerException e){
		// e.printStackTrace();
		// }
		if (active) {
			assert (getEntered() != null);
			if (getEntered() == null || getEntered().getSegment() == null) {
				System.err.println("Exception: entered has been null");
				return;
			}
			SegmentController s = getEntered().getSegment().getSegmentController();
			// getState().setShip(s);
			PositionControl controlledElements = s.getControlElementMap().getControlledElements(Element.TYPE_ALL, getEntered().getAbsolutePos(new Vector3i()));
			// getState().getPlayer().getPlayerKeyConfigurationManager().addDefaultConfigIfNotExistsClient(s, getEntered());
			if (getEntered().getType() == ElementKeyMap.BUILD_BLOCK_ID) {
				segmentBuildController.setActive(true);
			} else {
				segmentExternalController.setActive(true);
			}
			SegmentController segmentController = getEntered().getSegment().getSegmentController();
			Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
		// System.err.println("ENTER ON ENTRYPOINT: "+absolutePos);
		} else {
			// getState().getPlayer().requestControlRelease(getState().getShip(), getEntered().getAbsolutePos(new Vector3i()), false );
			System.err.println("[INSHIP] EXIT setting ship to null. " + getState());
			if (getEntered() != null && getEntered().getSegment().getSegmentController() == getState().getCurrentPlayerObject()) {
				getState().setCurrentPlayerObject(null);
			}
			segmentExternalController.setActive(false);
			segmentBuildController.setActive(false);
		}
		assert (!active || getEntered() != null) : ": Entered: " + getEntered().getSegment().getSegmentController() + " -> " + getEntered().getAbsolutePos(new Vector3i());
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		if (getEntered() != null && !getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(getEntered().getSegment().getSegmentController().getId())) {
			System.err.println("[CLIENT][SegmentControlManager] Entered object no longer exists");
			exit();
		}
		if (getEntered() != null) {
			getEntered().refresh();
			if (getEntered().getType() == Element.TYPE_NONE) {
				Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
				// autorequest true previously
				SegmentPiece pointUnsave = getEntered().getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(absolutePos);
				if (pointUnsave != null) {
					if (pointUnsave.getType() == Element.TYPE_NONE) {
						exit();
					} else {
						setEntered(pointUnsave);
					}
				}
			}
			if (getEntered() != null && !getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(getEntered().getSegment().getSegmentController().getId())) {
				exit();
			}
		}
	}

	public void initialize() {
		segmentExternalController = new SegmentExternalController(getState());
		segmentBuildController = new SegmentBuildController(getState(), this);
		getControlManagers().add(segmentExternalController);
		getControlManagers().add(segmentBuildController);
	}
}
