package org.schema.schine.graphicsengine.camera.viewer;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Positionable;

public abstract class AbstractViewer implements Positionable {

	/**
	 * The speed.
	 */
	private Vector3f speed = new Vector3f();

	/**
	 * The viewer height.
	 */
	private float viewerHeight = 0.0f;

	private Camera camera;

	public AbstractViewer() {
	}

	/**
	 * @return the camera
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * @param camera the camera to set
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.camera.Camera#getForward()
	 */
	public Vector3f getForward() {
		return camera.getForward();
	}

	/**
	 * @param val
	 * @see org.schema.schine.graphicsengine.camera.Camera#setForward(javax.vecmath.Vector3f)
	 */
	public void setForward(Vector3f val) {
		camera.setForward(val);
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.camera.Camera#getLeft()
	 */
	public Vector3f getLeft() {
		return camera.getLeft();
	}

	/**
	 * @param val
	 * @see org.schema.schine.graphicsengine.camera.Camera#setLeft(javax.vecmath.Vector3f)
	 */
	public void setLeft(Vector3f val) {
		camera.setLeft(val);
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.camera.Camera#getRight()
	 */
	public Vector3f getRight() {
		return camera.getRight();
	}

	/**
	 * @param val
	 * @see org.schema.schine.graphicsengine.camera.Camera#setRight(javax.vecmath.Vector3f)
	 */
	public void setRight(Vector3f val) {
		camera.setRight(val);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Viewable#getSpeed()
	 */
	public Vector3f getSpeed() {
		return speed;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Viewable#setSpeed(org.schema.common.util.linAlg.Vector3f)
	 */
	public void setSpeed(Vector3f speed) {
		this.speed = speed;
	}

	/**
	 * @return
	 * @see org.schema.schine.graphicsengine.camera.Camera#getUp()
	 */
	public Vector3f getUp() {
		return camera.getUp();
	}

	/**
	 * @param val
	 * @see org.schema.schine.graphicsengine.camera.Camera#setUp(javax.vecmath.Vector3f)
	 */
	public void setUp(Vector3f val) {
		camera.setUp(val);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Viewable#getViewerHeight()
	 */
	public float getViewerHeight() {
		return viewerHeight;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Viewable#setViewerHeight(float)
	 */
	public void setViewerHeight(float viewerHeight) {
		this.viewerHeight = viewerHeight;
	}

	public void update(Timer timer) {
		
	}


}
