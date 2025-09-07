package org.schema.game.client.view.gui.buildtools;

import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

// import org.schema.schine.graphicsengine.forms.font.FontLibrary;
// import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
public abstract class GUIBuildToolSettingCheckbox extends GUISettingsElement {

	// private GUITextOverlay settingName;
	private GUICheckBox checkBox;

	public GUIBuildToolSettingCheckbox(InputState state) {
		super(state);
		this.setMouseUpdateEnabled(true);
		// settingName = new GUITextOverlay(30, 30, FontSize.BIG_24, getState());
		// 
		// settingName.setTextSimple(name);
		// 
		// settingName.setPos(36, 0, 0);
		checkBox = new GUICheckBox(state) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GUIBuildToolSettingCheckbox.this.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(353);
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GUIBuildToolSettingCheckbox.this.deactivate();
			}

			@Override
			protected boolean isActivated() {
				return GUIBuildToolSettingCheckbox.this.isActivated();
			}
		};
		// attach(settingName);
		attach(checkBox);
	}

	public abstract void activate() throws StateParameterNotFoundException;

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#cleanUp()
	 */
	@Override
	public void cleanUp() {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#draw()
	 */
	@Override
	public void draw() {
		drawAttached();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#onInit()
	 */
	@Override
	public void onInit() {
	}

	public abstract void deactivate() throws StateParameterNotFoundException;

	public abstract boolean isActivated();
}
