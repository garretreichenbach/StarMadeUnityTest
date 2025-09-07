package org.schema.game.client.view.gui.options;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.JoystickMappingFile;

public class GUIJoystickVirt extends GUIAnchor {

	private boolean init;

	private GUIElementList list;

	public GUIJoystickVirt(InputState state) {
		super(state);
		list = new GUIElementList(state);
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

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();


		if (JoystickMappingFile.ok()) {
			GUITextOverlay a = new GUITextOverlay(getState());
			a.setTextSimple("Below you can test the axis & buttons of the selected joystick/pad");
			list.add(new GUIListElement(a, a, getState()));

			for (int i = 0; i < JoystickMappingFile.getAxesCount(); i++) {
				final int index = i;
				GUITextOverlay axis = new GUITextOverlay(getState());
				axis.setTextSimple(new Object() {
					@Override
					public String toString() {
						return "Axis [" + index + "] '" + JoystickMappingFile.getAxisName(index) + "': " + JoystickMappingFile.getAxisValue(index);
					}
				});
				list.add(new GUIListElement(axis, axis, getState()));
			}
			int width = 150;
			int height = 24;
			int row = 3;
			int y = 0;
			for (int i = 0; i < JoystickMappingFile.getButtonCount(); i += row) {
				int x = 0;
				GUIAnchor bAnch = new GUIAnchor(getState(), 400, height + 2);
				for (int b = 0; b < row && b + i < JoystickMappingFile.getButtonCount(); b++) {
					final int index = b + i;
					GUITextButton button = new GUITextButton(getState(), width, height - 4, JoystickMappingFile.getButtonName(index), null) {

						/* (non-Javadoc)
						 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
						 */
						@Override
						public void draw() {
							if (isActive()) {
								getBackgroundColor().set(1, 0, 0, 1);
							} else {
								getBackgroundColor().set(0.3f, 0.3f, 0.6f, 0.9f);
							}
							super.draw();
						}

						/* (non-Javadoc)
						 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#isActive()
						 */
						@Override
						public boolean isActive() {
							return JoystickMappingFile.isButtonPressed(index);
						}

					};
					button.getBackgroundColorMouseOverlay().set(1, 0, 0, 1);
					button.setPos((width + 5) * x, 0, 0);
					bAnch.attach(button);
					x++;
				}
				list.add(new GUIListElement(bAnch, bAnch, getState()));

				y++;
			}

//			GUISettingSelector xaxis = new GUISettingSelector(getState(), new SettingsInterface() {
//
//				@Override
//				public void switchSetting() throws StateParameterNotFoundException {
//				}
//
//				@Override
//				public Object getCurrentState() {
//					return null;
//				}
//
//				@Override
//				public void switchSettingBack() throws StateParameterNotFoundException {
//				}
//
//				@Override
//				public void onSwitchedSetting(InputState state) {
//				}
//			});

		} else {
			GUITextOverlay a = new GUITextOverlay(getState());
			a.setTextSimple("Joystick/pad cannot be found");
			list.add(new GUIListElement(a, a, getState()));
		}

		attach(list);
		init = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#getHeight()
	 */
	@Override
	public float getHeight() {
		return list.getHeight();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#getWidth()
	 */
	@Override
	public float getWidth() {
		return list.getWidth();
	}

}
