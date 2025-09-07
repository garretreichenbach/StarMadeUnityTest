package org.schema.game.client.view.gui;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.network.client.ClientState;

import java.util.ArrayList;
import java.util.Collections;

public class BigMessage extends GUIElement {

	public float index = 0;
	private float timeOutInSeconds = 5;
	private float timeDrawn;
	private float timeDelayed;
	private GUITextOverlay text;
	private boolean firstDraw = true;
	private float timeDelayInSecs;
	private float currentIndex = 0;
	private Color color = new Color(1, 1, 1, 1);
	private String id;
	private GUITextOverlay subtitle;

	//INSERTED CODE
	public GUITextOverlay getText() {
		return text;
	}

	public GUITextOverlay getSubtitle() {
		return subtitle;
	}
	///

	public BigMessage(String id, ClientState state, String message, String subtitleText, Color color) {
		super(state);
		this.id = id;
		text = new GUITextOverlay(FontSize.BIG_30, state);
		text.setText(new ArrayList());

		subtitle = new GUITextOverlay(FontSize.MEDIUM_15, state);
		subtitle.setText(new ArrayList());

		this.color.r = color.r;
		this.color.g = color.g;
		this.color.b = color.b;
		this.color.a = color.a;

		text.getText().add(message);
		subtitle.getText().add(subtitleText);
	}

	@Override
	public void cleanUp() {
		text.cleanUp();
		subtitle.cleanUp();
	}

	@Override
	public void draw() {
		if(firstDraw) {
			onInit();
		}
		if(timeDelayed < timeDelayInSecs) {
			return;
		}
		getPos().x = GLFrame.getWidth() / 2 - text.getFont().getWidth(text.getText().get(0).toString()) / 2;
		getPos().y = currentIndex + (100);
		if(!isOnScreen()) {
			return;
		}
		//		text.setColor(color);
		float a = timeOutInSeconds - timeDrawn;
		if(a < 1) {
			text.getColor().a = a;
			subtitle.getColor().a = a;
		}
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlUtil.glPushMatrix();
		transform();
		text.draw();
		GlUtil.glPopMatrix();

		getPos().x = GLFrame.getWidth() / 2 - subtitle.getFont().getWidth(subtitle.getText().get(0).toString()) / 2;
		GlUtil.glPushMatrix();
		transform();
		GlUtil.translateModelview(0, text.getFont().getLineHeight() + 5, 0);
		subtitle.draw();
		GlUtil.glPopMatrix();

		GlUtil.glDisable(GL11.GL_BLEND);

		//INSERTED CODE
		this.text.setColor(color);
		//todo: control subtitle color
		this.subtitle.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		///
	}

	@Override
	public void onInit() {

		text.setColor(Color.white);
		text.onInit();

		//		setScale(0.5f,0.5f,0.5f);

		firstDraw = false;

		currentIndex = -1f * ((getHeight() * getScale().y) + 5);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.id.equals(((BigMessage) obj).id);
	}

	@Override
	public float getHeight() {
		return text.getHeight() + 5 + subtitle.getHeight();
	}

	@Override
	public float getWidth() {
		return text.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public boolean isAlive() {
		return timeDrawn < timeOutInSeconds;
	}

	/**
	 * restarts the popup message
	 * without delaying
	 * the restart
	 */
	public void restartPopupMessage() {
		timeDrawn = 0;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		text.getText().set(0, message);
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

	public void timeOut() {
		if(timeDrawn < timeOutInSeconds - 1) {
			timeDrawn = timeOutInSeconds - 1;
		}
	}

	@Override
	public void update(Timer timer) {
		if(timeDelayed < timeDelayInSecs) {
			timeDelayed += timer.getDelta();
			return;
		}
		timeDrawn += timer.getDelta();

		float targetYPos = this.index * ((getHeight() * getScale().y) + 5);

		float distSpeed = Math.min(1.0f, (Math.max(0.01f, Math.abs(currentIndex - targetYPos))) / (getHeight() * getScale().y));

		if(currentIndex > targetYPos) {
			currentIndex -= timer.getDelta() * 1000 * distSpeed;
			if(currentIndex <= targetYPos) {
				currentIndex = targetYPos;
			}
		} else if(currentIndex < targetYPos) {
			currentIndex += timer.getDelta() * 1000 * distSpeed;
			if(currentIndex >= targetYPos) {
				currentIndex = targetYPos;
			}
		}

	}

}
