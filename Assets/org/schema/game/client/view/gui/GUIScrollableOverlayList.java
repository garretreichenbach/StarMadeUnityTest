package org.schema.game.client.view.gui;

import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITile;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITileButtonDesc;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITilePane;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class GUIScrollableOverlayList extends GUIElement {

	private final GUIAnchor depend;
	private final int tileWidth;
	private final int tileHeight;
	private final GUIScrollablePanel scrollPanel;
	private boolean initialized;

	private GUITilePane<GUIOverlay> tilePane;
	private final ArrayList<GUIOverlay> overlays = new ArrayList<>();
	private GUIOverlay selected;

	public GUIScrollableOverlayList(InputState state, GUIAnchor depend, int scroll, int tileWidth, int tileHeight) {
		super(state);
		this.depend = depend;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		scrollPanel = new GUIScrollablePanel(getWidth(), getHeight(), depend, getState());
		scrollPanel.setScrollable(scroll);
		attach(scrollPanel);
	}

	@Override
	public void onInit() {
		tilePane = new GUITilePane<>(getState(), depend, tileWidth, tileHeight);
		tilePane.onInit();
		scrollPanel.setContent(tilePane);
		scrollPanel.onInit();
		initialized = true;
	}

	@Override
	public void draw() {
		if(!initialized) onInit();
		if(selected == null && !overlays.isEmpty()) setSelected(overlays.get(0));
		scrollPanel.draw();
	}

	@Override
	public void cleanUp() {
		if(initialized) {
			tilePane.cleanUp();
			scrollPanel.cleanUp();
			initialized = false;
		}
	}

	@Override
	public float getWidth() {
		if(depend.getWidth() <= 0) return 100.0f;
		else return depend.getWidth();
	}

	@Override
	public float getHeight() {
		if(depend.getHeight() <= 0) return 300.0f;
		else return depend.getHeight();
	}

	public void addOverlay(GUIOverlay overlay, String title, GUICallback callback) {
		GUITile tile = tilePane.addButtonTile(title, "", GUIHorizontalArea.HButtonColor.BLUE, callback, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		tile.getContent().attach(overlay);
		((GUITileButtonDesc<?>) tile).button.setUserPointer(overlay.getUserPointer());
		overlay.setPos(tileWidth / 2.0f, tileHeight / 2.0f + 10.0f, 0);
		overlays.add(overlay);
	}

	public void setColor(Vector4f color) {
		for(GUIOverlay overlay : overlays) overlay.getSprite().setTint(color);
	}

	public void setSelected(GUIOverlay selected) {
		this.selected = selected;
//		for(GUITile tile : tilePane.getTiles()) ((GUITileButtonDesc<?>) tile).button.setColor(GUIHorizontalArea.HButtonColor.BLUE);
//		((GUITileButtonDesc<?>) tilePane.getTiles().get(overlays.indexOf(selected))).button.setColor(GUIHorizontalArea.HButtonColor.GREEN);
	}
}