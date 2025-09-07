package org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.SettingsInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.network.client.ClientState;

public class GUISettingSelectorNew extends GUISettingsElement implements GUICallback {

	private GUITextOverlay settingName;
	private GUIOverlay leftArrow;
	private GUIOverlay rightArrow;

	private boolean checked = false;
	private SettingsInterface setting;
	private boolean init;

	public GUISettingSelectorNew(ClientState state, int width, int heigth, SettingsInterface settings) {
		super(state);
		this.setMouseUpdateEnabled(true);
		this.setCallback(this);
		this.setting = settings;
		leftArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"tools-16x16-gui-"), getState());
		rightArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath()+"tools-16x16-gui-"), getState());
		settingName = new GUITextOverlay(FontSize.SMALL_14, getState());
	}

	public GUISettingSelectorNew(ClientState state, SettingsInterface settings) {
		this(state, UIScale.getUIScale().scale(140), UIScale.getUIScale().scale(30), settings);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			checked = !checked;
			setting.next();

		}
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();

		settingName.draw();
		leftArrow.draw();
		rightArrow.draw();
		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {

		settingName.setTextSimple(setting.name());
		settingName.onInit();

		settingName.getPos().y += UIScale.getUIScale().scale(8);

		leftArrow.setMouseUpdateEnabled(true);
		rightArrow.setMouseUpdateEnabled(true);
		leftArrow.getPos().y += UIScale.getUIScale().scale(3);
		rightArrow.getPos().y += UIScale.getUIScale().scale(3);

		leftArrow.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {

				if (event.pressedLeftMouse()) {
					checked = !checked;
					setting.previous();
					updateText();
				}

			}

			@Override
			public boolean isOccluded() {
				return false;
			}

		});
		rightArrow.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					checked = !checked;
					setting.next();
					updateText();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});

		leftArrow.setSpriteSubIndex(21);
		rightArrow.setSpriteSubIndex(20);
		settingName.getPos().x = leftArrow.getWidth();
		rightArrow.getPos().x = leftArrow.getWidth() + settingName.getWidth();

		init = true;
	}

	@Override
	protected void doOrientation() {
		
	}

	@Override
	public float getHeight() {
		return 30;
	}

	@Override
	public float getWidth() {
		return settingName.getWidth() + leftArrow.getWidth() + rightArrow.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	private void updateText() {
		settingName.getText().set(0, setting.name());
	}



}
