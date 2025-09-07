package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class WeaponAssignControllerManager extends AbstractControlManager implements GUICallback {

	public static final int LIST_CHANGED = -1;

	public static final int SELECTED_CHANGED = -2;

	private long selectedPiece;

	private boolean needsUpdate;

	public WeaponAssignControllerManager(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGui, MouseEvent event) {
		if (this.isTreeActive() && !this.isSuspended()) {
			for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
				if (e.releasedLeftMouse()) {
					if (callingGui instanceof GUIListElement) {
						GUIListElement en = (GUIListElement) callingGui;
						GUIElementList list = en.getParent();
						int index = list.indexOf(en);
						list.deselectAll();
						en.setSelected(true);
						selectedPiece = (Long) en.getContent().getUserPointer();
						selectionUpdate();
					}
				}
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return !getState().getController().getPlayerInputs().isEmpty();
	}

	public void flagUpdate() {
		this.needsUpdate = true;
	}

	public void forceUpdate() {
		notifyObservers();
	}

	public PlayerInteractionControlManager getInteractionManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	/**
	 * @return the selectedPiece
	 */
	public long getSelectedPiece() {
		return selectedPiece;
	}

	/**
	 * @param selectedPiece the selectedPiece to set
	 */
	public void setSelectedPiece(long selectedPiece) {
		this.selectedPiece = selectedPiece;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		if (e.isSlotKey()) {
			int slot = e.getSlotKey();
			numberKeyPressed(slot);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		CameraMouseState.setGrabbed(!active);
		if (active) {
			needsUpdate = true;
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(185);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(184);
		}
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
		// notify GUI
		notifyObservers();
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

	private void numberKeyPressed(int i) {
		if (selectedPiece == Long.MIN_VALUE) {
			return;
		}
		assert (getState().getShip() != null);
		SlotAssignment shipConfiguration = getState().getShip().getSlotAssignment();
		int remove = -1;
		if (shipConfiguration.hasConfigForPos(selectedPiece)) {
			remove = shipConfiguration.removeByPosAndSend(selectedPiece);
		} else {
		// System.err.println("DOESNT HAVE CONFIG FOR OLD IN THIS SLOT: "+remove+" ("+ElementCollection.getIndex(absolutePos)+")");
		}
		System.err.println("[CLIENT][SHIPKEYCONFIG] PRESSED " + i + " (key " + (i + 1) + "); WEAPON BAR: " + getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getSelectedWeaponBottomBar() + ": REMOVE: " + remove + " (" + selectedPiece + ") index: " + selectedPiece);
		i += getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getSelectedWeaponBottomBar() * 10;
		if (remove != i) {
			System.err.println("[CLIENT][SHIPKEYCONFIG] ASSINGING: " + i + " to " + selectedPiece);
			shipConfiguration.modAndSend((byte) i, selectedPiece);
		}
		notifyObservers();
	}

	public void selectionUpdate() {
		notifyObservers();
	}
}
