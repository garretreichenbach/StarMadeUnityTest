package org.schema.game.client.data;

import api.common.GameClient;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import java.util.Locale;

public enum WaypointIcons {

	PLANET(6),
	GAS_GIANT(17),
	ASTEROID(7), //Todo
	STATION(9),
	SHOP(8),
	SHIP(10),
	FACTORY(14), //Todo
	WARP_GATE(7), //Todo
	SHIPYARD(7), //Todo
	TURRET(7), //Todo
	OUTPOST(11);

	public final Sprite sprite;
	public final int index;

	WaypointIcons(int index) {
		sprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "waypoint-sprites-8x4-c-gui-");
		sprite.setSelectedMultiSprite(ordinal());
		sprite.setSelectedMultiSprite(index);
		this.index = index;
	}

	public static GUIOverlay[] getOverlays() {
		WaypointIcons[] icons = values();
		GUIOverlay[] overlays = new GUIOverlay[icons.length];
		for(int i = 0; i < icons.length; i ++) {
			overlays[i] = new GUIOverlay(icons[i].sprite, GameClient.getClientState());
			overlays[i].setUserPointer(icons[i].name());
			overlays[i].setSpriteSubIndex(i);
		}
		return overlays;
	}

	public static WaypointIcons getIcon(String pointer) {
		return valueOf(pointer.toUpperCase(Locale.ENGLISH));
	}
}
