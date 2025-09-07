package org.schema.schine.graphicsengine.camera.viewer;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Timer;

public class PositionableViewer extends AbstractViewer {

	Vector3f position = new Vector3f();

	/**
	 * Instantiates a new viewer.
	 */
	public PositionableViewer() {
	}

	@Override
	public Vector3f getPos() {
		return position;
	}

	@Override
	public void update(Timer timer) {

	}

}