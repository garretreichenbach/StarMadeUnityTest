package org.schema.game.client.controller.manager.ingame.map;

import api.common.GameClient;
import org.lwjgl.glfw.GLFW;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.gamemap.entry.SelectableMapEntry;
import org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gamemap.GameMapPosition;
import org.schema.game.client.view.gamemap.StarPosition;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.util.HashSet;

public class MapControllerManager extends AbstractControlManager {

	public static final HashSet<SelectableMapEntry> selected = new HashSet<>();
	public static final HashSet<FleetMember.FleetMemberMapIndication> selectedFleets = new HashSet<>();

	private long lastClick;
	private MapEntryOptionsMenu lastDialog;

	public MapControllerManager(GameClientState state) {
		super(state);
	}

	public static void resetFleets() {
		for(FleetMember.FleetMemberMapIndication f : selectedFleets) f.s = false;
		selectedFleets.clear();
	}

	public PlayerInteractionControlManager getInteractionManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		if(e.getKey() == GLFW.GLFW_KEY_F12) getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
		boolean doubleClick = System.currentTimeMillis() - lastClick < 300;
		if(lastDialog != null && lastDialog.isActive() && lastDialog.getInputPanel() != null) lastDialog.deactivate();
		boolean hasEntry = false;
		if(!selected.isEmpty()) {
			for(SelectableMapEntry m : selected) {
				if(doubleClick) {
					if(e.isTriggered(KeyboardMappings.MAP_SELECT_ITEM)) {
						createMapOptions(m);
						hasEntry = true;
						break;
					} else if(e.isTriggered(KeyboardMappings.MAP_NAVIGATE_TO)) {
						navigateTo(m);
						hasEntry = true;
						break;
					}
				}
			}
		}
		if(!hasEntry) {
			for(SavedCoordinate coordinate : getState().getController().getClientGameData().getSavedCoordinates()) {
				if(coordinate.getSector().equals(getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().get(new Vector3i()))) {
					if(doubleClick && e.isTriggered(KeyboardMappings.MAP_SELECT_ITEM)) {
						createMapOptions(coordinate);
						hasEntry = true;
					}
				}
			}
		}
		if(!hasEntry && doubleClick && e.isTriggered(KeyboardMappings.MAP_SELECT_ITEM)) createMapOptions(null);
		lastClick = System.currentTimeMillis();
		getState().getWorldDrawer().getGameMapDrawer().handleKeyEvent(e);
	}

	public Vector3i getEntryPos(SelectableMapEntry entry) {
		Vector3i pos = new Vector3i();
		getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().get(pos);
		switch(entry) {
			case SavedCoordinate t -> pos.set(t.getSector());
			case TransformableEntityMapEntry t -> {
				pos.x = (int) ((t.pos.x / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
				pos.y = (int) ((t.pos.y / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
				pos.z = (int) ((t.pos.z / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
			}
			case FleetMember.FleetMemberMapIndication t -> pos.set(t.getSector());
			case StarPosition t -> {
				pos.x = (int) ((t.pos.x / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
				pos.y = (int) ((t.pos.y / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
				pos.z = (int) ((t.pos.z / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
			}
			case null, default -> {}
		}

		if(!getState().getPlayer().getCurrentSystem().equals(getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos())) {
			//We are in the correct local pos, but not the correct system pos, we need to adjust
			Vector3i mapSystemPos = new Vector3i(getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos());
			Vector3i playerSystemPos = new Vector3i(getState().getPlayer().getCurrentSystem());
			Vector3i diff = new Vector3i(mapSystemPos);
			diff.sub(playerSystemPos);
			diff.scale(VoidSystem.SYSTEM_SIZE);
			pos.add(diff);
		}
		return pos;
	}

	public void navigateTo(SelectableMapEntry entry) {
		GameMapPosition pos = getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition();
		pos.set(getEntryPos(entry), false);
	}

	public void navigateTo(Vector3i sector) {
		getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set(sector.x, sector.y, sector.z, false);
	}

	private void createMapOptions(SelectableMapEntry entry) {
		String name;
		switch(entry) {
			case SavedCoordinate t -> name = t.name;
			case TransformableEntityMapEntry t -> name = t.name;
			case FleetMember.FleetMemberMapIndication t -> name = t.getName();
			case StarPosition t -> name = t.getName();
			case null, default -> {
				Vector3i pos = getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().get(new Vector3i());
				name = "Sector (" + pos.x + "." + pos.y + "." + pos.z + ")";
			}
		}

		if(name != null) {
			if(lastDialog != null && lastDialog.isActive()) lastDialog.deactivate();
			(lastDialog = new MapEntryOptionsMenu(entry, name)).activate();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		// INSERTED CODE
		resetFleets();
		Vector3i sector = GameClient.getClientPlayerState().getCurrentSector();
		if(sector.x >= 100000000 || sector.y >= 100000000 || sector.z >= 100000000) active = false; //Don't draw if outside universe to prevent visual glitches
		//
		//		System.err.println("MAP CONTROL MANAGER ACTIVE: "+active);
		if(active) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(147);
			getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(146);
			if(lastDialog != null && lastDialog.isActive()) lastDialog.deactivate();
		}
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
		if(active) getState().getWorldDrawer().getGameMapDrawer().resetToCurrentSector();
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(Mouse.isSecondaryMouseDownUtility());
		getInteractionManager().suspend(true);
	}
}
