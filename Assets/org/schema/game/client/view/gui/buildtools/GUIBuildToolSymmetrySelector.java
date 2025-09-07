package org.schema.game.client.view.gui.buildtools;

import java.util.ArrayList;

import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SymmetryPlanes;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIBuildToolSymmetrySelector extends GUISettingsElement {

	private final int mode;

	private GUITextOverlay settingName;

	private GUITextButton button;

	private boolean init;

	public GUIBuildToolSymmetrySelector(InputState state, int mode) {
		super(state);
		this.setMouseUpdateEnabled(true);
		this.mode = mode;
		settingName = new GUITextOverlay(FontSize.BIG_24, getState());
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		super.drawAttached();
	// GlUtil.glPushMatrix();
	// transform();
	// settingName.draw();
	// button.draw();
	// GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		settingName.setText(new ArrayList());
		switch(mode) {
			case (SymmetryPlanes.MODE_XY) -> settingName.getText().add(Lng.str("XY-Plane"));
			case (SymmetryPlanes.MODE_XZ) -> settingName.getText().add(Lng.str("XZ-Plane"));
			case (SymmetryPlanes.MODE_YZ) -> settingName.getText().add(Lng.str("YZ-Plane"));
		}
		settingName.onInit();
		button = new GUITextButton(getState(), 100, 20, new Object() {

			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				if (getSymmetryPlanes().getPlaceMode() == mode) {
					return Lng.str("*click on block*");
				} else {
					switch(mode) {
						case (SymmetryPlanes.MODE_XY):
							if (getSymmetryPlanes().isXyPlaneEnabled()) {
								return Lng.str("unset");
							} else {
								return Lng.str("set");
							}
						case (SymmetryPlanes.MODE_XZ):
							if (getSymmetryPlanes().isXzPlaneEnabled()) {
								return Lng.str("unset");
							} else {
								return Lng.str("set");
							}
						case (SymmetryPlanes.MODE_YZ):
							if (getSymmetryPlanes().isYzPlaneEnabled()) {
								return Lng.str("unset");
							} else {
								return Lng.str("set");
							}
					}
					return Lng.str("error");
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(354);
					if (getSymmetryPlanes().getPlaceMode() == 0) {
						switch(mode) {
							case (SymmetryPlanes.MODE_XY):
								if (getSymmetryPlanes().isXyPlaneEnabled()) {
									getSymmetryPlanes().setXyPlaneEnabled(false);
								} else {
									getSymmetryPlanes().setPlaceMode(mode);
								}
								break;
							case (SymmetryPlanes.MODE_XZ):
								if (getSymmetryPlanes().isXzPlaneEnabled()) {
									getSymmetryPlanes().setXzPlaneEnabled(false);
								} else {
									getSymmetryPlanes().setPlaceMode(mode);
								}
								break;
							case (SymmetryPlanes.MODE_YZ):
								if (getSymmetryPlanes().isYzPlaneEnabled()) {
									getSymmetryPlanes().setYzPlaneEnabled(false);
								} else {
									getSymmetryPlanes().setPlaceMode(mode);
								}
								break;
						}
					} else {
						getSymmetryPlanes().setPlaceMode(0);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		button.getPos().x = 40;
		attach(button);
		init = true;
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		return settingName.getHeight();
	}

	@Override
	public float getWidth() {
		return 100;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public SymmetryPlanes getSymmetryPlanes() {
		PlayerInteractionControlManager pp = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		if (pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive()) {
			return pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().getSymmetryPlanes();
		} else {
			return pp.getSegmentControlManager().getSegmentBuildController().getSymmetryPlanes();
		}
	}
}
