package org.schema.game.client.controller.manager.ingame.navigation;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class NavigationControllerManager extends AbstractControlManager {

	private NavigationFilter filter = new NavigationFilter() {

		/**
		 * @return the filter
		 */
		@Override
		public long getFilter() {
			return EngineSettings.E_NAVIGATION_FILTER.getLong();
		}

		/**
		 * @param filter the filter to set
		 */
		@Override
		public void setFilter(long filter) {
			EngineSettings.E_NAVIGATION_FILTER.setLong(filter);
		}
	};

	public NavigationControllerManager(GameClientState state) {
		super(state);
	}

	private static final Vector3i tmpParam = new Vector3i();

	public static boolean isPlayerInCore(SimpleTransformableSendableObject s) {
		if (s instanceof PlayerCharacter) {
			for (ControllerStateUnit unit : ((PlayerCharacter) s).getOwnerState().getControllerState().getUnits()) {
				if (unit.playerControllable instanceof SegmentController && Ship.core.equals(unit.getParameter(tmpParam))) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isPlayerInJammedShip(SimpleTransformableSendableObject s) {
		if (s instanceof PlayerCharacter) {
			for (ControllerStateUnit unit : ((PlayerCharacter) s).getOwnerState().getControllerState().getUnits()) {
				if (unit.playerControllable instanceof Ship && ((Ship) unit.playerControllable).isJammingFor(((GameClientState) s.getState()).getCurrentPlayerObject())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the filter
	 */
	public NavigationFilter getFilter() {
		return filter;
	}

	public void setFilter(NavigationFilter filter) {
		this.filter = filter;
	}

	@Override
	public boolean isOccluded() {
		return !getState().getController().getPlayerInputs().isEmpty();
	}

	public NavigationFilter getFilterClone() {
		NavigationFilter f = new NavigationFilter(filter) {

			/**
			 * @return the filter
			 */
			@Override
			public long getFilter() {
				return ((Long) EngineSettings.E_NAVIGATION_FILTER.getLong());
			}

			/**
			 * @param filter the filter to set
			 */
			@Override
			public void setFilter(long filter) {
				EngineSettings.E_NAVIGATION_FILTER.setLong(filter);
			}
		};
		return f;
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
		long l = EngineSettings.E_NAVIGATION_FILTER.getLong();
		if (l < 0) {
			// default setting
			EngineSettings.E_NAVIGATION_FILTER.setLong(NavigationFilter.POW_SHOP | NavigationFilter.POW_SPACESTATION | NavigationFilter.POW_SHIP | NavigationFilter.POW_PLAYER | NavigationFilter.POW_DOCKED | NavigationFilter.POW_PLANET_CORE);
		}
		if (active) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(151);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(150);
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

	public boolean isDisplayed(SimpleTransformableSendableObject<?> s) {
		return filter.isDisplayed(s) && isVisibleRadar(s);
	}

	public boolean isFiltered(SimpleTransformableSendableObject<?> s) {
		return !filter.isDisplayed(s) || !isVisibleRadar(s);
	}

	public static boolean isVisibleRadar(SimpleTransformableSendableObject<?> s) {
		boolean adminInvisible = s.isInAdminInvisibility();
		boolean jamming = s instanceof Ship && ((Ship) s).isJammingFor(((GameClientState) s.getState()).getCurrentPlayerObject());
		return !adminInvisible && !jamming && !isPlayerInCore(s) && !isPlayerInJammedShip(s);
	}
}
