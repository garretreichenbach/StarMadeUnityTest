package org.schema.game.client.controller.manager.ingame.catalog;

import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public class CatalogControlManager extends AbstractControlManager implements GUICallback {

	private PersonalCatalogControlManager personalCatalogControlManager;

	private AccessibleCatalogControlManager accessibleCatalogControlManager;

	private AdminCatalogControlManager adminCatalogControlManager;

	public CatalogControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	@Override
	public void callback(GUIElement callingGui, MouseEvent event) {
	}

	/**
	 * @return the accessibleCatalogControlManager
	 */
	public AccessibleCatalogControlManager getAccessibleCatalogControlManager() {
		return accessibleCatalogControlManager;
	}

	/**
	 * @return the adminCatalogControlManager
	 */
	public AdminCatalogControlManager getAdminCatalogControlManager() {
		return adminCatalogControlManager;
	}

	public Faction getOwnFaction() {
		int factionId = getState().getPlayer().getFactionId();
		return getState().getFactionManager().getFaction(factionId);
	}

	/**
	 * @return the personalCatalogControlManager
	 */
	public PersonalCatalogControlManager getPersonalCatalogControlManager() {
		return personalCatalogControlManager;
	}

	private void initialize() {
		personalCatalogControlManager = new PersonalCatalogControlManager(getState());
		accessibleCatalogControlManager = new AccessibleCatalogControlManager(getState());
		adminCatalogControlManager = new AdminCatalogControlManager(getState());
		getControlManagers().add(personalCatalogControlManager);
		getControlManagers().add(accessibleCatalogControlManager);
		getControlManagers().add(adminCatalogControlManager);
		personalCatalogControlManager.setActive(true);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		if (active) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(105);
			notifyObservers();
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(104);
		}
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(false);
		super.update(timer);
	}

	@Override
	public boolean isOccluded() {
		return false;
	}
}
