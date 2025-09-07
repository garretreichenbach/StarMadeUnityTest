package org.schema.schine.sound.controller;

public enum AudioTags implements AudioTag{
	GUI, OK, CANCEL, POPUP, CLOSE, SWITCH, MOUSE_OVER, SELECT, DESELECT, EXPAND, DELETE, UNEXPAND, WARNING, INFO, ERROR,
	BUTTON, PRESS, CHECKBOX, SHIP, WEAPON, MISSLE, CANNON, BEAM, MINE, SALVAGE,
	EXPLOSION, MAXIMIZE, MINIMIZE, DIALOG, ACTIVATE, GAME, ALERT, TIP, FLASHING, CHAT_FACTION, CHAT_PRIVATE,
	CHAT_PRIVATE_SEND, CHAT_PUBLIC, HOVER, BUY, SELL, DEACTIVATE, SHOP, BLOCK, BUILD, REMOVE, ADD, TITLE_POPUP,
	DROP_CREDITS, DOCKING, DOCK, HUD, MISSILE, FIRE, TRACTOR, REPAIR, DAMAGE, MINE_LAYER, RAIL, HIT, HULL, SHIELD,
	AMBIENCE, MAIN_REACTOR, THRUSTER, FACTORY, DOOR, TRANSPORTER, UNDO, REDO, CONNECT,;

	@Override
	public AudioTag getParent() {
		return null;
	}

	@Override
	public String getTagName() {
		return name();
	}


	@Override
	public String toString() {
		return name();
	}

	@Override
	public short getTagId() {
		return (short)ordinal();
	}
}
