package org.schema.game.client.view.camera;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.EditSegmentInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

public class ShipOffsetCameraViewable extends FixedViewer {

	protected Vector3i posMod = new Vector3i();
	protected SegmentController controller;
	protected EditSegmentInterface edit;
	Transform tinv = new Transform();
	Vector3f dist = new Vector3f();
	private Vector3f posCur = new Vector3f();
	private float jumpToBlockSpeed = 13f;
	Vector3f offset = new Vector3f();  
	public ShipOffsetCameraViewable(EditSegmentInterface edit) {
		super(edit.getSegmentController());
		this.controller = edit.getSegmentController();
		this.edit = edit;
	}

	public synchronized Vector3i getCurrentBlock() {
		Vector3i t = new Vector3i(posMod);
		t.add(edit.getCore()); //sub start
		return t;
	}

	/**
	 * @return the jumpToBlockSpeed
	 */
	public float getJumpToBlockSpeed() {
		return jumpToBlockSpeed;
	}

	/**
	 * @param jumpToBlockSpeed the jumpToBlockSpeed to set
	 */
	public void setJumpToBlockSpeed(float jumpToBlockSpeed) {
		this.jumpToBlockSpeed = jumpToBlockSpeed;
	}
	@Override
	public Vector3f getPos() {
		Vector3f pos = super.getPos();
		//		System.err.println("CUR "+posCur+" CORE: "+edit.getCore());
		Vector3f mod = getRelativeCubePos();
		//		Vector3f mod = new Vector3f(posCur.x, posCur.y, posCur.z);

		//		t.setFromOpenGLMatrix(fs);
		tinv.set(getEntity().getWorldTransform());
		//		tinv.inverse(); // put into local space of this ship
		tinv.basis.transform(mod);
		pos.add(mod);
		return pos;
	}

	@Override
	public void update(Timer timer) {
		dist.set(posMod.x, posMod.y, posMod.z);
		dist.sub(posCur);

		float len = dist.length();
		if (len <= 0) {
			return;
		}
		float l = dist.length();
		dist.normalize();

		dist.scale(timer.getDelta() * Math.max(l * 3, jumpToBlockSpeed));

		if (dist.length() < len) {
			//adding if the distance to go is bigger than the distance to target
			posCur.add(dist);
			//			System.err.println("RUUUN "+len+" < "+distTest.length());
		} else {
			//			System.err.println(distTest.length() +" > "+dist.length()+": "+len+"; "+(distTest.length() > len )+"; ");
			//target reached
			posCur.set(posMod.x, posMod.y, posMod.z);
		}

	}

	public Vector3i getPosMod() {
		return posMod;
	}

	public Vector3f getRelativeCubePos() {
		return new Vector3f(
				offset.x + posCur.x + edit.getCore().x - SegmentData.SEG_HALF, 
				offset.y + posCur.y + edit.getCore().y - SegmentData.SEG_HALF, 
				offset.z + posCur.z + edit.getCore().z - SegmentData.SEG_HALF);
	}

	public void jumpTo(Vector3i absPos) {
		absPos.sub(edit.getCore());
		posMod.set(absPos);
	}

	public void jumpToInstantly(Vector3i absPos) {
		absPos.sub(edit.getCore());
		posMod.set(absPos);
		posCur.set(posMod.x, posMod.y, posMod.z);
	}

}
