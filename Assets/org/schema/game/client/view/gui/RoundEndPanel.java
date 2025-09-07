package org.schema.game.client.view.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class RoundEndPanel extends GUIElement {
	private GUITextOverlay infoText;
	private GUITextOverlay errorText;
	private GUIOverlay background;
	private long timeError;
	private long timeErrorShowed;
	private String info;
	private boolean firstDraw = true;

	public RoundEndPanel(InputState state, GUICallback guiCallback, String info) {
		super(state);
		this.info = info;

	}

	@Override
	public void cleanUp() {
		background.cleanUp();
		infoText.cleanUp();
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		infoText.getText().set(0, info);
		GlUtil.glPushMatrix();
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		transform();
		if (timeError < System.currentTimeMillis() - timeErrorShowed) {
			errorText.getText().clear();
		}
		background.draw();

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {

		infoText = new GUITextOverlay(FontSize.BIG_30, getState());

		errorText = new GUITextOverlay(getState());
		background = new GUIOverlay(Controller.getResLoader().getSprite("panel-std-gui-"), getState());

		ArrayList<Object> t = new ArrayList<Object>();
		t.add(info);
		infoText.setText(t);

		ArrayList<Object> te = new ArrayList<Object>();
		errorText.setText(te);
		background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);

		background.onInit();
		infoText.onInit();

		this.attach(background);
		background.attach(infoText);
		background.attach(errorText);

		infoText.setPos(280, 80, 0);
		errorText.setPos(300, 30, 0);

		firstDraw = false;
	}

	@Override
	public float getHeight() {
		return 256;
	}

	@Override
	public float getWidth() {
		return 256;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	public void setErrorMessage(String msg, long timeShowed) {
		errorText.getText().add(msg);
		timeError = System.currentTimeMillis();
		timeErrorShowed = timeShowed;
	}

}
