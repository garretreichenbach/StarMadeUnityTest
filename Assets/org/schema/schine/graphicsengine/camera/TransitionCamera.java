package org.schema.schine.graphicsengine.camera;

import javax.vecmath.Matrix4f;

import org.schema.schine.graphicsengine.core.Timer;

public class TransitionCamera extends Camera {
	private final Camera oldCamera;
	private final float transitionTime;
	private final Camera newCamera;
	Matrix4f oldM = new Matrix4f();
	Matrix4f newM = new Matrix4f();
	Matrix4f subM = new Matrix4f();
	float t = 0;

	public TransitionCamera(Camera oldCamera, Camera newCamera, float transitionTime) {
		super(oldCamera.state, oldCamera.getViewable());
		this.oldCamera = oldCamera;
		this.transitionTime = transitionTime;
		this.newCamera = newCamera;
	}

	public boolean isActive() {
		//		System.err.println("ACTIVE: ");
		return t < transitionTime;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Camera#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer, boolean server) {

		t += timer.getDelta();

		float pc = t / transitionTime;

		oldCamera.getWorldTransform().getMatrix(oldM);

		newCamera.getWorldTransform().getMatrix(newM);

		subM.sub(newM, oldM);

		subM.mul(pc);

		oldM.add(subM);

		getWorldTransform().set(oldM);

	}

}
