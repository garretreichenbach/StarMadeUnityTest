package org.schema.game.client.controller.manager;

import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class AiConfigurationManager extends AbstractControlManager implements GUICallback {

	private boolean needsUpdate;

	private AiInterface aiInterface;

	private boolean canEdit;

	public AiConfigurationManager(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGui, MouseEvent event) {
	}

	@Override
	public boolean isOccluded() {
		return !getState().getController().getPlayerInputs().isEmpty();
	}

	public AiInterface getAi() {
		return aiInterface;
	}

	public void setAi(AiInterface aiInterface) {
		this.aiInterface = aiInterface;
		this.needsUpdate = true;
	}

	public PlayerInteractionControlManager getInteractionManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		CameraMouseState.setGrabbed(!active);
		if (active) {
			notifyObservers();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(95);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(94);
		}
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(false);
		getInteractionManager().suspend(true);
	}

	/**
	 * @return the needsUpdate
	 */
	public boolean isNeedsUpdate() {
		return needsUpdate;
	}

	/**
	 * @param needsUpdate the needsUpdate to set
	 */
	public void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}

	public boolean canEdit() {
		return canEdit;
	}

	/**
	 * @param canEdit the canEdit to set
	 */
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
}
