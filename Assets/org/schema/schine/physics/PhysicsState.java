package org.schema.schine.physics;

public interface PhysicsState {
	public float getLinearDamping();

	public Physics getPhysics();

	public String getPhysicsSlowMsg();

	public float getRotationalDamping();

	public void handleNextPhysicsSubstep(float maxPhysicsSubsteps);

	public String toStringDebug();

	public short getNumberOfUpdate();
}
