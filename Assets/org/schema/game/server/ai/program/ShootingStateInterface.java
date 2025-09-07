package org.schema.game.server.ai.program;

import javax.vecmath.Vector3f;

public interface ShootingStateInterface {
	public int getTargetId();

	public byte getTargetType();

	/**
	 * @return the shootingDir
	 */
	public Vector3f getTargetPosition();

	public Vector3f getTargetVelocity();

}
