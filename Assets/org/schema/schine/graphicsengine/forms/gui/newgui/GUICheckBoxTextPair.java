package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUICheckBoxTextPair extends GUIAnchor {

	private int textWidth;

	private boolean init;

	private Object text;

	private FontInterface font;

	public GUICheckBoxTextPair(InputState state, Object text, int textWidth, int height) {
		this(state, text, textWidth, FontSize.SMALL_14, height);
	}

	public GUICheckBoxTextPair(InputState state, Object text, int textWidth, FontInterface font, int height) {
		super(state);
		this.font = font;
		this.textWidth = textWidth;
		this.text = text;
		this.height = height;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		super.draw();
	}

	public int textPosY = 2;

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		this.width = textWidth + Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-").getWidth();
		GUITextOverlay l = new GUITextOverlay(font, getState());
		l.setTextSimple(text);
		l.setPos(0, textPosY, 0);
		attach(l);
		GUICheckBox checkBox = new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GUICheckBoxTextPair.this.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(17);
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GUICheckBoxTextPair.this.deactivate();
			}

			@Override
			protected boolean isActivated() {
				return GUICheckBoxTextPair.this.isActivated();
			}
		};
		checkBox.setPos(width - Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-").getWidth(), 0, 0);
		attach(checkBox);
		init = true;
	}

	public abstract void activate();

	public abstract void deactivate();

	public abstract boolean isActivated();
}
