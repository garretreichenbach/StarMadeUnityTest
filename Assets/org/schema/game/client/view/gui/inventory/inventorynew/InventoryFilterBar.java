package org.schema.game.client.view.gui.inventory.inventorynew;

import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;

public class InventoryFilterBar extends GUIActivatableTextBar {

	public InventoryFilterBar(InputState state, GUIElement dependent,
	                          TextCallback textCallback, OnInputChangedCallback onInputChangedCallback) {
		super(state, FontSize.SMALL_14, dependent, textCallback, onInputChangedCallback);
		icon = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 16px-8x8-gui-"), state);
		icon.setSpriteSubIndex(1);
		icon.onInit();
		
		setClearButtonEnabled(true);
	}

	public InventoryFilterBar(InputState state, String inactiveText, GUIElement dependent,
	                          TextCallback textCallback, OnInputChangedCallback onInputChangedCallback) {
		super(state, FontSize.SMALL_14, inactiveText, dependent, textCallback, onInputChangedCallback);
		icon = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"UI 16px-8x8-gui-"), state);
		icon.setSpriteSubIndex(1);
		icon.onInit();
		
		setClearButtonEnabled(true);
	}

}
