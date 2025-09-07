package org.schema.schine.network.objects;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

public class NetworkTransformation {
	public boolean received;
	public boolean sendVil;
	public boolean receivedVil;
	private Transform transform;
	private Transform transformReceive;
	private final Vector3f lin = new Vector3f();
	private final Vector3f ang = new Vector3f();
	private final Vector3f linReceive = new Vector3f();
	private final Vector3f angReceive = new Vector3f();
	private long timeStamp;
	private long timeStampReceive;
	private boolean playerAttached;
	private boolean playerAttachedReceive;
	public boolean prime;
	


	public NetworkTransformation() {
		super();
		transform = new Transform();
		transformReceive = new Transform();

	}

	public NetworkTransformation(Transform transform, long timeStamp) {
		super();
		this.transform = transform;
		this.transformReceive = new Transform(transform);
		this.timeStamp = timeStamp;
		this.timeStampReceive = timeStamp;
	}

	/**
	 * @return the ang
	 */
	public Vector3f getAng() {
		return ang;
	}

	/**
	 * @param ang the ang to set
	 */
	public void setAng(Vector3f ang) {
		this.ang.set(ang);
	}

	/**
	 * @return the angReceive
	 */
	public Vector3f getAngReceive() {
		return angReceive;
	}


	/**
	 * @return the lin
	 */
	public Vector3f getLin() {
		return lin;
	}

	/**
	 * @param lin the lin to set
	 */
	public void setLin(Vector3f lin) {
		this.lin.set(lin);
	}

	/**
	 * @return the linReceive
	 */
	public Vector3f getLinReceive() {
		return linReceive;
	}


	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the timeStampReceive
	 */
	public long getTimeStampReceive() {
		return timeStampReceive;
	}

	/**
	 * @param timeStampReceive the timeStampReceive to set
	 */
	public void setTimeStampReceive(long timeStampReceive) {
		this.timeStampReceive = timeStampReceive;
	}

	/**
	 * @return the transform
	 */
	public Transform getTransform() {
		return transform;
	}

	/**
	 * @param transform the transform to set
	 */
	public void setTransform(Transform transform) {
		this.transform = transform;
	}

	/**
	 * @return the transformReceive
	 */
	public Transform getTransformReceive() {
		return transformReceive;
	}

	/**
	 * @param transformReceive the transformReceive to set
	 */
	public void setTransformReceive(Transform transformReceive) {
		this.transformReceive = transformReceive;
	}

	/**
	 * @return the playerAttached
	 */
	public boolean isPlayerAttached() {
		return playerAttached;
	}

	/**
	 * @param playerAttached the playerAttached to set
	 */
	public void setPlayerAttached(boolean playerAttached) {
		this.playerAttached = playerAttached;
	}

	/**
	 * @return the playerAttachedReceive
	 */
	public boolean isPlayerAttachedReceive() {
		return playerAttachedReceive;
	}

	/**
	 * @param playerAttachedReceive the playerAttachedReceive to set
	 */
	public void setPlayerAttachedReceive(boolean playerAttachedReceive) {
		this.playerAttachedReceive = playerAttachedReceive;
	}

}
