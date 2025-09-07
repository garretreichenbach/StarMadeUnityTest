package org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew;

import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.IconDatabase;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUICheckBoxTextPairNew extends GUIElement {

	private boolean init;

	private Object text;

	private FontInterface font;

	public int distanceAfterText = 3;

	boolean checkboxBeforeText = true;

	private int width;

	private int height;

	public GUICheckBoxTextPairNew(InputState state, Object text, FontInterface font) {
		super(state);
		this.font = font;
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		Sprite icons16 = IconDatabase.getIcons16(getState());
		this.width = l.getMaxLineWidth() + distanceAfterText + icons16.getWidth();
		height = Math.max(l.getTextHeight(), icons16.getHeight());
		checkBox.setPos(checkboxBeforeText ? 0 : (l.getMaxLineWidth() + distanceAfterText), (int) (height * 0.5f - icons16.getHeight() * 0.5f), 0);
		l.setPos(checkboxBeforeText ? (icons16.getWidth() + distanceAfterText) : 0, (int) (height * 0.5f - l.getTextHeight() * 0.5f), 0);
		drawAttached();
	}

	private GUICheckBox checkBox;

	private GUITextOverlay l;

	public GUIActiveInterface activeInterface;

	public boolean isInsideCheckbox() {
		return checkBox.isInside();
	}

	@Override
	public boolean isActive() {
		return super.isActive() && (activeInterface == null || activeInterface.isActive());
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		if (init) {
			return;
		}
		l = new GUITextOverlay(font, getState());
		l.setTextSimple(text);
		attach(l);
		checkBox = new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GUICheckBoxTextPairNew.this.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(30);
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GUICheckBoxTextPairNew.this.deactivate();
			}

			@Override
			protected boolean isActivated() {
				return GUICheckBoxTextPairNew.this.isChecked();
			}
		};
		checkBox.activeInterface = GUICheckBoxTextPairNew.this::isActive;
		attach(checkBox);
		init = true;
	}

	public abstract void activate();

	public abstract void deactivate();

	public abstract boolean isChecked();

	@Override
	public void cleanUp() {
		if (checkBox != null) {
			checkBox.cleanUp();
		}
		if (l != null) {
			l.cleanUp();
		}
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}
}
