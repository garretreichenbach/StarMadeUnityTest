package org.schema.game.client.controller;

import java.util.Observable;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.inventory.Inventory;

public class GUIController extends Observable {

	public static final int GUI_MODE_INGAME = 0;
	public static final int GUI_MODE_INVENTORY = 1;
	public static final int GUI_MODE_SHOP = 2;

	private int guiMode = GUI_MODE_INGAME;

	private GameClientState state;

	public GUIController(GameClientState state) {
		super();
		this.state = state;
	}

	public int getGUIMode() {
		return guiMode;
	}

	public void setGUIMode(int gUIMode) {
		guiMode = gUIMode;
	}

	public GameClientState getState() {
		return state;
	}

	public void setState(GameClientState state) {
		this.state = state;
	}

	public void update() {
		Inventory inventory = state.getPlayer().getInventory(null);

	}

}
