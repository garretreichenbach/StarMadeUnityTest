package org.schema.game.client.view.camera;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.look.AxialCameraLook;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;

import com.bulletphysics.linearmath.Transform;

public class GameMapCamera extends Camera {

	private Transform stdTransform;

	public GameMapCamera(GameClientState state, Transformable e) {
		super(state, new UpperFixedViewer(e));
		this.setCharacter(e);
		this.stdTransform = new Transform();
		stdTransform.setIdentity();
		//		setLookAlgorithm(new TransformableRestrictedCameraLook(this, e));
		setLookAlgorithm(new AxialCameraLook(this));
	}

	public void jumpTo(Vector3i absPos) {
		((ShipOffsetCameraViewable) getViewable()).jumpTo(absPos);
	}

	//
	//	public ClosestRayResultCallback getNearestIntersection(PlayerCharacter playerCharacter){
	//		if(getCameraOffset() > 0){
	//
	//			Vector3f camPos = new Vector3f(playerCharacter.getHeadWorldTransform().origin);
	//
	//			Vector3f camTo = new Vector3f(getOffsetPos(new Vector3f()));
	//
	//			CubeRayCastResult rayCallback = new CubeRayCastResult(
	//					camPos, camTo, false, null);
	//			rayCallback.setRespectShields(false);
	//			rayCallback.onlyCubeMeshes = true;
	//			ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt)state.getPhysics()).testRayCollisionPoint(
	//					camPos, camTo,  rayCallback, false);
	//
	//			return testRayCollisionPoint;
	//		}
	//		return null;
	//
	//		//((PhysicsExt)getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, null, null, false, null, false);
	//	}

	/**
	 * @param e the character to set
	 */
	public void setCharacter(Transformable e) {
		((FixedViewer) getViewable()).setEntity(e);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Camera#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer, boolean server) {
		((AxialCameraLook) getLookAlgorithm()).getFollowing().set(stdTransform);
		super.update(timer, server);

	}

	//	@Override
	//	protected int limitWheel(int in){
	//		return Math.max(0,  Math.min(in, 2500));
	//	}

	public void updateTransition(Timer timer) {

		/*
		t += timer.getDelta();

		float pc = t / transitionTime;

		oldCamera.getWorldTransform().getMatrix(oldM);

		newCamera.getWorldTransform().getMatrix(newM);

		subM.sub(newM, oldM);

		subM.mul(pc);

		oldM.add(subM);


		getWorldTransform().set(oldM);
		 */
	}

}
