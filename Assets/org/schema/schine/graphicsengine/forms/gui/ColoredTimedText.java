package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;

public class ColoredTimedText implements ColoredInterface {

	public static final LinearTimerUtil blink = new LinearTimerUtil(25);
	private static final float FADE_START = 3;
	private final Vector4f color = new Vector4f();
	public String text;
	protected float lengthInSec = 15;
	private float currentLengthInSec;
	private Vector4f startColor = new Vector4f(1, 1, 1, 1);

	public ColoredTimedText() {

	}

	public ColoredTimedText(ColoredTimedText o) {
		this.startColor = o.getStartColor();
		this.text = o.text;
		this.lengthInSec = o.lengthInSec;
		reset();
	}

	public ColoredTimedText(String text, Vector4f startColor, float lengthInSec) {
		this.startColor = startColor;
		this.text = text;
		this.lengthInSec = lengthInSec;
		reset();
	}

	@Override
	public Vector4f getColor() {
		return color;
	}

	public boolean isAlive() {
		return currentLengthInSec > 0;
	}

	public void reset() {
		this.currentLengthInSec = lengthInSec;
		if(getStartColor() != null){
			this.color.set(getStartColor());
		}
//		try {
//			throw new Exception("COLOR: :::: "+text+" -> "+color);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return text;
	}

	public void update(Timer timer) {
		currentLengthInSec = Math.max(0, currentLengthInSec - timer.getDelta());

		if (currentLengthInSec > lengthInSec - 0.25f) {
			color.x = 1.0f - 0.5f * blink.getTime();
			color.y = 1.0f - 0.5f * blink.getTime();
		} else {
			color.set(getStartColor());
		}
		if (currentLengthInSec < FADE_START) {
			float alpha = currentLengthInSec / FADE_START;
			color.w = alpha;
		}

	}

	/**
	 * @return the startColor
	 */
	public Vector4f getStartColor() {
		return startColor;
	}

	/**
	 * @param startColor the startColor to set
	 */
	public void setStartColor(Vector4f startColor) {
		this.startColor = startColor;
	}

}
