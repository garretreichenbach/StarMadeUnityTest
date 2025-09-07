package org.schema.schine.graphicsengine.forms;

import javax.vecmath.Vector3f;

public interface Orientatable extends Positionable {
	public Vector3f getForward();

	public void setForward(Vector3f val);

	public Vector3f getLeft();

	public void setLeft(Vector3f val);

	public Vector3f getRight();

	public void setRight(Vector3f val);

	public Vector3f getUp();

	public void setUp(Vector3f val);

}
