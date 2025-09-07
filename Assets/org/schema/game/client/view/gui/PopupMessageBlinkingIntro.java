package org.schema.game.client.view.gui;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.input.InputState;

public class PopupMessageBlinkingIntro extends GUIElement {

	public float index = 0;
	private GUIOverlay background;
	private float timeDrawn;
	private float timeDelayed;
	private GUITextOverlay text;
	private boolean firstDraw = true;
	private float timeDelayInSecs;
	private LinearTimerUtil linearTimerUtil = new LinearTimerUtil(30);
	private String message;
	private Color color = new Color(1, 1, 1, 1);
	private boolean alive = true;

	public PopupMessageBlinkingIntro(InputState state, String message, Color color) {
		super(state);
		if (background == null) {
			background = new GUIOverlay(Controller.getResLoader().getSprite("std-message-gui-"), state);
			text = new GUITextOverlay(state);
			text.setText(new ArrayList());
		}

		this.message = message;

		this.color.r = color.r;
		this.color.g = color.g;
		this.color.b = color.b;
		this.color.a = color.a;

		text.getText().clear();
		String[] split = message.split("\n");
		for (String s : split) {
			text.getText().add(s);
		}
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (!alive || timeDelayed < timeDelayInSecs) {
			return;
		}

		//		text.setColor(color);
		background.getSprite().getTint().set(color.r, color.g, color.b, 1);
		if (timeDrawn < 0.3f) {

			if (linearTimerUtil.getTime() < 0.5f) {
				float f = Math.max(1, (1.0f - timeDrawn * 5));
				background.getSprite().getTint().set(f / 2 + color.r, f / 2 + color.g, f / 2 + color.b, 1);
			}
		}
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlUtil.glPushMatrix();
		transform();
		background.draw();
		GlUtil.glPopMatrix();

		GlUtil.glDisable(GL11.GL_BLEND);

		background.getSprite().getTint().set(1, 1, 1, 1);
		text.getColor().a = 1;
		text.getColor().r = 1;
		text.getColor().g = 1;
		text.getColor().b = 1;
	}

	@Override
	public void onInit() {

		text.setPos(30, 30, 0);
		text.setColor(Color.white);
		text.setFont(FontSize.TINY_12);
		text.onInit();

		background.getSprite().setTint(new Vector4f(1, 1, 1, 1));
		background.onInit();

		background.attach(text);

		//		setScale(0.5f,0.5f,0.5f);

		firstDraw = false;
	}

	public void deactivate() {
		alive = false;
	}

	@Override
	public float getHeight() {
		return background.getHeight();
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isAlive() {
		return alive;
	}

	/**
	 * restarts the popup message
	 * without delaying
	 * the restart
	 */
	public void restartPopupMessage() {
		alive = true;
	}

	/**
	 * starts a popup message with a delay
	 *
	 * @param timeDelayInSecs
	 */
	public void startPopupMessage(float timeDelayInSecs) {
		this.timeDelayInSecs = timeDelayInSecs;
		timeDelayed = 0;
		timeDrawn = 0;
	}

	@Override
	public void update(Timer timer) {
		if (timeDelayed < timeDelayInSecs) {
			timeDelayed += timer.getDelta();
			return;
		}
		timeDrawn += timer.getDelta();
		linearTimerUtil.update(timer);

	}

}
