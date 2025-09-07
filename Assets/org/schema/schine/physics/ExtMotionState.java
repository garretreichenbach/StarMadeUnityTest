package org.schema.schine.physics;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class ExtMotionState extends MotionState {

	/**
	 * Current interpolated world transform, used to draw object.
	 */
	public final Transform graphicsWorldTrans = new Transform();

	/**
	 * Center of mass offset transform, used to adjust graphics world transform.
	 */
	public final Transform centerOfMassOffset = new Transform();
	public final Transform centerOfMassOffsetInv = new Transform();

	/**
	 * Initial world transform.
	 */
	public final Transform startWorldTrans = new Transform();

	private boolean centerDefault = true;

	/**
	 * Creates a new DefaultMotionState with all transforms set to identity.
	 */
	public ExtMotionState() {
		graphicsWorldTrans.setIdentity();
		centerOfMassOffset.setIdentity();
		startWorldTrans.setIdentity();
	}

	/**
	 * Creates a new DefaultMotionState with initial world transform and center
	 * of mass offset transform set to identity.
	 */
	public ExtMotionState(Transform startTrans) {
		this.graphicsWorldTrans.set(startTrans);
		centerOfMassOffset.setIdentity();
		this.startWorldTrans.set(startTrans);

	}

	/**
	 * Creates a new DefaultMotionState with initial world transform and center
	 * of mass offset transform.
	 */
	public ExtMotionState(Transform startTrans, Transform centerOfMassOffset) {
		this.graphicsWorldTrans.set(startTrans);
		this.centerOfMassOffset.set(centerOfMassOffset);
		this.startWorldTrans.set(startTrans);
		centerOfMassOffsetInv.set(centerOfMassOffset);
		centerOfMassOffsetInv.inverse();
		centerDefault = false;
	}

	@Override
	public Transform getWorldTransform(Transform out) {
		if (!centerDefault) {
			out.set(centerOfMassOffsetInv);
			out.mul(graphicsWorldTrans);
			return out;
		} else {
			out.set(graphicsWorldTrans);
			return out;
		}
	}

	@Override
	public void setWorldTransform(Transform centerOfMassWorldTrans) {
		graphicsWorldTrans.set(centerOfMassWorldTrans);
		if (!centerDefault) {
			graphicsWorldTrans.mul(centerOfMassOffset);
		}
	}

}
