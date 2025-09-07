package org.schema.game.client.view.effects;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.ColoredInterface;

import com.bulletphysics.linearmath.Transform;

public abstract class Indication {
	private static final float TEXT_MAX_DISTANCE = 100;
	public float lifetime = 0.3f;
	public Transform start;
	private float timeLived;
	private Object text;
	private Vector4f color = null;
	private float dist;

	public Indication(Transform start, Object text) {
		this.start = start;
		this.text = text;
		this.dist = TEXT_MAX_DISTANCE;
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(final Vector4f color) {
		final String t = text.toString();
		this.color = color;
		this.text = new ColoredInterface() {
			@Override
			public Vector4f getColor() {
				return color;
			}

			@Override
			public String toString() {
				return t;
			}		
		};
	}

	public abstract Transform getCurrentTransform();

	/**
	 * @return the text
	 */
	public Object getText() {
		return text;
	}

	public void setText(Object text) {
		this.text = text;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
				return text.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((Indication) obj).text.equals(text);
	}

	public boolean isAlive() {
		return timeLived < lifetime;
	}

	public float scaleIndication() {
		return 1;
	}

	public void update(Timer timer) {
		this.timeLived += timer.getDelta();
	}

	/**
	 * @return the dist
	 */
	public float getDist() {
		return dist;
	}

	/**
	 * @param dist the dist to set
	 */
	public void setDist(float dist) {
		this.dist = dist;
	}

}
